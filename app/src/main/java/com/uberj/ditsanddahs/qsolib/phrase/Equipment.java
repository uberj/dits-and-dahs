package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.collect.ImmutableMap;
import com.uberj.ditsanddahs.qsolib.data.RandomEquipmentGenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;

public class Equipment implements Phrase {
    private static final Random r = new Random();
    private final RandomEquipmentGenerator.Equipment equipment;

    public Equipment() {
        equipment = RandomEquipmentGenerator.getEquipment();
    }

    private Map<String, String> resolveFacts() {
        return ImmutableMap.of(
                "antenna", equipment.antenna,
                "rigModel", equipment.radio.model,
                "watts", equipment.watts
        );
    }


    @Override
    public List<Phrase> reduce(Location location) {
        return of(new LeafPhrase(choose(
                "ANT IS ${antenna} RIG IS ${rigModel} PWR ${watts} WATTS",
                "ANT IS ${antenna} ES RIG IS ${rigModel} PWR ${watts} W",

                "RIG IS ${rigModel} PWR ${watts} WATTS ANT IS ${antenna}",
                "RIG IS ${rigModel} PWR ${watts} W ES ANT IS ${antenna}",
                "RIG IS ${rigModel} PWR ${watts}W ES ANT IS ${antenna}"
        ), this::resolveFacts));
    }
}
