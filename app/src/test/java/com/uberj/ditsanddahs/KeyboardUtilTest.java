package com.uberj.ditsanddahs;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class KeyboardUtilTest {
    @Test
    public void trailingSpacesTest() {
        ArrayList<String> list1 = Lists.newArrayList("A", "B", "C", "SPC", "SPC");
        List<String> list2 = KeyboardUtil.stripTrailingSpaces(list1);
        Assert.assertEquals(5, list1.size());
        Assert.assertEquals(3, list2.size());
        Assert.assertFalse(list2.contains("SPC"));
    }

    @Test
    public void delCursorZero() {
        Pair<Integer, String> convert = KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:0:0" // E
        ));
        Assert.assertEquals(0, convert.getKey().intValue());
        Assert.assertEquals("ES", convert.getValue());
    }

    @Test
    public void convertKeyPressesToStringCursorMove() {
        Assert.assertEquals(0, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList()).getKey().intValue());
        Assert.assertEquals(1, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0" // E
        )).getKey().intValue());
        Assert.assertEquals(2, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1" // ES
        )).getKey().intValue());
        Assert.assertEquals(1, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2" // E
        )).getKey().intValue());
        Assert.assertEquals(2, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2", // E
                "v1:S:1:1" // ES
        )).getKey().intValue());
        Assert.assertEquals(3, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2", // E
                "v1:S:1:1", // ES
                "v1:S:2:2" // ESS
        )).getKey().intValue());
        Assert.assertEquals(0, KeyboardUtil.convertKeyPressesToString(Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2", // E
                "v1:S:1:1", // ES
                "v1:S:2:2", // ESS
                "v1:DEL:1:1"  // SS
        )).getKey().intValue());
    }

    @Test
    public void convertKeyPressesToStringWithSpace() {
        ArrayList<String> strings = Lists.newArrayList(
                "v1:A:0:0",
                "v1:S:1:1",
                "v1:D:2:2",
                "v1:F:3:3",
                "v1:SPC:4:4",
                "v1:H:5:5",
                "v1:J:6:6",
                "v1:K:7:7",
                "v1:L:8:8",
                "v1:SPC:9:9",
                "v1:Q:10:10",
                "v1:W:11:11",
                "v1:E:12:12",
                "v1:R:13:13",
                "v1:T:14:14",
                "v1:Y:15:15");
        String s = KeyboardUtil.convertKeyPressesToString(strings).getValue();
        Assert.assertEquals("ASDF HJKL QWERTY", s);
    }

    @Test
    public void convertKeyPressesToStringPointDelete() {
        ArrayList<String> strings = Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2", // E
                "v1:S:1:1", // ES
                "v1:E:2:2", // ESE
                "v1:DEL:3:3", // ES
                "v1:S:2:2", // ESS
                "v1:S:3:3", // ESSS
                "v1:S:4:4", // ESSSS
                "v1:E:5:5", // ESSSSE
                "v1:S:6:6", // ESSSSES
                "v1:S:7:7", // ESSSSESS
                "v1:DEL:6:6");  // ESSSSSS
        String s = KeyboardUtil.convertKeyPressesToString(strings).getValue();
        Assert.assertEquals("ESSSSSS", s);
    }

    @Test
    public void convertKeyPressesToStringRangeDelete() {
        ArrayList<String> strings = Lists.newArrayList(
                "v1:E:0:0", // E
                "v1:S:1:1", // ES
                "v1:DEL:2:2", // E
                "v1:S:1:1", // ES
                "v1:E:2:2", // ESE
                "v1:DEL:3:3", // ES
                "v1:S:2:2", // ESS
                "v1:S:3:3", // ESSS
                "v1:S:4:4", // ESSSS
                "v1:E:5:5", // ESSSSE
                "v1:S:6:6", // ESSSSES
                "v1:S:7:7", // ESSSSESS
                "v1:DEL:6:6");  // ESSSSSS
        Pair<Integer, String> convert = KeyboardUtil.convertKeyPressesToString(strings);
        Integer pos = convert.getKey();
        Assert.assertEquals(5, pos.intValue());
        Assert.assertEquals("ESSSSSS", convert.getValue());
    }

    @Test
    public void convertKeyPressesToStringRangeDeleteBetweenSpaces() {
        ArrayList<String> strings = Lists.newArrayList(
                "v1:A:0:0",
                "v1:S:1:1",
                "v1:D:2:2",
                "v1:F:3:3",
                "v1:SPC:4:4",
                "v1:H:5:5",
                "v1:J:6:6",
                "v1:K:7:7",
                "v1:L:8:8",
                "v1:SPC:9:9",
                "v1:Q:10:10",
                "v1:W:11:11",
                "v1:E:12:12",
                "v1:R:13:13",
                "v1:T:14:14",
                "v1:Y:15:15",
                "v1:DEL:5:9"
        );

        Pair<Integer, String> convert = KeyboardUtil.convertKeyPressesToString(strings);
        String s = convert.getValue();
        Integer pos = convert.getKey();

        // ASDF HJKL QWERTY
        Assert.assertEquals("ASDF  QWERTY", s);
        Assert.assertEquals(5, pos.intValue());
    }

}