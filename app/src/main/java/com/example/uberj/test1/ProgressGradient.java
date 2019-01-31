package com.example.uberj.test1;

import android.graphics.Color;

import com.google.common.collect.ImmutableMap;

public class ProgressGradient {

    public static final int DISABLED = Color.parseColor("#DCDCDC") ;
    /* Red - Yellow - Green */
    private static final ImmutableMap<Integer, String> weightToColor = ImmutableMap.<Integer, String>builder()
            .put(100, "#57bb8a")
            .put(95, "#63b682")
            .put(90, "#73b87e")
            .put(85, "#84bb7b")
            .put(80, "#94bd77")
            .put(75, "#a4c073")
            .put(70, "#b0be6e")
            .put(65, "#c4c56d")
            .put(60, "#d4c86a")
            .put(55, "#e2c965")
            .put(50, "#f5ce62")
            .put(45, "#f3c563")
            .put(40, "#e9b861")
            .put(35, "#e6ad61")
            .put(30, "#ecac67")
            .put(25, "#e9a268")
            .put(20, "#e79a69")
            .put(15, "#e5926b")
            .put(10, "#e2886c")
            .put(5 , "#e0816d")
            .put(0 , "#dd776e")
            .build();
    public static int forWeight(Integer competencyWeight) {
        if (competencyWeight > 100 || competencyWeight < 0) {
            throw new RuntimeException("CompetencyWeight is invalid: " + competencyWeight);
        }
        return Color.parseColor(weightToColor.get((competencyWeight / 5) * 5));
    }
}
