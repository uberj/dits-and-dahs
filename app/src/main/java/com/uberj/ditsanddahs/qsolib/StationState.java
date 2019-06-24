package com.uberj.ditsanddahs.qsolib;

public class StationState {
    public final StuffSaid stuffSaid;
    public final String callSign;

    public StationState(String callSign, StuffSaid stuffSaid) {
        this.stuffSaid = stuffSaid;
        this.callSign = callSign;
    }
}
