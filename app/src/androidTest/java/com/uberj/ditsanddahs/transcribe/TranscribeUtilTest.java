package com.uberj.ditsanddahs.transcribe;


import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;
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
        session.playedMessage = Lists.newArrayList("M", " ", "K", "K");
        session.enteredKeys = Lists.newArrayList("M", " ", "K", "M");
        session.stringsRequested = session.playedMessage;
        Map<String, Pair<Integer, Integer>> errorMap = TranscribeUtil.calculateHitMap(session);
        Assert.assertEquals(3, errorMap.size());

        {
            Assert.assertNotNull(errorMap.get("M"));
            int hits = errorMap.get("M").getLeft();
            int opportunities = errorMap.get("M").getRight();
            Assert.assertEquals(1, hits);
            Assert.assertEquals(1, opportunities);
        }

        {
            Assert.assertNotNull(errorMap.get("K"));
            int hits = errorMap.get("K").getLeft();
            int opportunities = errorMap.get("K").getRight();
            Assert.assertEquals(1, hits);
            Assert.assertEquals(2, opportunities);
        }

        {
            Assert.assertNotNull(errorMap.get(" "));
            int hits = errorMap.get(" ").getLeft();
            int opportunities = errorMap.get(" ").getRight();
            Assert.assertEquals(1, hits);
            Assert.assertEquals(1, opportunities);
        }
    }
}