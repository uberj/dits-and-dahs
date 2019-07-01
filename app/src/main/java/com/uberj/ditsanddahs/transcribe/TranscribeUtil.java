package com.uberj.ditsanddahs.transcribe;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;

import androidx.core.content.ContextCompat;

import com.annimon.stream.Optional;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.keyboards.KeyConfig;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TranscribeUtil {
    public static String convertKeyPressesToString(List<String> enteredStrings) {
        List<String> stringsToDisplay = Lists.newArrayList();
        for (String transcribedString : enteredStrings) {
            Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(transcribedString);
            if (controlType.isPresent()) {
                if (controlType.get().equals(KeyConfig.ControlType.DELETE)) {
                    if (!stringsToDisplay.isEmpty()) {
                        stringsToDisplay.remove(stringsToDisplay.size() - 1);
                    }
                } else if (controlType.get().equals(KeyConfig.ControlType.SPACE)) {
                    stringsToDisplay.add(" ");
                } else {
                    throw new RuntimeException("unhandled control eventType " + transcribedString);
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
            String text = diff.text;
            if (diff.text.contains(QSOWordSupplier.STATION_SWITCH_MARKER)) {
                text = text.replace(QSOWordSupplier.STATION_SWITCH_MARKER, "\n\n");
            }

            messageSpan.append(text);

            Optional<CharacterStyle> spanColor = getSpanColor(context, diff.operation);
            if (spanColor.isPresent()) {
                int cursorEnd = cursor + text.length();
                messageSpan.setSpan(
                        spanColor.get(),
                        cursor,
                        cursorEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            cursor = cursor + text.length();
        }

        Map<String, Pair<Integer, Integer>> errorMap = calculateHitMap(session);
        float overallAccuracyRate = TranscribeUtil.calculateOverallAccuracyForSession(errorMap);
        long sessionDuration  = TranscribeUtil.calcSessionDuration(session);
        return new TranscribeSessionAnalysis(messageSpan, overallAccuracyRate, errorMap, sessionDuration);
    }

    private static long calcSessionDuration(TranscribeTrainingSession session) {
        // TODO
        return session.durationRequestedMillis;
    }

    private static LinkedList<DiffPatchMatch.Diff> calcMessageDiff(TranscribeTrainingSession session) {
        List<String> transcribedKeys = stripTrailingSpaces(session.enteredKeys);
        List<String> playedKeys = stripTrailingSpaces(session.playedMessage);

        String transcription = convertKeyPressesToString(transcribedKeys);
        String message = Joiner.on("").join(playedKeys);
        DiffPatchMatch dmp = new DiffPatchMatch();
        return dmp.diff_main(message, transcription);
    }

    protected static List<String> stripTrailingSpaces(List<String> inputStrings) {
        List<String> outputStrings = Lists.newArrayList(inputStrings);
        while (outputStrings.size() > 0 && outputStrings.get(outputStrings.size() - 1).equals("SPC")) {
            outputStrings.remove(outputStrings.size() -1);
        }
        return outputStrings;
    }

    private static Optional<CharacterStyle> getSpanColor(Context context, DiffPatchMatch.Operation operation) {
        if (operation == DiffPatchMatch.Operation.DELETE) {
            return Optional.of(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.incorrectMissedCharacterColor)));
        } else if (operation == DiffPatchMatch.Operation.EQUAL) {
            return Optional.empty();
        } else if (operation == DiffPatchMatch.Operation.INSERT) {
            return Optional.of(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.incorrectMissedCharacterColor)));
        } else {
            throw new RuntimeException("Unknown diff operation " + operation);
        }
    }

    public static Map<String, Double> calculateErrorMap(TranscribeTrainingSession session) {
        Map<String, Pair<Integer, Integer>> hitMap = TranscribeUtil.calculateHitMap(session);
        Map<String, Double> errorMap = Maps.newHashMap();
        for (Map.Entry<String, Pair<Integer, Integer>> entry : hitMap.entrySet()) {
            String s = entry.getKey();
            Pair<Integer, Integer> hitMisses = entry.getValue();
            double error = 1 - (hitMisses.getLeft().doubleValue() / hitMisses.getRight().doubleValue());
            errorMap.put(s, error);
        }

        return errorMap;
    }

    public static Map<String, Pair<Integer, Integer>> calculateHitMap(TranscribeTrainingSession session) {
        LinkedList<DiffPatchMatch.Diff> messageDiff = calcMessageDiff(session);
        HashMap<String, Integer> opportunityCountMap = Maps.newHashMap();
        HashMap<String, Integer> hitCountMap = Maps.newHashMap();
        for (String s : session.playedMessage) {
            if (s.equals(QSOWordSupplier.STATION_SWITCH_MARKER)) {
                continue;
            }

            if (opportunityCountMap.containsKey(s)) {
                Integer v = opportunityCountMap.get(s);
                opportunityCountMap.put(s, v + 1);
            } else {
                opportunityCountMap.put(s, 1);
            }
        }

        for (DiffPatchMatch.Diff diff : messageDiff) {
            for (char c : diff.text.toCharArray()) {
                String s = String.valueOf(c);
                if (diff.operation != DiffPatchMatch.Operation.EQUAL) {
                    continue;
                }

                if (hitCountMap.containsKey(s)) {
                    Integer v = hitCountMap.get(s);
                    hitCountMap.put(s, v + 1);
                } else {
                    hitCountMap.put(s, 1);
                }
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
        public final double overallAccuracyRate;
        public final Map<String, Pair<Integer, Integer>> hitMap;
        public final long sessionDuration;

        public TranscribeSessionAnalysis(SpannableStringBuilder messageSpan, double overallAccuracyRate, Map<String, Pair<Integer, Integer>> hitMap, long sessionDuration) {
            this.messageSpan = messageSpan;
            this.overallAccuracyRate = overallAccuracyRate;
            this.hitMap = hitMap;
            this.sessionDuration = sessionDuration;
        }
    }
}
