package com.uberj.pocketmorsepro.training.simple;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.uberj.pocketmorsepro.DynamicKeyboard;
import com.uberj.pocketmorsepro.ProgressGradient;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.keyboards.KeyConfig;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import static com.uberj.pocketmorsepro.simplesocratic.SocraticKeyboardSessionActivity.DISABLED_BUTTON_ALPHA;
import static com.uberj.pocketmorsepro.simplesocratic.SocraticKeyboardSessionActivity.DISABLED_PROGRESS_BAR_ALPHA;
import static com.uberj.pocketmorsepro.simplesocratic.SocraticKeyboardSessionActivity.ENABLED_BUTTON_ALPHA;
import static com.uberj.pocketmorsepro.simplesocratic.SocraticKeyboardSessionActivity.ENABLED_PROGRESS_BAR_ALPHA;

public class SimpleLetterTrainingHelpDialog extends DialogFragment implements NextPrevTabHandler, DismissibleFragment {
    private ViewPager viewPager;

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
        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        return view;
    }

    @Override
    public void requestTab(int requestTab) {
        // requestTab is 1 indexed, so its value is actually the correct "next" value
        viewPager.setCurrentItem(Math.min(viewPager.getChildCount(), requestTab), true);
    }

    @Override
    public void requestDismiss() {
        dismiss();
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
        adapter.addFragment(new HelpScreen0(), "Listen and Guess");
        adapter.addFragment(new HelpScreen1(), "Playing a letter tone");
        adapter.addFragment(new HelpScreen2(), "The timer bar");
        adapter.addFragment(new HelpScreen3(), "Progress");
        adapter.addFragment(new HelpScreen4(), "Ready?");
        viewPager.setAdapter(adapter);
    }

    public static class HelpScreen0 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen0, container, false);
            LinearLayout exampleLetterKeyContainer = inflate.findViewById(R.id.example_letter);
            DynamicKeyboard builder = new DynamicKeyboard.Builder()
                    .setRootView(exampleLetterKeyContainer)
                    .setContext(getActivity())
                    .setKeys(ImmutableList.of(ImmutableList.of(KeyConfig.l("M"))))
                    .setButtonCallback((b, kc) -> {})
                    .setProgressBarCallback((p, v) -> {})
                    .build();

            builder.buildAtRoot();

            ImageView nextTab = inflate.findViewById(R.id.next_help_tab0);
            nextTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(1));
            return inflate;
        }
    }

    public static class HelpScreen1 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen1, container, false);
            LinearLayout exampleLetterKeyContainer = inflate.findViewById(R.id.example_letter_2);
            DynamicKeyboard builder = new DynamicKeyboard.Builder()
                    .setRootView(exampleLetterKeyContainer)
                    .setContext(getActivity())
                    .setKeys(ImmutableList.of(ImmutableList.of(KeyConfig.l("M"))))
                    .setButtonCallback((b, kc) -> {})
                    .setProgressBarCallback((p, v) -> {})
                    .build();

            builder.buildAtRoot();

            ImageView nextTab = inflate.findViewById(R.id.next_help_tab1);
            nextTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(2));

            ImageView prevTab = inflate.findViewById(R.id.prev_help_tab1);
            prevTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(0));
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

            ImageView nextTab = inflate.findViewById(R.id.next_help_tab2);
            nextTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(3));

            ImageView prevTab = inflate.findViewById(R.id.prev_help_tab2);
            prevTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(1));

            return inflate;
        }
    }

    public static class HelpScreen3 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen3, container, false);

            SimpleLetterTrainingHelpDialogViewModel viewModel = ViewModelProviders
                    .of(Objects.requireNonNull(getActivity()))
                    .get(SimpleLetterTrainingHelpDialogViewModel.class);

            LinearLayout exampleLetterKeyContainer = inflate.findViewById(R.id.example_letters);
            DynamicKeyboard keyboard = new DynamicKeyboard.Builder()
                    .setRootView(exampleLetterKeyContainer)
                    .setContext(getActivity())
                    .setKeys(SimpleLetterTrainingHelpDialogViewModel.EXAMPLE_BOARD_KEYS)
                    .setButtonCallback((b, kc) -> {})
                    .setProgressBarCallback((p, v) -> {})
                    .build();
            keyboard.buildAtRoot();

            BiConsumer<String, Map<String, Integer>> progressBarUpdater = (letter, weights) -> {
                View progressBar = keyboard.getLetterProgressBar(letter);
                if (progressBar == null) {
                    return;
                }
                Integer competencyWeight = weights.get(letter);
                Integer color = ProgressGradient.forWeight(competencyWeight);
                progressBar.setBackgroundColor(color);
            };

            viewModel.inPlayLetterKeys.observe(this, (inPlayKeys) -> {
                for (String key : SimpleLetterTrainingHelpDialogViewModel.EXAMPLE_LETTERS) {
                    View view = keyboard.getViewFromKeyText(key);
                    if (!(view instanceof Button)) {
                        return;
                    }
                    Button button = (Button) view;
                    View progressBar = keyboard.getLetterProgressBar(key);

                    if (inPlayKeys.contains(key)) {
                        String buttonLetter = button.getText().toString();
                        progressBarUpdater.accept(buttonLetter, viewModel.weights.getValue());
                        button.setAlpha(ENABLED_BUTTON_ALPHA);
                        progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);

                    } else {
                        button.setAlpha(DISABLED_BUTTON_ALPHA);
                        progressBar.setAlpha(DISABLED_PROGRESS_BAR_ALPHA);
                        progressBar.setBackgroundColor(ProgressGradient.DISABLED);
                    }
                }
            });

            viewModel.weights.observe(this, (weights) -> {
                for (String letter : SimpleLetterTrainingHelpDialogViewModel.EXAMPLE_LETTERS) {
                    if (viewModel.inPlayLetterKeys.getValue().contains(letter)) {
                        progressBarUpdater.accept(letter, weights);
                    }
                }
            });


            ImageView nextTab = inflate.findViewById(R.id.next_help_tab3);
            nextTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(4));

            ImageView prevTab = inflate.findViewById(R.id.prev_help_tab3);
            prevTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(2));
            return inflate;
        }

    }

    public static class HelpScreen4 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View inflate = inflater.inflate(R.layout.simple_letter_training_help_dialog_screen4, container, false);

            ImageView prevTab = inflate.findViewById(R.id.prev_help_tab4);
            prevTab.setOnClickListener((view) ->
                    ((NextPrevTabHandler)Objects.requireNonNull(getParentFragment())).requestTab(3));

            Button continueButton = inflate.findViewById(R.id.continue_session_button);
            continueButton.setOnClickListener((view) ->
                    ((DismissibleFragment)Objects.requireNonNull(getParentFragment())).requestDismiss());
            return inflate;
        }
    }
}
