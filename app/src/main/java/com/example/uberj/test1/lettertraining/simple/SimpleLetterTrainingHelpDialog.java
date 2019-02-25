package com.example.uberj.test1.lettertraining.simple;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

public class SimpleLetterTrainingHelpDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
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

        View view = inflater.inflate(R.layout.simple_letter_training_help_dialog, container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        return view;
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new HelpScreen1(), "Listen and Guess");
        adapter.addFragment(new HelpScreen2(), "The timer bar");
        adapter.addFragment(new HelpScreen3(), "Ready?");
        viewPager.setAdapter(adapter);
    }

    public static class HelpScreen1 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen1, container, false);
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

    public static class HelpScreen2 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen2, container, false);

            SimpleLetterTrainingHelpDialogViewModel viewModel = ViewModelProviders
                    .of(Objects.requireNonNull(getActivity()))
                    .get(SimpleLetterTrainingHelpDialogViewModel.class);
            viewModel.timerCountDownProgress.observe(this, (progress) -> {
                ProgressBar progressBar = inflate.findViewById(R.id.top_example_timer_progress_bar);
                progressBar.setProgress(progress, true);
            });

            viewModel.correctGuessCounter.observe(this, (progress) -> {
                ProgressBar progressBar = inflate.findViewById(R.id.correct_example_timer_progress_bar);
                TransitionDrawable background = (TransitionDrawable) progressBar.getProgressDrawable();
                background.startTransition(0);
                background.reverseTransition(500);
            });

            viewModel.incorrectGuessCounter.observe(this, (progress) -> {
                ProgressBar progressBar = inflate.findViewById(R.id.incorrect_example_timer_progress_bar);
                TransitionDrawable background = (TransitionDrawable) progressBar.getProgressDrawable();
                background.startTransition(0);
                background.reverseTransition(500);
            });

            return inflate;
        }
    }

    public static class HelpScreen3 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen3, container, false);
            return inflate;
        }
    }
}
