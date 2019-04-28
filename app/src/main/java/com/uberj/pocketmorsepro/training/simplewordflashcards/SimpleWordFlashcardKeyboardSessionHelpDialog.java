package com.uberj.pocketmorsepro.training.simplewordflashcards;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uberj.pocketmorsepro.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class SimpleWordFlashcardKeyboardSessionHelpDialog extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View inflate = inflater.inflate(R.layout.simple_word_flashscreen_training_keyboard_help_dialog, container, false);
        inflate.findViewById(R.id.close_dialog).setOnClickListener((v) -> {
            dismiss();
        });

        return inflate;
    }
}
