package com.example.uberj.test1.lettertraining;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

interface DialogFragmentProvider {
    @NonNull
    DialogFragment getHelpDialog();

    FragmentManager getHelpDialogFragmentManager();
}