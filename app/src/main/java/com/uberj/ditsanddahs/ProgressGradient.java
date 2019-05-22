package com.uberj.ditsanddahs;

import android.graphics.Color;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ProgressGradient {

    public static final int DISABLED = Color.parseColor("#DCDCDC") ;
    /* Red - Yellow - Green */
    private static final ImmutableMap<Integer, Integer> weightToColor = ImmutableMap.<Integer, Integer>builder()
            .put(100, Color.parseColor("#57bb8a"))
            .put(95, Color.parseColor("#63b682"))
            .put(90, Color.parseColor("#73b87e"))
            .put(85, Color.parseColor("#84bb7b"))
            .put(80, Color.parseColor("#94bd77"))
            .put(75, Color.parseColor("#a4c073"))
            .put(70, Color.parseColor("#b0be6e"))
            .put(65, Color.parseColor("#c4c56d"))
            .put(60, Color.parseColor("#d4c86a"))
            .put(55, Color.parseColor("#e2c965"))
            .put(50, Color.parseColor("#f5ce62"))
            .put(45, Color.parseColor("#f3c563"))
            .put(40, Color.parseColor("#e9b861"))
            .put(35, Color.parseColor("#e6ad61"))
            .put(30, Color.parseColor("#ecac67"))
            .put(25, Color.parseColor("#e9a268"))
            .put(20, Color.parseColor("#e79a69"))
            .put(15, Color.parseColor("#e5926b"))
            .put(10, Color.parseColor("#e2886c"))
            .put(5 , Color.parseColor("#e0816d"))
            .put(0 , Color.parseColor("#dd776e"))
            .build();

    public static int forWeight(Integer competencyWeight) {
        if (competencyWeight > 100 || competencyWeight < 0) {
            throw new RuntimeException("CompetencyWeight is invalid: " + competencyWeight);
        }
        for (Map.Entry<Integer, Integer> weightColor : weightToColor.entrySet()) {
            if (competencyWeight >= weightColor.getKey()) {
                return weightColor.getValue();
            }
        }
        return weightToColor.get(100);
    }
}
