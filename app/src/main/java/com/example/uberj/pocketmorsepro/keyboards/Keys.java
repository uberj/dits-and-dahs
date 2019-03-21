package com.example.uberj.pocketmorsepro.keyboards;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public interface Keys {

    ImmutableList<ImmutableList<KeyConfig>> getKeys();

    default List<String> allPlayableKeysNames() {
        List<String> inPlayLetters = Lists.newArrayList();
        for (ImmutableList<KeyConfig> row : getKeys()) {
            for (KeyConfig keyConfig : row) {
                if (keyConfig.isPlayable) {
                    inPlayLetters.add(keyConfig.textName);
                }
            }
        }
        return inPlayLetters;
    }
}
