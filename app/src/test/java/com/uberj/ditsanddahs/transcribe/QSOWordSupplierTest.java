package com.uberj.ditsanddahs.transcribe;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class QSOWordSupplierTest {

    @Test
    public void get() {
        ArrayList<String> passedMessages = Lists.newArrayList("cq cq cq ki7gbn", "ki7gbn de foobar", "abc def g");
        QSOWordSupplier qsoWordSupplier = new QSOWordSupplier(passedMessages, null, null);
        Assert.assertEquals("c", qsoWordSupplier.get().getKey());
        Assert.assertEquals("q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("c", qsoWordSupplier.get().getKey());
        Assert.assertEquals("q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("c", qsoWordSupplier.get().getKey());
        Assert.assertEquals("q", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("k", qsoWordSupplier.get().getKey());
        Assert.assertEquals("i", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("g", qsoWordSupplier.get().getKey());
        Assert.assertEquals("b", qsoWordSupplier.get().getKey());
        Assert.assertEquals("n", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("k", qsoWordSupplier.get().getKey());
        Assert.assertEquals("i", qsoWordSupplier.get().getKey());
        Assert.assertEquals("7", qsoWordSupplier.get().getKey());
        Assert.assertEquals("g", qsoWordSupplier.get().getKey());
        Assert.assertEquals("b", qsoWordSupplier.get().getKey());
        Assert.assertEquals("n", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("d", qsoWordSupplier.get().getKey());
        Assert.assertEquals("e", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("f", qsoWordSupplier.get().getKey());
        Assert.assertEquals("o", qsoWordSupplier.get().getKey());
        Assert.assertEquals("o", qsoWordSupplier.get().getKey());
        Assert.assertEquals("b", qsoWordSupplier.get().getKey());
        Assert.assertEquals("a", qsoWordSupplier.get().getKey());
        Assert.assertEquals("r", qsoWordSupplier.get().getKey());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get().getKey());
        Assert.assertEquals("a", qsoWordSupplier.get().getKey());
        Assert.assertEquals("b", qsoWordSupplier.get().getKey());
        Assert.assertEquals("c", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("d", qsoWordSupplier.get().getKey());
        Assert.assertEquals("e", qsoWordSupplier.get().getKey());
        Assert.assertEquals("f", qsoWordSupplier.get().getKey());
        Assert.assertEquals(" ", qsoWordSupplier.get().getKey());
        Assert.assertEquals("g", qsoWordSupplier.get().getKey());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }
}