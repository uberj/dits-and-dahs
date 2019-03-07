package com.example.uberj.test1.keyboards;

import static com.example.uberj.test1.keyboards.KeyConfig.KeyType.DELETE_KEY;
import static com.example.uberj.test1.keyboards.KeyConfig.KeyType.SPACE_KEY;

public class KeyConfig {
    public final String textName;
    public final float weight;
    public final boolean isPlayable;
    public final KeyType type;

    public enum KeyType {
        LETTER,
        PROSIGN,
        HALF_SPACE,
        SPACE_KEY,
        DELETE_KEY
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
    public static KeyConfig s(float weight) {
        return new KeyConfig(null, weight, false, KeyType.HALF_SPACE);
    }

    public static KeyConfig ctrl(ControlType ctrlType) {
        return new KeyConfig(ctrlType.keyName, ctrlType.weight, false, ctrlType.keyType);
    }

    public enum ControlType {
        SPACE("SPC", 4, SPACE_KEY),
        DELETE("DEL", 2, DELETE_KEY);

        public final int weight;
        public final String keyName;
        private final KeyType keyType;

        ControlType(String keyName, int weight, KeyType keyType) {
            this.keyName = keyName;
            this.weight = weight;
            this.keyType = keyType;
        }
    }
}
