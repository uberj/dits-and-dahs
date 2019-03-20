package com.example.uberj.morsepocketpro.training.simple;

import android.app.Application;

import com.example.uberj.morsepocketpro.keyboards.KeyConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

class SimpleLetterTrainingHelpDialogViewModel extends AndroidViewModel {
    public static final ImmutableList<String> EXAMPLE_LETTERS = ImmutableList.of("H", "J", "K", "N", "M");
    public static final ImmutableList<ImmutableList<KeyConfig>> EXAMPLE_BOARD_KEYS = ImmutableList.of(
            ImmutableList.of(KeyConfig.l("H"), KeyConfig.l("J"), KeyConfig.l("K")),
            ImmutableList.of(KeyConfig.s(), KeyConfig.l("N"), KeyConfig.l("M"), KeyConfig.s())
    );

    private boolean animateThreadKeepAlive = true;
    public final MutableLiveData<Map<String, Integer>> weights = new MutableLiveData<>(buildBlankWeights());
    public final MutableLiveData<List<String>> inPlayLetterKeys = new MutableLiveData<>(Lists.newArrayList());
    public final MutableLiveData<Integer> timerCountDownProgress = new MutableLiveData<>(1);
    public final MutableLiveData<Integer> incorrectGuessCounter = new MutableLiveData<>(1);
    public final MutableLiveData<Integer> correctGuessCounter = new MutableLiveData<>(1);

    public SimpleLetterTrainingHelpDialogViewModel(@NonNull Application application) {
        super(application);
        // Init weights
        animate(this::updateExampleCountDownProgress, 0, 50);
        animate(this::updateCorrectGuessCounter, 0, 4000);
        animate(this::updateIncorrectGuessCounter, 2000, 4000);
        animate(this::updateExampleWeights, 0, 500);
        animate(this::updateExampleInPlayKeys, 2000, 5000);
    }

    private static Map<String, Integer> buildBlankWeights() {
        Map<String, Integer> w = Maps.newHashMap();
        for (String letter : EXAMPLE_LETTERS) {
            w.put(letter, 0);
        }
        return w;
    }

    private Void updateExampleInPlayKeys() {
        List<String> updatedInPlayKeys = Lists.newArrayList();
        for (String letter : EXAMPLE_LETTERS) {
            Integer weight = weights.getValue().get(letter);
            if (weight >= 75) {
                updatedInPlayKeys.add(letter);
            } else {
                updatedInPlayKeys.add(letter);
                inPlayLetterKeys.postValue(updatedInPlayKeys);
                return null;
            }
        }

        // Everything was 100 so lets restart;
        inPlayLetterKeys.postValue(Lists.newArrayList(EXAMPLE_LETTERS.get(0)));
        weights.postValue(buildBlankWeights());
        return null;
    }

    private Void updateExampleWeights() {
        for (String letter : inPlayLetterKeys.getValue()) {
            Map<String, Integer> weightMap = weights.getValue();
            Integer weight = weightMap.get(letter);
            if (weight == 100) {
                continue;
            }
            weight = Math.min(100, weight + 10);
            weightMap.put(letter, weight);
            weights.postValue(weightMap);
            break;
        }

        return null;
    }

    private void animate(Callable<Void> step, int delay, int sleepDurationMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }

            while (true) {
                if (!animateThreadKeepAlive) {
                    return;
                }

                try {
                    step.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                try {
                    Thread.sleep(sleepDurationMillis);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    private Void updateCorrectGuessCounter() {
        Integer currentValue = correctGuessCounter.getValue();
        correctGuessCounter.postValue(Math.max(0, currentValue - 10));
        if (currentValue == 0) {
            correctGuessCounter.postValue(1000);
        };

        return null;
    }

    private Void updateIncorrectGuessCounter() {
        Integer currentValue = incorrectGuessCounter.getValue();
        incorrectGuessCounter.postValue(Math.max(0, currentValue - 1));
        if (currentValue == 0) {
            incorrectGuessCounter.postValue(1000);
        }

        return null;
    }

    private Void updateExampleCountDownProgress() {
        Integer currentValue = timerCountDownProgress.getValue();
        timerCountDownProgress.postValue(Math.max(0, currentValue - 1));
        if (currentValue == 0) {
            timerCountDownProgress.postValue(1000);
        }

        return null;
    }

    @Override
    protected void onCleared() {
        animateThreadKeepAlive = false;
        super.onCleared();
    }
}
