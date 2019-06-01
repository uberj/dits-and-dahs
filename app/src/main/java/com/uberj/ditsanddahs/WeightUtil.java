package com.uberj.ditsanddahs;

import android.os.Build;

import java.util.Map;

public class WeightUtil {
    public static void increment(Map<String, Integer> competencyWeights, String currentMessage, int amount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            competencyWeights.compute(currentMessage, (m, v) -> Math.min(100, v + amount));
        } else {
            Integer cw = competencyWeights.get(currentMessage);
            if (cw == null) {
                competencyWeights.put(currentMessage, 10);
            } else {
                competencyWeights.put(currentMessage, Math.min(100, cw + amount));
            }
        }
    }
    public static void decrement(Map<String, Integer> competencyWeights, String currentMessage, int amount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            competencyWeights.compute(currentMessage, (m, v) -> Math.max(0, v - amount));
        } else {
            Integer cw = competencyWeights.get(currentMessage);
            if (cw == null) {
                competencyWeights.put(currentMessage, 0);
            } else {
                competencyWeights.put(currentMessage, Math.max(0, cw - amount));
            }
        }
    }
}
