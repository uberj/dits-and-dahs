package com.uberj.ditsanddahs;

import org.junit.Assert;
import org.junit.Test;

public class AudioManagerTest {
    @Test
    public void testSymbolConvert() {
        String asdf = AudioManager.explodeToSymbols("asdf");
        Assert.assertEquals(".-/.../-../..-.", asdf);

        String asdfasdf = AudioManager.explodeToSymbols("asdf asdf");
        Assert.assertEquals(".-/.../-../..-. .-/.../-../..-.", asdfasdf);
    }
}