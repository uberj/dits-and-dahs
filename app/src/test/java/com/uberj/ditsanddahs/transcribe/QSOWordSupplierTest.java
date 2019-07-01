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
        Assert.assertEquals("cq", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("cq", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("cq", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("ki7gbn", qsoWordSupplier.get());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get());
        Assert.assertEquals("ki7gbn", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("de", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("foobar", qsoWordSupplier.get());
        Assert.assertEquals(QSOWordSupplier.STATION_SWITCH_MARKER, qsoWordSupplier.get());
        Assert.assertEquals("abc", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("def", qsoWordSupplier.get());
        Assert.assertEquals(" ", qsoWordSupplier.get());
        Assert.assertEquals("g", qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
        Assert.assertNull(qsoWordSupplier.get());
    }
}