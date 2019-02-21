package com.example.uberj.test1.lettertraining.simple;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SimpleLetterTrainingHelpDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

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
        View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog, container, false);
        DynamicKeyboard builder = new DynamicKeyboard.Builder()
                .setContext(getActivity())
                .setKeys(ImmutableList.of(ImmutableList.of(KeyConfig.l("M"))))
                .setButtonCallback((b) -> {})
                .setProgressBarCallback((p, v) -> {})
                .createKeyboardBuilder();

        LinearLayout exampleLetterKeyContainer = inflate.findViewById(R.id.example_letter_key);
        builder.buildAtRoot(exampleLetterKeyContainer);

        return inflate;
    }
}
