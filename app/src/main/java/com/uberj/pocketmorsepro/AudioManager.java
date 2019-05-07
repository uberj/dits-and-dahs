package com.uberj.pocketmorsepro;

import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class AudioManager {
    private static final int sampleRateHz = 44100;
    private static int silenceSymbolsAfterDitDah = 1;

    private static final ImmutableMap<String, String> LETTER_DEFINITIONS = ImmutableMap.<String, String>builder()
            .put("A", ".-")
            .put("B", "-...")
            .put("C", "-.-.")
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


    private final AudioTrack cwplayer;
    private final int minBufferSize;
    private final int letterWpm;
    private final double farnsworth;
    private final int freqOfToneHz;
    private final AudioTrack incorrectTonePlayer;
    private final AudioTrack correctTonePlayer;
    private final byte[] incorrectTone;
    private final byte[] correctTone;

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
       50 (symbolAnalysis in "paris"
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
                int rampSamples = (int) (pcmDetails.numSamplesPerSymbol * 0.20); // 20% ramp up/down

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

    public long letterSpaceToMillis() {
        int numSymbols = numSymbols('/', farnsworth);
        return symbolCountToMillis(numSymbols);
    }

    public void playIncorrectTone() {
        incorrectTonePlayer.write(incorrectTone, 0, incorrectTone.length);
    }

    public void playCorrectTone() {
        correctTonePlayer.write(correctTone, 0, correctTone.length);
    }


    public static class PCMDetails {
        public int totalNumberSymbols;
        public int totalNumberSamples;
        public int numSamplesPerSymbol;
        public double symbolsPerSecond;
    }

    private PCMDetails calcPCMDetails(int wpm, String s) {
        // calculate number of symbolAnalysis
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
        baud(w) = symbolAnalysis per second at $w words per minute
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

    public AudioManager(int letterWpm, int effective, int audioToneFrequency, Resources resources) {
        this.freqOfToneHz = audioToneFrequency;
        this.letterWpm = letterWpm;
        this.farnsworth = calcFarnsworth(letterWpm, effective);

        int channelOutStereo = AudioFormat.CHANNEL_OUT_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateHz, channelOutStereo, encoding);
        cwplayer = new AudioTrack.Builder()
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

        InputStream incorrectToneInputStream = resources.openRawResource(R.raw.incorrect_wav_16000);
        try {
            incorrectTone = IOUtils.toByteArray(incorrectToneInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load incorrect tone");
        }
        incorrectTonePlayer = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(16000)
                        .setChannelMask(channelOutStereo)
                        .build())
                .setBufferSizeInBytes(incorrectTone.length)
                .build();
        incorrectTonePlayer.play();

        InputStream correctToneInputStream = resources.openRawResource(R.raw.correct_wav_16000);
        try {
            correctTone = IOUtils.toByteArray(correctToneInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load correct tone");
        }
        correctTonePlayer = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(16000)
                        .setChannelMask(channelOutStereo)
                        .build())
                .setBufferSizeInBytes(correctTone.length)
                .build();
        correctTonePlayer.play();
    }

    private double calcFarnsworth(int letterWpm, int effectiveWpm) {
        // letterwpm = 20
        // effectivewpm = 20
        // -> farnsworth = 1

        // letterwpm = 20
        // effectivewpm = 10
        // -> farnsworth = 2

        double f = (double) letterWpm / (double) effectiveWpm;
        return Math.max(1, f);
    }

    public AudioManager(int letterWpm, int audioToneFrequency, Resources resources) {
        this(letterWpm, letterWpm, audioToneFrequency, resources);
    }

    public void destroy() {
        cwplayer.stop();
        cwplayer.release();
        incorrectTonePlayer.stop();
        incorrectTonePlayer.release();
        correctTonePlayer.stop();
        correctTonePlayer.release();
    }

    void playSoundTest(){
        //byte[] generatedSnd1 = buildSnd("....");
        byte[] generatedSnd1 = buildSnd(50, "... --- ...");
        for (int j = 0; j < 44; j++) {
        }
    }

    public void playMessage(String requestedMessage) {
        Timber.d("Requested message: %s", requestedMessage);
        String pattern = explodeToSymbols(requestedMessage);
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
            cwplayer.write(generatedSnd, i, size);
            if (cwplayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                cwplayer.play();
            }
        }

    }

    protected static String explodeToSymbols(String requestedMessage) {
        StringBuilder symbols = new StringBuilder();
        for (int i = 0; i < requestedMessage.length(); i++) {
            char c = requestedMessage.charAt(i);
            String pattern = LETTER_DEFINITIONS.get(String.valueOf(c).toUpperCase());
            symbols.append(pattern);

            boolean skipLetterSpace =
                            i == requestedMessage.length() - 1 ||
                            requestedMessage.charAt(i + 1) == ' ' ||
                            c == ' ';

            if (!skipLetterSpace) {
                symbols.append("/");
            }
        }
        return symbols.toString();
    }
}
