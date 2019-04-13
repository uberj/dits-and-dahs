package com.uberj.pocketmorsepro.flashcard;

import android.app.Application;

import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionWithEvents;
import com.uberj.pocketmorsepro.storage.Repository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FlashcardTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;

    public FlashcardTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<FlashcardTrainingSessionWithEvents>> getLatestSession(FlashcardSessionType sessionType) {
        return repository.flashcardTrainingSessionDAO.getLatestSessionAndEvents(sessionType.name());
    }
}
