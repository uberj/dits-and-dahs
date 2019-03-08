package com.example.uberj.test1.transcribe;

import com.example.uberj.test1.keyboards.KeyConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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
}
