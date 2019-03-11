package com.example.uberj.test1.transcribe;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;

import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TranscribeUtil {
    private final double SUGGESTION_CUTOFF = 0.90;

    public static String convertKeyPressesToString(List<String> enteredStrings) {
        List<String> stringsToDisplay = Lists.newArrayList();
        for (String transcribedString : enteredStrings) {
            Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(transcribedString);
            if (controlType.isPresent()) {
                if (controlType.get().equals(KeyConfig.ControlType.DELETE)) {
                    stringsToDisplay.remove(stringsToDisplay.size() - 1);
                } else if (controlType.get().equals(KeyConfig.ControlType.SPACE)) {
                    stringsToDisplay.add(" ");
                } else {
                    throw new RuntimeException("unhandled control type " + transcribedString);
                }
            } else {
                stringsToDisplay.add(transcribedString);
            }
        }
        return Joiner.on("").join(stringsToDisplay);
    }

    public static TranscribeSessionAnalysis analyzeSession(Context context, TranscribeTrainingSession session) {
        LinkedList<DiffPatchMatch.Diff> messageDiff = calcMessageDiff(session);
        SpannableStringBuilder messageSpan = new SpannableStringBuilder("");
        int cursor = 0;
        for (DiffPatchMatch.Diff diff : messageDiff) {
            messageSpan.append(diff.text);
            Optional<CharacterStyle> spanColor = getSpanColor(context, diff.operation);
            if (spanColor.isPresent()) {
                int cursorEnd = cursor + diff.text.length();
                messageSpan.setSpan(
                        spanColor.get(),
                        cursor,
                        cursorEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            cursor = cursor + diff.text.length();
        }

        Map<String, Pair<Integer, Integer>> errorMap = calculateHitMap(session);
        float overallAccuracyRate = TranscribeUtil.calculateOverallAccuracyForSession(errorMap);
        return new TranscribeSessionAnalysis(messageSpan, overallAccuracyRate, errorMap);
    }

    private static LinkedList<DiffPatchMatch.Diff> calcMessageDiff(TranscribeTrainingSession session) {
        String transcription = convertKeyPressesToString(session.enteredKeys);
        String message = Joiner.on("").join(session.playedMessage);
        DiffPatchMatch dmp = new DiffPatchMatch();
        LinkedList<DiffPatchMatch.Diff> messageDiff = dmp.diff_main(message, transcription);
        dmp.diff_cleanupSemantic(messageDiff);
        return messageDiff;
    }

    private static Optional<CharacterStyle> getSpanColor(Context context, DiffPatchMatch.Operation operation) {
        if (operation == DiffPatchMatch.Operation.DELETE) {
            return Optional.of(new BackgroundColorSpan(context.getResources().getColor(R.color.incorrectMissedCharacterColor, context.getTheme())));
        } else if (operation == DiffPatchMatch.Operation.EQUAL) {
            return Optional.empty();
        } else if (operation == DiffPatchMatch.Operation.INSERT) {
            return Optional.of(new BackgroundColorSpan(context.getResources().getColor(R.color.incorrectAddedCharacterColor, context.getTheme())));
        } else {
            throw new RuntimeException("Unknown diff operation " + operation);
        }
    }

    public static ArrayList<String> calculateSuggestedStrings(TranscribeTrainingSession session) {
        LinkedList<DiffPatchMatch.Diff> messageDiff = calcMessageDiff(session);
        for (DiffPatchMatch.Diff diff : messageDiff) {
            if (diff.operation == DiffPatchMatch.Operation.EQUAL) {
                continue;
            }

        }
        return null;
    }

    public static Map<String, Pair<Integer, Integer>> calculateHitMap(TranscribeTrainingSession session) {
        LinkedList<DiffPatchMatch.Diff> messageDiff = calcMessageDiff(session);
        HashMap<String, Integer> opportunityCountMap = Maps.newHashMap();
        HashMap<String, Integer> hitCountMap = Maps.newHashMap();
        for (String s : session.playedMessage) {
            opportunityCountMap.putIfAbsent(s, 0);
            opportunityCountMap.computeIfPresent(s, (k, v) -> v + 1);
        }

        for (DiffPatchMatch.Diff diff : messageDiff) {
            for (char c : diff.text.toCharArray()) {
                String s = String.valueOf(c);
                if (diff.operation != DiffPatchMatch.Operation.EQUAL) {
                    continue;
                }

                if (!session.stringsRequested.contains(s) && !s.equals(" ")) {
                    throw new RuntimeException("Issue finding diff char in played keys");
                }
                hitCountMap.putIfAbsent(s, 0);
                hitCountMap.computeIfPresent(s, (k, v) -> v + 1);
            }
        }

        HashMap<String, Pair<Integer, Integer>> percentMap = Maps.newHashMap();
        for (Map.Entry<String, Integer> countEntry : opportunityCountMap.entrySet()) {
            String key = countEntry.getKey();
            Integer playCount = countEntry.getValue();
            int errorCount;
            if (hitCountMap.containsKey(key)) {
                //noinspection ConstantConditions
                errorCount = hitCountMap.get(key);
            } else {
                errorCount = 0;
            }

            percentMap.put(key, Pair.of(errorCount, playCount));
        }

        return percentMap;
    }

    public static float calculateOverallAccuracyForSession(Map<String, Pair<Integer, Integer>> errorMap) {
        double totalError = 0;
        for (Pair<Integer, Integer> error : errorMap.values()) {
            totalError += error.getLeft().doubleValue() / error.getRight().doubleValue();
        }
        return (float) (totalError / errorMap.size());
    }

    public static class TranscribeSessionAnalysis {
        public final SpannableStringBuilder messageSpan;
        public final float overallAccuracyRate;
        public final Map<String, Pair<Integer, Integer>> hitMap;

        public TranscribeSessionAnalysis(SpannableStringBuilder messageSpan, float overallAccuracyRate, Map<String, Pair<Integer, Integer>> hitMap) {
            this.messageSpan = messageSpan;
            this.overallAccuracyRate = overallAccuracyRate;
            this.hitMap = hitMap;
        }
    }
}
