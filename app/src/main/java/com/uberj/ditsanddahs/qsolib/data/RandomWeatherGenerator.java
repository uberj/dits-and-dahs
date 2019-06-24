package com.uberj.ditsanddahs.qsolib.data;

import com.google.common.collect.ImmutableList;

import java.util.Random;

import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;

public class RandomWeatherGenerator {
    private static final Random r = new Random();
    private static final ImmutableList.Builder<Weather> weatherConditionsBuilder = ImmutableList.builder();
    static {
        weatherConditionsBuilder.add(new Weather(65,85,"good"));
        weatherConditionsBuilder.add(new Weather(75,90,"great"));
        weatherConditionsBuilder.add(new Weather(65,85,"nice"));
        weatherConditionsBuilder.add(new Weather(50,68,"fine"));
        weatherConditionsBuilder.add(new Weather(70,85,"lovely"));
        weatherConditionsBuilder.add(new Weather(75,85,"beautiful"));
        weatherConditionsBuilder.add(new Weather(75,85,"wonderful"));
        weatherConditionsBuilder.add(new Weather(75,85,"excellent"));
        weatherConditionsBuilder.add(new Weather(75,85,"gorgeous"));
        weatherConditionsBuilder.add(new Weather(70,79,"fair"));
        weatherConditionsBuilder.add(new Weather(50,66,"mild"));
        weatherConditionsBuilder.add(new Weather(66,80,"pleasant"));
        weatherConditionsBuilder.add(new Weather(35,45,"bad"));
        weatherConditionsBuilder.add(new Weather(-30,10,"awful"));
        weatherConditionsBuilder.add(new Weather(-40,0,"terrible"));
        weatherConditionsBuilder.add(new Weather(35,40,"nasty"));
        weatherConditionsBuilder.add(new Weather(40,50,"lousy"));
        weatherConditionsBuilder.add(new Weather(0,45,"foul"));
        weatherConditionsBuilder.add(new Weather(0,45,"rotten"));
        weatherConditionsBuilder.add(new Weather(0,45,"miserable"));
        weatherConditionsBuilder.add(new Weather(50,65,"dull"));
        weatherConditionsBuilder.add(new Weather(50,65,"gloomy"));
        weatherConditionsBuilder.add(new Weather(40,59,"ugly"));
        weatherConditionsBuilder.add(new Weather(50,60,"sunny"));
        weatherConditionsBuilder.add(new Weather(78,80,"warm"));
        weatherConditionsBuilder.add(new Weather(85,103,"hot"));
        weatherConditionsBuilder.add(new Weather(70,80,"mild"));
        weatherConditionsBuilder.add(new Weather(40,50,"cool"));
        weatherConditionsBuilder.add(new Weather(10,30,"chilly"));
        weatherConditionsBuilder.add(new Weather(10,30,"cold"));
        weatherConditionsBuilder.add(new Weather(28,32,"freezing"));
        weatherConditionsBuilder.add(new Weather(31,34,"icy"));
        weatherConditionsBuilder.add(new Weather(30,36,"frosty"));
        weatherConditionsBuilder.add(new Weather(-30,10,"very cold"));
        weatherConditionsBuilder.add(new Weather(-30,10,"bitter cold"));
        weatherConditionsBuilder.add(new Weather(38,65,"rainy"));
        weatherConditionsBuilder.add(new Weather(38,65,"wet"));
        weatherConditionsBuilder.add(new Weather(65,78,"humid"));
        weatherConditionsBuilder.add(new Weather(50,99,"dry"));
        weatherConditionsBuilder.add(new Weather(10,30,"frigid"));
        weatherConditionsBuilder.add(new Weather(40,50,"foggy"));
        weatherConditionsBuilder.add(new Weather(50,60,"windy"));
        weatherConditionsBuilder.add(new Weather(50,65,"stormy"));
        weatherConditionsBuilder.add(new Weather(50,65,"breezy"));
        weatherConditionsBuilder.add(new Weather(80,100,"windless"));
        weatherConditionsBuilder.add(new Weather(60,80,"calm"));
        weatherConditionsBuilder.add(new Weather(55,70,"cloudy"));
        weatherConditionsBuilder.add(new Weather(55,70,"overcast"));
        weatherConditionsBuilder.add(new Weather(70,80,"cloudless"));
        weatherConditionsBuilder.add(new Weather(40,70,"clear"));
        weatherConditionsBuilder.add(new Weather(40,65,"gray "));
        weatherConditionsBuilder.add(new Weather(40,65,"grey"));
        weatherConditionsBuilder.add(new Weather(70,89,"warm es sunny"));
        weatherConditionsBuilder.add(new Weather(70,89,"hot es sunny"));
        weatherConditionsBuilder.add(new Weather(50,70,"heavy rain"));
        weatherConditionsBuilder.add(new Weather(40,65,"pouring rain"));
        weatherConditionsBuilder.add(new Weather(40,65,"steady rain"));
        weatherConditionsBuilder.add(new Weather(40,65,"constant rain"));
        weatherConditionsBuilder.add(new Weather(33,40,"cold rain"));
        weatherConditionsBuilder.add(new Weather(60,75,"warm rain"));
        weatherConditionsBuilder.add(new Weather(50,70,"light rain"));
        weatherConditionsBuilder.add(new Weather(45,50,"gentle rain"));
    }
    private static final ImmutableList<Weather> weatherConditions = weatherConditionsBuilder.build();

    public static class Weather {
        public final int tempFLow;
        public final int tempFHigh;
        public final String adjective;

        public Weather(int tempFLow, int tempFHigh, String adjective) {
            this.tempFLow = tempFLow;
            this.tempFHigh = tempFHigh;
            this.adjective = adjective.toUpperCase();
        }
    }

    public static Weather getWeather() {
        return choose(weatherConditions);
    }
}
