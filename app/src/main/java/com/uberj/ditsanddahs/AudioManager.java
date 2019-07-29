package com.uberj.ditsanddahs;

import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

import static android.media.AudioManager.STREAM_MUSIC;

public class AudioManager {
    public static final char WORD_SPACE = ' '; // TODO, remove coupling
    public static final char LETTER_SPACE = '_'; // TODO, remove coupling
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
            .put("_", "_")
            .build();


    private final AudioTrack cwplayer;
    // crispness describes how much the pcm should be scaled up
    private final int minBufferSize;
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
    public byte[] buildSnd(String s, MorseConfig config) {
        PCMDetails pcmDetails = calcPCMDetails(config, s);
        double[] rawSnd = new double[pcmDetails.totalNumberSamples];

        int sndIdx = 0;
        double fadeInOutPercentage = (double) config.fadeInOutPercentage/(double) 100;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            double farnsworth = calcFarnsworth(config.letterWpm, config.effectiveWpm);
            int curNumberSymbols = numSymbols(c, config, farnsworth);
            int numSamplesForCurSymbol = curNumberSymbols * pcmDetails.numSamplesPerSymbol;

            // fill out the array with symbol
            if (symbolIsSilent(c)) {
                for (int j = 0; j < numSamplesForCurSymbol; ++j) {
                    rawSnd[sndIdx++] = 0;
                }
            } else {
                int rampSamples = (int) (pcmDetails.numSamplesPerSymbol * fadeInOutPercentage);

                for (int j = 0; j < rampSamples; j++) {
                    double amplitude = (double) j / (double) rampSamples;
                    rawSnd[sndIdx++] = amplitude * Math.sin((2 * Math.PI * j * config.toneFrequencyHz) / sampleRateHz);
                }

                for (int j = rampSamples; j < numSamplesForCurSymbol - rampSamples; j++) {
                    rawSnd[sndIdx++] = Math.sin((2 * Math.PI * j * config.toneFrequencyHz) / sampleRateHz);
                }

                int rampIdx = rampSamples;
                for (int j = numSamplesForCurSymbol - rampSamples; j < numSamplesForCurSymbol; j++) {
                    double amplitude = (double) rampIdx / (double) rampSamples;
                    rawSnd[sndIdx++] = amplitude * Math.sin((2 * Math.PI * j * config.toneFrequencyHz) / sampleRateHz);
                    rampIdx--;
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

    private long symbolCountToMillis(int numSymbols, int letterWpm) {
        double symbolsPerSecond = getSymbolsPerSecond(letterWpm);
        return (long) ((1D / symbolsPerSecond) * ((double) numSymbols) * 1000D);
    }

    public long wordSpaceToMillis(MorseConfig config) {
        double farnsworth = calcFarnsworth(config.letterWpm, config.effectiveWpm);
        int numSymbols = numSymbols(' ', config, farnsworth);
        // Why scale by 70%? I'm not sure why, but if I don't do this the space just seems too long. Maybe its because of the thread context switching? probably should use a morse config flag for this
        return symbolCountToMillis(numSymbols, config.letterWpm);
    }

    public void playIncorrectTone() {
        synchronized (incorrectTonePlayer) {
            incorrectTonePlayer.write(incorrectTone, 0, incorrectTone.length);
            if (incorrectTonePlayer.getState() == AudioTrack.STATE_INITIALIZED) {
                incorrectTonePlayer.play();
            }
        }
    }

    public void playCorrectTone() {
        synchronized (correctTonePlayer) {
            correctTonePlayer.write(correctTone, 0, correctTone.length);
            if (correctTonePlayer.getState() == AudioTrack.STATE_INITIALIZED) {
                correctTonePlayer.play();
            }
        }
    }


    public static class PCMDetails {
        public int totalNumberSymbols;
        public int totalNumberSamples;
        public int numSamplesPerSymbol;
        public double symbolsPerSecond;
    }

    private PCMDetails calcPCMDetails(MorseConfig config, String s) {
        // calculate number of symbolAnalysis
        int totalNumSymbols = 0;
        double farnsworth = calcFarnsworth(config.letterWpm, config.effectiveWpm);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            totalNumSymbols += numSymbols(c, config, farnsworth);
            if (!symbolIsSilent(c)) {
                totalNumSymbols += silenceSymbolsAfterDitDah;
            }
        }

        double symbolsPerSecond = getSymbolsPerSecond(config.letterWpm);
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
            case LETTER_SPACE:
            case WORD_SPACE:
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

    public static int numSymbolsForStringNoFarnsworth(String s, MorseConfig config) {
        int total = 0;
        String ditDahs = LETTER_DEFINITIONS.get(s);
        if (ditDahs == null) {
            throw new RuntimeException("Unknown letter: " + s);
        }

        for (char c : ditDahs.toCharArray()) {
            total+= numSymbols(c, config, 1);
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

    private static int numSymbols(char c, MorseConfig config, double farnsworth) {
        switch (c) {
            case '-':
                return 3;
            case '.':
                return 1;
            case LETTER_SPACE:
                return (int) (config.symbolsBetweenLetters * farnsworth);
            case WORD_SPACE:
                return (int) (config.symbolsBetweenWords * farnsworth);
            default:
                throw new RuntimeException("Unhandled char case " + c);
        }
    }

    public AudioManager(Resources resources) {
        int channelOutStereo = AudioFormat.CHANNEL_OUT_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateHz, channelOutStereo, encoding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .build();
            cwplayer.play();
        } else {
            cwplayer = new AudioTrack(STREAM_MUSIC, sampleRateHz, channelOutStereo, encoding, minBufferSize, AudioTrack.MODE_STREAM);
        }

        InputStream incorrectToneInputStream = resources.openRawResource(R.raw.incorrect_wav_16000);
        try {
            incorrectTone = IOUtils.toByteArray(incorrectToneInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load incorrect tone");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } else {
            incorrectTonePlayer = new AudioTrack(STREAM_MUSIC, 16000, channelOutStereo, encoding, incorrectTone.length, AudioTrack.MODE_STREAM);
        }

        InputStream correctToneInputStream = resources.openRawResource(R.raw.correct_wav_16000);
        try {
            correctTone = IOUtils.toByteArray(correctToneInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load correct tone");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } else {
            correctTonePlayer = new AudioTrack(STREAM_MUSIC, 16000, channelOutStereo, encoding, correctTone.length, AudioTrack.MODE_STREAM);
        }
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

    public void destroy() {
        synchronized (cwplayer) {
            if (cwplayer.getState() == AudioTrack.STATE_INITIALIZED) {
                cwplayer.stop();
            }
            cwplayer.release();
        }
        synchronized (incorrectTonePlayer) {
            if (incorrectTonePlayer.getState() == AudioTrack.STATE_INITIALIZED) {
                incorrectTonePlayer.stop();
            }
            incorrectTonePlayer.release();
        }
        synchronized (correctTonePlayer) {
            if (correctTonePlayer.getState() == AudioTrack.STATE_INITIALIZED) {
                correctTonePlayer.stop();
            }
            correctTonePlayer.release();
        }
    }

    void playSoundTest(){
        //byte[] generatedSnd1 = buildSnd("....");
        MorseConfig.Builder builder = MorseConfig.builder();
        builder.setEffectiveWpm(30);
        builder.setLetterWpm(30);
        builder.setToneFrequencyHz(440);
        byte[] generatedSnd1 = buildSnd("... --- ...", builder.build());
        for (int j = 0; j < 44; j++) {
        }
    }

    public static class MorseConfig {
        private final int toneFrequencyHz;
        private final int letterWpm;
        private final int effectiveWpm;
        private final int fadeInOutPercentage;
        private final int symbolsBetweenLetters;
        private final int symbolsBetweenWords;

        private MorseConfig(int toneFrequencyHz, int letterWpm, int effectiveWpm, GlobalSettings globalSettings) {
            this.toneFrequencyHz = toneFrequencyHz;
            this.letterWpm = letterWpm;
            this.effectiveWpm = effectiveWpm;
            this.fadeInOutPercentage = globalSettings.getFadeInOutPercentage();
            this.symbolsBetweenLetters = globalSettings.getSymbolsBetweenLetters();
            this.symbolsBetweenWords = globalSettings.getSymbolsBetweenWords();
        }

        public int getToneFrequencyHz() {
            return toneFrequencyHz;
        }

        public int getLetterWpm() {
            return letterWpm;
        }

        public int getEffectiveWpm() {
            return effectiveWpm;
        }

        public int getFadeInOutPercentage() {
            return fadeInOutPercentage;
        }

        public int getSymbolsBetweenLetters() {
            return symbolsBetweenLetters;
        }

        public int getSymbolsBetweenWords() {
            return symbolsBetweenWords;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer toneFrequencyHz;
            private Integer letterWpm;
            private Integer effectiveWpm;
            private GlobalSettings globalSettings;

            public Builder setToneFrequencyHz(int toneFrequencyHz) {
                this.toneFrequencyHz = toneFrequencyHz;
                return this;
            }

            public Builder setLetterWpm(int letterWpm) {
                this.letterWpm = letterWpm;
                return this;
            }

            public Builder setEffectiveWpm(int effectiveWpm) {
                this.effectiveWpm = effectiveWpm;
                return this;
            }

            public Builder setGlobalSettings(GlobalSettings globalSettings) {
                this.globalSettings = globalSettings;
                return this;
            }

            public MorseConfig build() {
                Preconditions.checkNotNull(toneFrequencyHz);
                Preconditions.checkNotNull(letterWpm);
                Preconditions.checkNotNull(effectiveWpm);
                Preconditions.checkNotNull(globalSettings);
                return new MorseConfig(toneFrequencyHz, letterWpm, effectiveWpm, globalSettings);
            }
        }
    }

    public void playMessage(String requestedMessage, MorseConfig config) {
        Timber.d("Requested message: %s", requestedMessage);
        String pattern = explodeToSymbols(requestedMessage);
        byte[] generatedSnd = buildSnd(pattern, config);
        for (int i = 0; i <= generatedSnd.length; i += minBufferSize) {
            int size;
            if (i + minBufferSize > generatedSnd.length) {
                size = generatedSnd.length - i;
            } else {
                size = minBufferSize;
            }
            synchronized (cwplayer) {
                if (cwplayer.getState() == AudioTrack.STATE_INITIALIZED && cwplayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    cwplayer.play();
                }
                cwplayer.write(generatedSnd, i, size);
            }
        }
        if (cwplayer.getState() == AudioTrack.STATE_INITIALIZED) {
            cwplayer.play();
        }

    }

    protected static String explodeToSymbols(String requestedMessage) {
        StringBuilder symbols = new StringBuilder();
        for (int i = 0; i < requestedMessage.length(); i++) {
            char c = requestedMessage.charAt(i);
            String pattern = LETTER_DEFINITIONS.get(String.valueOf(c).toUpperCase());
            if (pattern == null) {
                throw new RuntimeException("No letter definition found for " + c);
            }
            symbols.append(pattern);

            boolean skipLetterSpace =
                            i == requestedMessage.length() - 1 ||
                            requestedMessage.charAt(i + 1) == ' ' ||
                            c == WORD_SPACE;

            if (!skipLetterSpace) {
                symbols.append(LETTER_SPACE);
            }
        }
        return symbols.toString();
    }
}
