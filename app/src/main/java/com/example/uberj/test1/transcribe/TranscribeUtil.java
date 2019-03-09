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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class TranscribeUtil {
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
        String transcription = convertKeyPressesToString(session.enteredKeys);
        String message = Joiner.on("").join(session.playedKeys);
        DiffPatchMatch dmp = new DiffPatchMatch();
        LinkedList<DiffPatchMatch.Diff> messageDiff = dmp.diff_main(message, transcription);
        dmp.diff_cleanupSemantic(messageDiff);
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

        return new TranscribeSessionAnalysis(messageSpan);
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

    public static class TranscribeSessionAnalysis {
        public final SpannableStringBuilder messageSpan;

        public TranscribeSessionAnalysis(SpannableStringBuilder messageSpan) {
            this.messageSpan = messageSpan;
        }
    }
}
