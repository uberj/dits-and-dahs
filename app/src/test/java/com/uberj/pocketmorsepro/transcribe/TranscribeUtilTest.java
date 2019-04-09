package com.uberj.pocketmorsepro.transcribe;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TranscribeUtilTest {
    @Test
    public void trailingSpacesTest() {
        ArrayList<String> list1 = Lists.newArrayList("A", "B", "C", "SPC", "SPC");
        List<String> list2 = TranscribeUtil.stripTrailingSpaces(list1);
        Assert.assertEquals(5, list1.size());
        Assert.assertEquals(3, list2.size());
        Assert.assertFalse(list2.contains("SPC"));
    }
}
