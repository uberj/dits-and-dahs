package com.uberj.ditsanddahs.transcribe;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class QSOWordSupplierTest {

    @Test
    public void get() {
        ArrayList<String> passedMessages = Lists.newArrayList("cq cq cq ki7gbn", "ki7gbn de foobar", "abc def g");
        QSOWordSupplier qsoWordSupplier = new QSOWordSupplier(passedMessages);
        Assert.assertEquals("c", qsoWordSupplier.get());
        Assert.assertEquals("q", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("c", qsoWordSupplier.get());
        Assert.assertEquals("q", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("c", qsoWordSupplier.get());
        Assert.assertEquals("q", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("k", qsoWordSupplier.get());
        Assert.assertEquals("i", qsoWordSupplier.get());
        Assert.assertEquals("7", qsoWordSupplier.get());
        Assert.assertEquals("g", qsoWordSupplier.get());
        Assert.assertEquals("b", qsoWordSupplier.get());
        Assert.assertEquals("n", qsoWordSupplier.get());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get());
        Assert.assertEquals("k", qsoWordSupplier.get());
        Assert.assertEquals("i", qsoWordSupplier.get());
        Assert.assertEquals("7", qsoWordSupplier.get());
        Assert.assertEquals("g", qsoWordSupplier.get());
        Assert.assertEquals("b", qsoWordSupplier.get());
        Assert.assertEquals("n", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("d", qsoWordSupplier.get());
        Assert.assertEquals("e", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("f", qsoWordSupplier.get());
        Assert.assertEquals("o", qsoWordSupplier.get());
        Assert.assertEquals("o", qsoWordSupplier.get());
        Assert.assertEquals("b", qsoWordSupplier.get());
        Assert.assertEquals("a", qsoWordSupplier.get());
        Assert.assertEquals("r", qsoWordSupplier.get());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get());
        Assert.assertEquals("a", qsoWordSupplier.get());
        Assert.assertEquals("b", qsoWordSupplier.get());
        Assert.assertEquals("c", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("d", qsoWordSupplier.get());
        Assert.assertEquals("e", qsoWordSupplier.get());
        Assert.assertEquals("f", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("g", qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }
}