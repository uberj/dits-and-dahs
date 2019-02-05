package com.example.uberj.test1.keyboards;

public class KeyConfig {
    public final String textName;
    public final float weight;
    public final boolean isPlayable;
    public final KeyType type;

    public enum KeyType {
        LETTER,
        PROSIGN,
        HALF_SPACE
    }

    public KeyConfig(String textName, float weight, boolean isPlayable, KeyType type) {
        this.textName = textName;
        this.weight = weight;
        this.isPlayable = isPlayable;
        this.type = type;
    }

    /* normal key */
    public static KeyConfig l(String s) {
        return new KeyConfig(s, 1, true, KeyType.LETTER);
    }

    public static KeyConfig p(String s) {
        return new KeyConfig(s, 1, true, KeyType.PROSIGN);
    }

    /* Half space key */
    public static KeyConfig s() {
        return new KeyConfig(null, 0.5f, false, KeyType.HALF_SPACE);
    }
}
