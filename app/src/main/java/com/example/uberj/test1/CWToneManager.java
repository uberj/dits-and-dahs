package com.example.uberj.test1;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.google.common.collect.ImmutableMap;

import timber.log.Timber;

public class CWToneManager {
    private static final int sampleRateHz = 44100;
    private static int freqOfToneHz = 440;
    private static int silenceSymbolsAfterDitDah = 1;

    private static final ImmutableMap<String, String> LETTER_DEFINITIONS = ImmutableMap.<String, String>builder()
            .put("A", ".-")
            .put("B", "-...")
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
            .put("V", "...-")
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
            .put("/", "-..-.")
            .put(".", ".-.-.-")
            .put(",", "--..--")
            .put("?", "..--..")
            .put("=", "-...-")
            .build();


    private final AudioTrack player;
    private final int minBufferSize;
    private final int letterWpm;
    private final double farnsworth;

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
    public byte[] buildSnd(int wpm, String s) {
        PCMDetails pcmDetails = calcPCMDetails(wpm, s);
        double rawSnd[] = new double[pcmDetails.totalNumberSamples];

        int sndIdx = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int curNumberSymbols = numSymbols(c, farnsworth);
            int numSamplesForCurSymbol = curNumberSymbols * pcmDetails.numSamplesPerSymbol;

            // fill out the array with symbol
            if (symbolIsSilent(c)) {
                for (int j = 0; j < numSamplesForCurSymbol; ++j) {
                    rawSnd[sndIdx++] = 0;
                }
            } else {
                int rampSamples = (int) (pcmDetails.numSamplesPerSymbol * 0.10); // 5% ramp up

                for (int j = 0; j < rampSamples; j++) {
                    rawSnd[sndIdx++] = ((double) j/(double) rampSamples) * Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                }
                for (int j = rampSamples; j < numSamplesForCurSymbol - rampSamples; j++) {
                    rawSnd[sndIdx++] = Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                }

                int x = rampSamples;
                for (int j = numSamplesForCurSymbol - rampSamples; j < numSamplesForCurSymbol; j++) {
                    rawSnd[sndIdx++] = ((double) x/(double) rampSamples) * Math.sin((2 * Math.PI * j * freqOfToneHz) / sampleRateHz);
                    x--;
                }
            }

            // fill out the array with space if needed
            if (!symbolIsSilent(c)) {
                int silencePadSamples = pcmDetails.numSamplesPerSymbol * silenceSymbolsAfterDitDah;
                for (int j = 0; j < silencePadSamples; ++j) {
                    rawSnd[sndIdx++] = 0;
                }
            }
        }

