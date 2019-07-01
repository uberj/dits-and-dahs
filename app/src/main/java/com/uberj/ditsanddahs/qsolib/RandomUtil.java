package com.uberj.ditsanddahs.qsolib;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

public class RandomUtil {
    private static final Random r = new Random();
    public static <T> T choose(List<T> possibles) {
        return possibles.get(r.nextInt(possibles.size()));
    }

    public static <T> T choose(T ...possibles) {
        return choose(Lists.newArrayList(possibles));
    }

    public static boolean randomGuard(double chance) {
        return r.nextInt(100) <= chance * 100;
    }

    public static int intBetween(int start, int end) {
        return start + r.nextInt(end - start);
    }
}
