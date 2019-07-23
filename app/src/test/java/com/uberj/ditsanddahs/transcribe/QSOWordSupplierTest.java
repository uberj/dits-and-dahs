package com.uberj.ditsanddahs.transcribe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.GlobalSettings;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class QSOWordSupplierTest {
    private AudioManager.MorseConfig morseConfig0 = mock(AudioManager.MorseConfig.class);
    private AudioManager.MorseConfig morseConfig1 = mock(AudioManager.MorseConfig.class);
    private GlobalSettings globalSettings;

    private GlobalSettings withProsignSettings(Set<String> enabledProsigns) {
        return new GlobalSettings(0, true, 3, 7, enabledProsigns, enableHapticFeedback);
    }

    private GlobalSettings noProsignSettings() {
        return new GlobalSettings(0, false, 3, 7, Sets.newConcurrentHashSet(), enableHapticFeedback);
    }

    @Test
    public void getNoProSignCollapse() {
        globalSettings = noProsignSettings();
        ArrayList<String> passedMessages = Lists.newArrayList("CQ CQ CQ KI7GBN", "KI7GBN DE FOOBAR", "ABC DEF G");
        QSOWordSupplier qsoWordSupplier = new QSOWordSupplier(passedMessages, globalSettings, morseConfig0, morseConfig1);
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("K", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("I", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("N", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("K", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("I", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("N", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("D", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("E", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("F", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("O", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("O", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("A", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("R", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("A", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("D", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("E", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("F", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }
    @Test
    public void getWithProSignCollapseTestStationSwitch() {
        globalSettings = withProsignSettings(Sets.newHashSet());
        ArrayList<String> passedMessages = Lists.newArrayList("A B", "C", "D");
        QSOWordSupplier qsoWordSupplier = new QSOWordSupplier(passedMessages, globalSettings, morseConfig0, morseConfig1);
        Assert.assertEquals("A", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("D", qsoWordSupplier.get().getKey());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }

    @Test
    public void getWithProSignCollapse() {
        globalSettings = withProsignSettings(Sets.newHashSet("QRM", "QTH", "CQ"));
        ArrayList<String> passedMessages = Lists.newArrayList("CQ CQ CQ KI7GBN", "KI7GBN DE FOOBAR", "ABC DEF G", "QR QRM QTH TH");
        QSOWordSupplier qsoWordSupplier = new QSOWordSupplier(passedMessages, globalSettings, morseConfig0, morseConfig1);
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("K", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("I", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("N", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("K", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("I", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("N", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("D", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("E", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("F", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("O", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("O", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("A", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("R", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("A", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("B", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("C", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("D", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("E", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("F", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("G", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("R", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals("R", qsoWordSupplier.get().getKey());
        Assert.assertEquals("M", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("Q", qsoWordSupplier.get().getKey());
        Assert.assertEquals("T", qsoWordSupplier.get().getKey());
        Assert.assertEquals("H", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("T", qsoWordSupplier.get().getKey());
        Assert.assertEquals("_", qsoWordSupplier.get().getKey());
        Assert.assertEquals("H", qsoWordSupplier.get().getKey());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }
}