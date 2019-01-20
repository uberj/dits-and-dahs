package com.example.uberj.test1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class CWToneManager {
    private static final String TAG = "CWToneManager";
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    // again modified by Jacques Uber <mail@uberj.com>

    private static final int wpm = 20;
    private static final int sampleRateHz = 44100;

//    private static final ImmutableMap<String, byte[]> LETTER_TONES = ImmutableMap.<String, byte[]>builder()
//            .put("a", buildSnd(".-"))
//            .put("b", buildSnd("...-"))
//            .put("c", buildSnd(".-.-"))
//            .put("d", buildSnd("-.."))
//            .put("e", buildSnd("."))
//            .put("f", buildSnd("..-."))
//            .put("g", buildSnd("--."))
//            .put("h", buildSnd("...."))
//            .put("i", buildSnd(".."))
//            .put("j", buildSnd(".---"))
//            .put("k", buildSnd("-.-"))
//            .put("l", buildSnd(".-.."))
//            .put("m", buildSnd("--"))
//            .put("n", buildSnd("-."))
//            .put("o", buildSnd("---"))
//            .put("p", buildSnd(".--."))
//            .put("q", buildSnd("--.-"))
//            .put("r", buildSnd(".-."))
//            .put("s", buildSnd("..."))
//            .put("t", buildSnd("-"))
//            .put("u", buildSnd("..-"))
//            .put("v", buildSnd("-..."))
//            .put("w", buildSnd(".--"))
//            .put("x", buildSnd("-..-"))
//            .put("y", buildSnd("-.--"))
//            .put("z", buildSnd("--.."))
//            .put("1", buildSnd(".----"))
//            .put("2", buildSnd("..---"))
//            .put("3", buildSnd("...--"))
//            .put("4", buildSnd("....-"))
//            .put("5", buildSnd("....."))
//            .put("6", buildSnd("-...."))
//            .put("7", buildSnd("--..."))
//            .put("8", buildSnd("---.."))
//            .put("9", buildSnd("----."))
//            .put("0", buildSnd("-----"))
//            .put(" ", buildSnd(" "))
//            .build();


    private static int farnsWorthSpace = 3;
    private static int freqOfToneHz = 440;
    private static int silenceSymbolsAfterDitDah = 1;
    private static int farnsWorthConstant = 1;

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
    public static byte[] buildSnd(String s) {
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
            int numSamples = curNumberSymbols * numSamplesPerSymbol;
            Log.d(TAG, "Tone letter: " + c + "\n" +
                    "Number of symbols: " + curNumberSymbols + "\n" +
                    "Sample rate: " + sampleRateHz + "\n" +
                    "Baud: " + symbolsPerSecond + "\n" +
                    "Tone length1 (seconds): " + (curNumberSymbols * symbolDuration) + "\n" +
                    "Tone length2 (seconds): " + (numSamplesPerSymbol * curNumberSymbols) / sampleRateHz);

            // fill out the array
            for (int j = 0; j < numSamples; ++j) {
                if (symbolIsSilent(c)) {
                    rawSnd[sndIdx++] = 0;
                } else {
                    rawSnd[sndIdx++] = Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                }
            }

            if (!symbolIsSilent(c)) {
                int silencePadSamples = numSamplesPerSymbol * silenceSymbolsAfterDitDah;
                for (int j = 0; j < silencePadSamples; ++j) {
                    if (symbolIsSilent(c)) {
                        rawSnd[sndIdx++] = 0;
                    } else {
                        rawSnd[sndIdx++] = Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                    }
                }
            }
        }

//        int i;
//        int idx = 0;
//        int ramp = totalNumSamples / 20; // Amplitude ramp as a percent of sample count
//        byte pcmSnd[] = new byte[2 * totalNumSamples];
//
//        for (i = 0; i < ramp; ++i) {  // Ramp amplitude up (to avoid clicks)
//            // Ramp up to maximum
//            final short val = (short) (rawSnd[i] * 32767 * i / ramp);
//            // in 16 bit wav PCM, first byte is the low order byte
//            pcmSnd[idx++] = (byte) (val & 0x00ff);
//            pcmSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//        }
//
//        for (i = ramp; i < totalNumSamples - ramp;
//             ++i) {                        // Max amplitude for most of the samples
//            // scale to maximum amplitude
//            final short val = (short) (rawSnd[i] * 32767);
//            // in 16 bit wav PCM, first byte is the low order byte
//            pcmSnd[idx++] = (byte) (val & 0x00ff);
//            pcmSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//        }
//
//        for (i = totalNumSamples - ramp; i < totalNumSamples; ++i) { // Ramp amplitude down
//            // Ramp down to zero
//            final short val = (short) (rawSnd[i] * 32767 * (totalNumSamples - i) / ramp);
//            // in 16 bit wav PCM, first byte is the low order byte
//            pcmSnd[idx++] = (byte) (val & 0x00ff);
//            pcmSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        byte pcmSnd[] = new byte[2 * totalNumSamples];
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

    // ditsPerSecond = (wpm * 50 dits) / 60 seconds
    // numSamplesPerDit = ditsPerSecond * sampleRateHz
    //
    // Notes:
    //  * 3 dits per dash
    //  * 7 dits per space

    public static int numSymbols(char c) {
        switch (c) {
            case '-':
                return 3;
            case '.':
                return 1;
            case '/':
                return 3;
            case ' ':
                return farnsWorthConstant * 7;
            default:
                throw new RuntimeException("Unhandled char case " + c);

        }
    }


    public CWToneManager() {

    }

    void playSound(){
        //byte[] generatedSnd1 = buildSnd("....");
        byte[] generatedSnd1 = buildSnd(".--./.-/.-./../...");
        //byte[] generatedSnd1 = buildSampleTone();
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateHz, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd1.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd1, 0, generatedSnd1.length);
        audioTrack.play();
    }

    private byte[] buildSampleTone() {
        int numSamples = sampleRateHz * 10;  // 3 seconds
        double rawSnd[] = new double[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            if ((i / sampleRateHz) % 2 == 0) {
                rawSnd[i] = 0;
            } else {
                rawSnd[i] = Math.sin((2 * Math.PI * i * freqOfToneHz) / sampleRateHz);
            }
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        byte pcmSnd[] = new byte[2 * numSamples];
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
}
