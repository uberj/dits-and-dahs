package com.uberj.ditsanddahs.flashcard;

import android.app.Application;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSessionWithEvents;
import com.uberj.ditsanddahs.storage.Repository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FlashcardTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;
    public final MutableLiveData<ArrayList<String>> selectedStrings = new MutableLiveData<>(Lists.newArrayList());
    public final MutableLiveData<boolean[]> selectedStringsBooleanMap = new MutableLiveData<>(new boolean[]{});

    public FlashcardTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<FlashcardTrainingSessionWithEvents>> getLatestSession() {
        return repository.flashcardTrainingSessionDAO.getLatestSessionAndEvents();
    }
}
