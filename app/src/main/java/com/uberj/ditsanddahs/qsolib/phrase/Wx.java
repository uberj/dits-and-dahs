package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.collect.ImmutableMap;
import com.uberj.ditsanddahs.qsolib.data.RandomWeatherGenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;

public class Wx implements Phrase {
    private static final Random r = new Random();
    private final RandomWeatherGenerator.Weather weather;

    public Wx() {
        weather = RandomWeatherGenerator.getWeather();
    }

    private Map<String, String> resolveFacts() {
        int tempTemp = weather.tempFLow + r.nextInt(weather.tempFHigh - weather.tempFLow);

        int temp;
        String tempUnit;

        if (r.nextInt() % 2 == 0) {
            temp = (int) (tempTemp - 32 * 0.5556F);
            tempUnit = "C";
        } else {
            temp = tempTemp;
            tempUnit = "F";
        }
        return ImmutableMap.of(
                "temp", String.valueOf(temp),
                "tempUnit", tempUnit,
                "condition", weather.adjective
        );
    }


    @Override
    public List<Phrase> reduce(Location location) {
        return of(new LeafPhrase(choose(
                "WX ${condition} ABT ${temp} ${tempUnit}",
                "WX IS ${condition} ABT ${temp} ${tempUnit}",
                "WX IS ${condition} ES ${temp} ${tempUnit}",
                "WX ${condition} ES IS ${temp} ${tempUnit}",
                "HERE WEATHER IS ${condition}",
                "WEATHER IS ${condition} ES ${temp} ${tempUnit}",
                "THE WEATHER HERE IS ${condition} ES ${temp} ${tempUnit}",
                "WX HR ${condition} ${temp} ${tempUnit}",
                "WX HR ${condition} IS ${temp} ${tempUnit}",
                "WX HR IS ${temp} ${tempUnit} ${condition}",
                "WX IS ${temp} ${tempUnit} ES ${condition}",
                "HERE WX IS ${temp} ${tempUnit} ES ${condition}"
        ), this::resolveFacts));
    }
}
