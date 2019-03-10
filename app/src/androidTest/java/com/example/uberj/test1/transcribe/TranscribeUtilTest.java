package com.example.uberj.test1.transcribe;


import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;
import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class TranscribeUtilTest {
    @Test
    public void testMissedLetterPercentages() {
        TranscribeTrainingSession session = new TranscribeTrainingSession();
        session.playedKeys = Lists.newArrayList("M", " ", "K", "K");
        session.enteredKeys = Lists.newArrayList("M", " ", "K", "M");
        Map<String, Double> errorMap = TranscribeUtil.calculateErrorMap(session);
        Assert.assertEquals(3, errorMap.size());

        Assert.assertNotNull(errorMap.get("M"));
        Assert.assertEquals(0.50D, errorMap.get("M"),  0);

        Assert.assertNotNull(errorMap.get("K"));
        Assert.assertEquals(0.50D, errorMap.get("K"),  0);

        Assert.assertNotNull(errorMap.get(" "));
        Assert.assertEquals(0D, errorMap.get(" "), 0);
    }
}