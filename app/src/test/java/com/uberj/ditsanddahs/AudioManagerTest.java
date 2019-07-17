package com.uberj.ditsanddahs;

import org.junit.Assert;
import org.junit.Test;

public class AudioManagerTest {
    @Test
    public void testSymbolConvert() {
        String asdf = AudioManager.explodeToSymbols("asdf");
        Assert.assertEquals(".-_..._-.._..-.", asdf);

        String asdfasdf = AudioManager.explodeToSymbols("asdf asdf");
        Assert.assertEquals(".-_..._-.._..-. .-_..._-.._..-.", asdfasdf);
    }
}