        return pcmConvert(rawSnd);
    }

    private long symbolCountToMillis(int numSymbols) {
        double symbolsPerSecond = getSymbolsPerSecond(letterWpm);
        return (long) ((1f / symbolsPerSecond) * numSymbols * 1000);
    }

    public long wordSpaceToMillis() {
        int numSymbols = numSymbols(' ', farnsworth);
        return symbolCountToMillis(numSymbols);
    }


    public static class PCMDetails {
        public int totalNumberSymbols;
        public int totalNumberSamples;
        public int numSamplesPerSymbol;
        public double symbolsPerSecond;
    }

    public PCMDetails calcPCMDetails(String s) {
        String ditDah = LETTER_DEFINITIONS.get(s);
        if (ditDah == null) {
            throw new RuntimeException("Unknown letter " + s);
        }
        return calcPCMDetails(letterWpm, ditDah);
    }

    private PCMDetails calcPCMDetails(int wpm, String s) {
        // calculate number of symbols
        int totalNumSymbols = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            totalNumSymbols += numSymbols(c, farnsworth);
            if (!symbolIsSilent(c)) {
                totalNumSymbols += silenceSymbolsAfterDitDah;
            }
        }

        double symbolsPerSecond = getSymbolsPerSecond(wpm);
        double symbolDuration = 1 / symbolsPerSecond;
        int numSamplesPerSymbol = (int) (symbolDuration * sampleRateHz);

        int totalNumSamples = totalNumSymbols * numSamplesPerSymbol;
        PCMDetails pcmDetails = new PCMDetails();
        pcmDetails.numSamplesPerSymbol = numSamplesPerSymbol;
        pcmDetails.symbolsPerSecond = symbolsPerSecond;
        pcmDetails.totalNumberSymbols = totalNumSymbols;
        pcmDetails.totalNumberSamples = totalNumSamples;
        return pcmDetails;
    }

    private static double getSymbolsPerSecond(int wpm) {
        return (wpm * 50f) / 60f;
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

    public static double baud(int wpm) {
        return (wpm * 50f) / 60f;
    }

    // ditsPerSecond = (wpm * 50 dits) / 60 seconds
    // numSamplesPerDit = ditsPerSecond * sampleRateHz
    //
    // Notes:
    //  * 3 dits per dash
    //  * 7 dits per space

    public static int numSymbolsForStringNoFarnsworth(String s) {
        int total = 0;
        String ditDahs = LETTER_DEFINITIONS.get(s);
        if (ditDahs == null) {
            throw new RuntimeException("Unknown letter: " + s);
        }

        for (char c : ditDahs.toCharArray()) {
            total+= numSymbols(c, 1);
        }
        return total;
    }

    /*
        baud(w) = symbols per second at $w words per minute
        baud(w) = number of times paris is played in a minute
        baud(w) = (w * 50 + (w - 1) * 7) / 60 seconds
        baud(w) = (w * 50 + w * 7 - 7) / 60
        baud(w) = (w * 57 - 7) / 60
        baud = lambda(w): w * (57.0/60) - (7.0/60)
     */

    private static int numSymbols(char c, double farnsworth) {
        switch (c) {
            case '-':
                return 3;
            case '.':
                return 1;
            case '/':
                return (int) (3 * farnsworth * 0.7f);
            case ' ':
                return (int) (7 * farnsworth);
            default:
                throw new RuntimeException("Unhandled char case " + c);
        }
    }

    public CWToneManager(int letterWpm, int transmitWpm) {
        this.letterWpm = letterWpm;
        this.farnsworth = calcFarnsworth(letterWpm, transmitWpm);

        int channelOutStereo = AudioFormat.CHANNEL_OUT_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateHz, channelOutStereo, encoding);
        player = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(sampleRateHz)
                        .setChannelMask(channelOutStereo)
                        .build())
                .setBufferSizeInBytes(minBufferSize)
                .build();
        player.play();
    }

    private double calcFarnsworth(int letterWpm, int transmitWpm) {
        // letterwpm = 20
        // transmitwpm = 20
        // -> farnsworth = 1

        // letterwpm = 20
        // transmitwpm = 10
        // -> farnsworth = 2

        double f = (double) letterWpm / (double) transmitWpm;
        return Math.max(1, f);
    }

    public CWToneManager(int letterWpm) {
        this(letterWpm, 1);
    }

    public void destroy() {
        player.stop();
        player.release();
    }

    void playSoundTest(){
        //byte[] generatedSnd1 = buildSnd("....");
        byte[] generatedSnd1 = buildSnd(50, "... --- ...");
        for (int j = 0; j < 44; j++) {
        }
    }

    public void playLetter(String requestedMessage) {
        Timber.d("Requested message: %s", requestedMessage);
        String pattern = LETTER_DEFINITIONS.get(requestedMessage.toUpperCase());
        if (pattern == null) {
            throw new RuntimeException("No pattern found for letter: " + requestedMessage);
        }

        byte[] generatedSnd = buildSnd(letterWpm, pattern);
        for (int i = 0; i < generatedSnd.length; i += minBufferSize) {
            int size;
            if (i + minBufferSize > generatedSnd.length) {
                size = generatedSnd.length - i;
            } else {
                size = minBufferSize;
            }
            player.write(generatedSnd, i, size);
        }

    }
}
