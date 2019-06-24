package com.uberj.ditsanddahs.qsolib.data;

import com.google.common.collect.ImmutableList;

import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;

public class RandomEquipmentGenerator {
    private static final ImmutableList<String> power = ImmutableList.of(
            "5",
            "10",
            "20",
            "50",
            "100",
            "200",
            "300",
            "500",
            "800",
            "900",
            "1000",
            "1500"
    );
    private static final ImmutableList<String> antennas = ImmutableList.of(
            "BOW TIE",
            "DIPOLE ARRAY",
            "DIPOLE",
            "MONOPOLE",
            "LOOP",
            "HELICAL",
            "YAGI",
            "INVERTED V"
    );

    private static final ImmutableList.Builder<Rig> rigsBuilder = ImmutableList.builder();

    public static class Rig {
        public final String manufacturer;
        public final String model;

        public Rig(String manufacturer, String model) {
            this.manufacturer = manufacturer.toUpperCase();
            this.model = model.toUpperCase();
        }
    }

    static {
        rigsBuilder.add(new Rig("Alinco", "DX77T"));
        rigsBuilder.add(new Rig("Alinco", "DXSR8T"));
        rigsBuilder.add(new Rig("Alinco", "DXSR9T"));
        rigsBuilder.add(new Rig("DZ", "Sienna"));
        rigsBuilder.add(new Rig("Elecraft", "K2"));
        rigsBuilder.add(new Rig("Elecraft", "K3"));
        rigsBuilder.add(new Rig("Elecraft", "KX2"));
        rigsBuilder.add(new Rig("Elecraft", "KX3"));
        rigsBuilder.add(new Rig("Flex", "1500"));
        rigsBuilder.add(new Rig("Flex", "3000"));
        rigsBuilder.add(new Rig("Flex", "5000"));
        rigsBuilder.add(new Rig("Flex", "6000"));
        rigsBuilder.add(new Rig("ICOM", "IC2730"));
        rigsBuilder.add(new Rig("ICOM", "IC7000"));
        rigsBuilder.add(new Rig("ICOM", "IC703"));
        rigsBuilder.add(new Rig("ICOM", "IC706"));
        rigsBuilder.add(new Rig("ICOM", "IC706MkII"));
        rigsBuilder.add(new Rig("ICOM", "IC706MkIIG"));
        rigsBuilder.add(new Rig("ICOM", "IC707"));
        rigsBuilder.add(new Rig("ICOM", "IC7100"));
        rigsBuilder.add(new Rig("ICOM", "IC718"));
        rigsBuilder.add(new Rig("ICOM", "IC7200"));
        rigsBuilder.add(new Rig("ICOM", "IC725"));
        rigsBuilder.add(new Rig("ICOM", "IC726"));
        rigsBuilder.add(new Rig("ICOM", "IC728"));
        rigsBuilder.add(new Rig("ICOM", "IC729"));
        rigsBuilder.add(new Rig("ICOM", "IC7300"));
        rigsBuilder.add(new Rig("ICOM", "IC735"));
        rigsBuilder.add(new Rig("ICOM", "IC736"));
        rigsBuilder.add(new Rig("ICOM", "IC737"));
        rigsBuilder.add(new Rig("ICOM", "IC738"));
        rigsBuilder.add(new Rig("ICOM", "IC7400"));
        rigsBuilder.add(new Rig("ICOM", "IC7410"));
        rigsBuilder.add(new Rig("ICOM", "IC746"));
        rigsBuilder.add(new Rig("ICOM", "IC746Pro"));
        rigsBuilder.add(new Rig("ICOM", "IC751A"));
        rigsBuilder.add(new Rig("ICOM", "IC756Pro"));
        rigsBuilder.add(new Rig("ICOM", "IC756ProII"));
        rigsBuilder.add(new Rig("ICOM", "IC756ProIII"));
        rigsBuilder.add(new Rig("ICOM", "IC7600"));
        rigsBuilder.add(new Rig("ICOM", "IC7600V2"));
        rigsBuilder.add(new Rig("ICOM", "IC7610"));
        rigsBuilder.add(new Rig("ICOM", "IC761"));
        rigsBuilder.add(new Rig("ICOM", "IC765"));
        rigsBuilder.add(new Rig("ICOM", "IC7700"));
        rigsBuilder.add(new Rig("ICOM", "IC775DSP"));
        rigsBuilder.add(new Rig("ICOM", "IC7800"));
        rigsBuilder.add(new Rig("ICOM", "IC781"));
        rigsBuilder.add(new Rig("ICOM", "IC7850"));
        rigsBuilder.add(new Rig("ICOM", "IC7851"));
        rigsBuilder.add(new Rig("ICOM", "IC821H"));
        rigsBuilder.add(new Rig("ICOM", "IC9100"));
        rigsBuilder.add(new Rig("ICOM", "IC910H"));
        rigsBuilder.add(new Rig("ICOM", "IC970H"));
        rigsBuilder.add(new Rig("ICOM", "ICR10"));
        rigsBuilder.add(new Rig("ICOM", "ICR20"));
        rigsBuilder.add(new Rig("ICOM", "ICR7000"));
        rigsBuilder.add(new Rig("ICOM", "ICR75"));
        rigsBuilder.add(new Rig("ICOM", "ICR8500"));
        rigsBuilder.add(new Rig("ICOM", "ICR9000"));
        rigsBuilder.add(new Rig("ICOM", "ICR9500"));
        rigsBuilder.add(new Rig("ICOM", "ID51 AE"));
        rigsBuilder.add(new Rig("ICOM", "ID5100"));
        rigsBuilder.add(new Rig("ICOM", "PCR1000"));
        rigsBuilder.add(new Rig("JRC", "NRD-535"));
        rigsBuilder.add(new Rig("KENWOOD", "R-5000"));
        rigsBuilder.add(new Rig("KENWOOD", "TS140S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS2000"));
        rigsBuilder.add(new Rig("KENWOOD", "TS440S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS450S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS480"));
        rigsBuilder.add(new Rig("KENWOOD", "TS50S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS570"));
        rigsBuilder.add(new Rig("KENWOOD", "TS590"));
        rigsBuilder.add(new Rig("KENWOOD", "TS590SG"));
        rigsBuilder.add(new Rig("KENWOOD", "TS60S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS680S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS690S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS790"));
        rigsBuilder.add(new Rig("KENWOOD", "TS850"));
        rigsBuilder.add(new Rig("KENWOOD", "TS870"));
        rigsBuilder.add(new Rig("KENWOOD", "TS940S"));
        rigsBuilder.add(new Rig("KENWOOD", "TS950"));
        rigsBuilder.add(new Rig("KENWOOD", "TS990"));
        rigsBuilder.add(new Rig("KENWOOD", "TSB2000"));
        rigsBuilder.add(new Rig("YAESU", "FT100"));
        rigsBuilder.add(new Rig("YAESU", "FT1000D"));
        rigsBuilder.add(new Rig("YAESU", "FT1000MP Mk V"));
        rigsBuilder.add(new Rig("YAESU", "FT2000"));
        rigsBuilder.add(new Rig("YAESU", "FT450"));
        rigsBuilder.add(new Rig("YAESU", "FT600"));
        rigsBuilder.add(new Rig("YAESU", "FT767 GX II"));
        rigsBuilder.add(new Rig("YAESU", "FT817"));
        rigsBuilder.add(new Rig("YAESU", "FT817D"));
        rigsBuilder.add(new Rig("YAESU", "FT840"));
        rigsBuilder.add(new Rig("YAESU", "FT847"));
        rigsBuilder.add(new Rig("YAESU", "FT857"));
        rigsBuilder.add(new Rig("YAESU", "FT857D"));
        rigsBuilder.add(new Rig("YAESU", "FT890"));
        rigsBuilder.add(new Rig("YAESU", "FT891"));
        rigsBuilder.add(new Rig("YAESU", "FT897"));
        rigsBuilder.add(new Rig("YAESU", "FT897D"));
        rigsBuilder.add(new Rig("YAESU", "FT900"));
        rigsBuilder.add(new Rig("YAESU", "FT920"));
        rigsBuilder.add(new Rig("YAESU", "FT950"));
        rigsBuilder.add(new Rig("YAESU", "FT980"));
        rigsBuilder.add(new Rig("YAESU", "FT990"));
        rigsBuilder.add(new Rig("YAESU", "FT991"));
        rigsBuilder.add(new Rig("YAESU", "FT991A"));
        rigsBuilder.add(new Rig("YAESU", "FTDX1200"));
        rigsBuilder.add(new Rig("YAESU", "FTDX3000"));
        rigsBuilder.add(new Rig("YAESU", "FTDX5000"));
        rigsBuilder.add(new Rig("YAESU", "FTDX9000"));
    }

    private static final ImmutableList<Rig> rigs = rigsBuilder.build();

    public static class Equipment {
        public final String watts;
        public final Rig radio;
        public final String antenna;

        public Equipment(String watts, Rig radio, String antenna) {
            this.watts = watts;
            this.radio = radio;
            this.antenna = antenna.toUpperCase();
        }
    }

    public static Equipment getEquipment() {
        return new Equipment(choose(power), choose(rigs), choose(antennas));
    }
}
