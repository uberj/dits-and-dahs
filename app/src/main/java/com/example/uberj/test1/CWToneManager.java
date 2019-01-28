package com.example.uberj.test1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

public class CWToneManager {
    private static final String TAG = "CWToneManager";
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    // again modified by Jacques Uber <mail@uberj.com>

    private final int wpm;
    private static final int sampleRateHz = 44100;

    private static final ImmutableMap<String, String> LETTER_TONES = ImmutableMap.<String, String>builder()
            .put("A", ".-")
            .put("B", "...-")
            .put("C", ".-.-")
            .put("D", "-..")
            .put("E", ".")
            .put("F", "..-.")
            .put("G", "--.")
            .put("H", "....")
            .put("I", "..")
            .put("J", ".---")
            .put("K", "-.-")
            .put("L", ".-..")
            .put("M", "--")
            .put("N", "-.")
            .put("O", "---")
            .put("P", ".--.")
            .put("Q", "--.-")
            .put("R", ".-.")
            .put("S", "...")
            .put("T", "-")
            .put("U", "..-")
            .put("V", "-...")
            .put("W", ".--")
            .put("X", "-..-")
            .put("Y", "-.--")
            .put("Z", "--..")
            .put("1", ".----")
            .put("2", "..---")
            .put("3", "...--")
            .put("4", "....-")
            .put("5", ".....")
            .put("6", "-....")
            .put("7", "--...")
            .put("8", "---..")
            .put("9", "----.")
            .put("0", "-----")
            .put(" ", " ")
            .put("SK", "...-.-")
            .put("AR", ".-.-.")
            .put("73", "--......--")
            .put("BT", "-...-")
            .build();


    private static int farnsWorthSpace = 3;
    private static int freqOfToneHz = 440;
    private static int silenceSymbolsAfterDitDah = 1;
    private static int farnsworthWordConstant = 1;

    /*
        16 = 1 + 7 + 7 + 1 = .--.
        3  = /
        8  = 1 + 7 = .-
        3  = /
        9  = 1 + 7 + 1 = .-.
        3  = /
        2  = 1 + 1 = ..
        3  = /
    +   3  = 1 + 1 + 1 ...
    -------------------
       50 (symbols in "paris"
     */
    public static byte[] buildSnd(int wpm, String s) {
        // calculate number of symbols
        int totalNumSymbols = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            totalNumSymbols += numSymbols(c);
            if (!symbolIsSilent(c)) {
                totalNumSymbols += silenceSymbolsAfterDitDah;
            }
        }

        float symbolsPerSecond = (wpm * 50f) / 60f;
        float symbolDuration = 1 / symbolsPerSecond;
        int numSamplesPerSymbol = (int) (symbolDuration * sampleRateHz);
        Log.d(TAG, "Entire Duration: " + symbolsPerSecond * totalNumSymbols);

        int totalNumSamples = totalNumSymbols * numSamplesPerSymbol;
        double rawSnd[] = new double[totalNumSamples];

        int sndIdx = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int curNumberSymbols = numSymbols(c);
            int numSamplesForCurSymbol = curNumberSymbols * numSamplesPerSymbol;

            // fill out the array with symbol
            if (symbolIsSilent(c)) {
                for (int j = 0; j < numSamplesForCurSymbol; ++j) {
                    rawSnd[sndIdx++] = 0;
                }
            } else {
                int rampSamples = (int) (numSamplesPerSymbol * 0.10); // 5% ramp up

                for (int j = 0; j < rampSamples; j++) {
                    rawSnd[sndIdx++] = ((float) j/(float) rampSamples) * Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                }
                for (int j = rampSamples; j < numSamplesForCurSymbol - rampSamples; j++) {
                    rawSnd[sndIdx++] = Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                }

                int x = rampSamples;
                for (int j = numSamplesForCurSymbol - rampSamples; j < numSamplesForCurSymbol; j++) {
                    rawSnd[sndIdx++] = ((float) x/(float) rampSamples) * Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                    x--;
                }
            }

            // fill out the array with space if needed
            if (!symbolIsSilent(c)) {
                int silencePadSamples = numSamplesPerSymbol * silenceSymbolsAfterDitDah;
                for (int j = 0; j < silencePadSamples; ++j) {
                    rawSnd[sndIdx++] = 0;
                }
            }
        }

        return pcmConvert(rawSnd);
    }

    private static byte[] pcmConvert(double[] rawSnd) {
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        byte pcmSnd[] = new byte[2 * rawSnd.length];
        int idx = 0;
        for (final double dval : rawSnd) {
            // scale to maximum amplitude
            final short val = (short) ((dval * 32767));
            // in 16 bit wav pcm, first byte is the low order byte
            pcmSnd[idx++] = (byte) (val & 0x00ff);
            pcmSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return pcmSnd;
    }

    private static boolean symbolIsSilent(char c) {
        switch (c) {
            case '-':
            case '.':
                return false;
            case '/':
            case ' ':
                return true;
            default:
                throw new RuntimeException("Unhandled silent check for char case " + c);

        }
    }

    public static float baud(int wpm) {
        return (wpm * 50f) / 60f;
    }

    // ditsPerSecond = (wpm * 50 dits) / 60 seconds
    // numSamplesPerDit = ditsPerSecond * sampleRateHz
    //
    // Notes:
    //  * 3 dits per dash
    //  * 7 dits per space

    public static int numSymbols(String s) {
        int total = 0;
        String ditDahs = LETTER_TONES.get(s);
        if (ditDahs == null) {
            throw new RuntimeException("Unknown letter: " + s);
        }

        for (char c : ditDahs.toCharArray()) {
            total+= numSymbols(c);
        }
        return total;
    }

    public static int numSymbols(char c) {
        switch (c) {
            case '-':
                return 3;
            case '.':
                return 1;
            case '/':
                return 3;
            case ' ':
                return farnsworthWordConstant * 7;
            default:
                throw new RuntimeException("Unhandled char case " + c);

        }
    }


    public CWToneManager(int wpm) {
        this.wpm = wpm;
    }

    void playSoundTest(){
        //byte[] generatedSnd1 = buildSnd("....");
        byte[] generatedSnd1 = buildSnd(20, "..../- .../- ../- ./-");
        //byte[] generatedSnd1 = buildSampleTone();
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateHz, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd1.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd1, 0, generatedSnd1.length);
        audioTrack.play();
    }

    public void playLetter(String requestedMessage) throws InterruptedException {
        Log.d(TAG, "Requested message: " + requestedMessage);
        String pattern = LETTER_TONES.get(requestedMessage.toUpperCase());
        if (pattern == null) {
            throw new RuntimeException("No pattern found for letter: " + requestedMessage);
        }

        byte[] generatedSnd = buildSnd(wpm, pattern);
        // TODO, do this audioTrack init only once. requires playing with buffer size
        // TODO, move to android 26 AudioTrack builder
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateHz, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();

        // Trying to make this method block until "audioTrack.play()" is done.
        // Polling for lack of change in the PlaybackHeadPosition seems to work.
        // It probably isn't the best way to do it, but idk, it seems to work!
        int playbackHeadPosition1;
        int playbackHeadPosition2;
        do {
            playbackHeadPosition1 = audioTrack.getPlaybackHeadPosition();
            Thread.sleep(100);
            playbackHeadPosition2 = audioTrack.getPlaybackHeadPosition();

        } while (playbackHeadPosition1 != playbackHeadPosition2);
    }
}
