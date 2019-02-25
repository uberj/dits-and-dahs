package com.example.uberj.test1.lettertraining.simple;

import android.app.Application;

import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

class SimpleLetterTrainingHelpDialogViewModel extends AndroidViewModel {
    private boolean animateThreadKeepAlive = true;

    public final MutableLiveData<Integer> timerCountDownProgress = new MutableLiveData<>(1);
    public final MutableLiveData<Integer> incorrectGuessCounter = new MutableLiveData<>(1);
    public final MutableLiveData<Integer> correctGuessCounter = new MutableLiveData<>(1);

    public SimpleLetterTrainingHelpDialogViewModel(@NonNull Application application) {
        super(application);
        animate(this::updateExampleCountDownProgress, 0, 50);
        animate(this::updateCorrectGuessCounter, 0, 4000);
        animate(this::updateIncorrectGuessCounter, 2000, 4000);
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
