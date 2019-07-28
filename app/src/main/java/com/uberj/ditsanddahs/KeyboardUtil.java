package com.uberj.ditsanddahs;

import com.annimon.stream.Optional;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.keyboards.KeyConfig;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class KeyboardUtil {
    public static Pair<Integer, String> convertKeyPressesToString(List<String> enteredStrings) {
        for (String enteredString : enteredStrings) {
            System.out.println(enteredString);
        }
        System.out.println("----");
        if (enteredStrings.isEmpty()) {
            return Pair.of(0, "");
        }
        String first = enteredStrings.get(0);
        if (first.startsWith("v1:")) {
            return handleV1Format(enteredStrings);
        } else {
            return handleLegacyFormat(enteredStrings);
        }
    }

    private static Pair<Integer, String> handleLegacyFormat(List<String> enteredStrings) {
        List<String> stringsToDisplay = Lists.newArrayList();
        for (String buttonPress : enteredStrings) {
            Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(buttonPress);
            if (controlType.isPresent()) {
                if (controlType.get().equals(KeyConfig.ControlType.DELETE)) {
                    if (!stringsToDisplay.isEmpty()) {
                        stringsToDisplay.remove(stringsToDisplay.size() - 1);
                    }
                } else if (controlType.get().equals(KeyConfig.ControlType.SPACE)) {
                    stringsToDisplay.add(" ");
                } else {
                    throw new RuntimeException("unhandled control eventType " + buttonPress);
                }
            } else {
                stringsToDisplay.add(buttonPress);
            }
        }
        String output = Joiner.on("").join(stringsToDisplay);
        return Pair.of(output.length(), output);
    }

    private static Pair<Integer, String> handleV1Format(List<String> enteredStrings) {
        List<String> currentInput = Lists.newArrayList();
        int lastStartSelection = 0;

        for (int curIdx = 0; curIdx < enteredStrings.size(); curIdx++) {
            String rawButtonPress = enteredStrings.get(curIdx);
            rawButtonPress = rawButtonPress.replace("v1:", "");
            String[] parts = rawButtonPress.split(":");
            String buttonName = parts[0];
            int startSelection = Integer.valueOf(parts[1]);
            int endSelection = Integer.valueOf(parts[2]);
            Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(buttonName);
            if (controlType.isPresent()) {
                if (controlType.get().equals(KeyConfig.ControlType.DELETE)) {
                    if (startSelection == endSelection) {
                        if (startSelection == 0) {
                            lastStartSelection = startSelection;
                        } else {
                            lastStartSelection = startSelection - 1;
                            for (int delIdx = lastStartSelection; delIdx < endSelection; delIdx++) {
                                currentInput.remove(delIdx);
                            }
                        }
                    } else {
                        lastStartSelection = startSelection;
                        if (endSelection > startSelection) {
                            currentInput.subList(startSelection, endSelection).clear();
                        }
                    }
                } else if (controlType.get().equals(KeyConfig.ControlType.SPACE)) {
                    lastStartSelection = startSelection + 1;
                    currentInput.add(startSelection, " ");
                }
            } else {
                lastStartSelection = startSelection + 1;
                currentInput.add(startSelection, buttonName);
            }
        }
        return Pair.of(lastStartSelection, Joiner.on("").join(currentInput));
    }

    public static List<String> stripTrailingSpaces(List<String> inputStrings) {
        List<String> outputStrings = Lists.newArrayList(inputStrings);
        while (outputStrings.size() > 0 && outputStrings.get(outputStrings.size() - 1).equals("SPC")) {
            outputStrings.remove(outputStrings.size() -1);
        }
        return outputStrings;
    }

}
