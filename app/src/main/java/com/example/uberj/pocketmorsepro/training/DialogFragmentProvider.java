package com.example.uberj.morsepocketpro.training;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public interface DialogFragmentProvider {
    @NonNull
    DialogFragment getHelpDialog();

    FragmentManager getHelpDialogFragmentManager();
}
