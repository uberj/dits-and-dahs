package com.uberj.pocketmorsepro;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;

import com.uberj.pocketmorsepro.keyboards.KeyConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.uberj.pocketmorsepro.views.ProgressDots;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class DynamicKeyboard {

    private final FragmentActivity context;
    private final ImmutableList<ImmutableList<KeyConfig>> keys;
    private final Button.OnClickListener buttonOnClickListener;
    private final Button.OnLongClickListener buttonLongClickListener;
    private final BiConsumer<View, KeyConfig> buttonCallback;
    private final BiConsumer<View, View> progressBarCallback;
    private final LinearLayout rootView;
    private final boolean drawProgressBar;

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }

        return views;
    }


    public DynamicKeyboard(FragmentActivity context, ImmutableList<ImmutableList<KeyConfig>> keys, View.OnClickListener buttonOnClickListener, View.OnLongClickListener buttonLongClickListener, BiConsumer<View, KeyConfig> buttonCallback, BiConsumer<View, View> progressBarCallback, LinearLayout rootView, boolean drawProgressBar) {
        this.context = context;
        this.keys = keys;
        this.buttonOnClickListener = buttonOnClickListener;
        this.buttonLongClickListener = buttonLongClickListener;
        this.buttonCallback = buttonCallback;
        this.progressBarCallback = progressBarCallback;
        this.rootView = rootView;
        this.drawProgressBar = drawProgressBar;
    }

    public static final class Builder {
        private FragmentActivity context;
        private LinearLayout rootView;
        private ImmutableList<ImmutableList<KeyConfig>> keys;
        private View.OnClickListener buttonOnClickListener;
        private View.OnLongClickListener buttonLongClickListener;
        private BiConsumer<View, KeyConfig> buttonCallback;
        private BiConsumer<View, View> progressBarCallback;
        private boolean drawProgressBar = true;

        public Builder setContext(FragmentActivity context) {
            this.context = context;
            return this;
        }

        public Builder setKeys(ImmutableList<ImmutableList<KeyConfig>> keys) {
            this.keys = keys;
            return this;
        }

        public Builder setButtonOnClickListener(View.OnClickListener buttonOnClickListener) {
            this.buttonOnClickListener = buttonOnClickListener;
            return this;
        }

        public Builder setButtonLongClickListener(View.OnLongClickListener buttonLongClickListener) {
            this.buttonLongClickListener = buttonLongClickListener;
            return this;
        }

        public Builder setButtonCallback(BiConsumer<View, KeyConfig> buttonCallback) {
            this.buttonCallback = buttonCallback;
            return this;
        }

        public Builder setProgressBarCallback(BiConsumer<View, View> progressBarCallback) {
            this.progressBarCallback = progressBarCallback;
            return this;
        }

        public Builder setRootView(LinearLayout rootView) {
            this.rootView = rootView;
            return this;
        }

        public Builder setDrawProgressBar(boolean drawProgressBar) {
            this.drawProgressBar = drawProgressBar;
            return this;
        }

        public DynamicKeyboard build() {
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(keys);
            if (drawProgressBar) {
                Preconditions.checkNotNull(progressBarCallback);
            }
            Preconditions.checkNotNull(buttonCallback);
            return new DynamicKeyboard(context, keys, buttonOnClickListener, buttonLongClickListener, buttonCallback, progressBarCallback, rootView, drawProgressBar);
        }
    }

    public void buildAtRoot() {
        for (ImmutableList<KeyConfig> row : keys) {
            LinearLayout curRow = new LinearLayout(context);
            curRow.setOrientation(LinearLayout.HORIZONTAL);
            float weightSum = calcWeightSumForRow(row);
            curRow.setWeightSum(weightSum);
            for (KeyConfig keyConfig : row) {
                Float weight = keyConfig.weight;
                String keyName = keyConfig.textName;
                if (keyConfig.type == KeyConfig.KeyType.HALF_SPACE) {
                    Space space = new Space(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                    space.setLayoutParams(params);
                    curRow.addView(space);
                } else {
                    keyName = keyName.toUpperCase();
                    LinearLayout buttonProgressBarContainer = new LinearLayout(context);
                    LinearLayout.LayoutParams buttonProgressBarContainerParams = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                    buttonProgressBarContainer.setLayoutParams(buttonProgressBarContainerParams);
                    buttonProgressBarContainer.setOrientation(LinearLayout.VERTICAL);

                    View button = makeButton(keyName, keyConfig);
                    buttonProgressBarContainer.addView(button);

                    if (drawProgressBar && keyConfig.isPlayable) {
                        View progressBar = makeProgressBar(button, keyName);
                        buttonProgressBarContainer.addView(progressBar);
                    }
                    curRow.addView(buttonProgressBarContainer);
                }
            }
            rootView.addView(curRow);
        }
    }

    private String getButtonIdName(String letter) {
        return "key" + buttonLetterToIdName(letter);
    }

    private String getProgressBarIdName(String letter) {
        return "progressBarForKey" + buttonLetterToIdName(letter);
    }

    public View getViewFromKeyText(String letter) {
        String buttonName = getButtonIdName(letter);
        int buttonId = context.getResources().getIdentifier(buttonName, "id", context.getApplicationContext().getPackageName());
        return rootView.findViewById(buttonId);
    }

    public ProgressDots getLetterProgressBar(String letter) {
        String progressBarIdName = getProgressBarIdName(letter);
        int progressBarId = context.getResources().getIdentifier(progressBarIdName, "id", context.getApplicationContext().getPackageName());
        return rootView.findViewById(progressBarId);
    }

    public static String getButtonLetter(Context context, View v) {
        String buttonId = context.getResources().getResourceEntryName(v.getId());
        if (!buttonId.startsWith("key")) {
            throw new RuntimeException("unknown button " + buttonId);
        }

        return idNameToButtonLetter(buttonId.replace("key", ""));
    }

    public String getButtonLetter(View v) {
        return getButtonLetter(context, v);
    }


    private static String idNameToButtonLetter(String idName) {
        if (idName.equals("SLASH")) {
            return "/";
        } else if (idName.equals("PERIOD")) {
            return ".";
        } else if (idName.equals("COMMA")) {
            return ",";
        } else if (idName.equals("QUESTION")) {
            return "?";
        } else if (idName.equals("EQUALS")) {
            return "=";
        } else {
            return idName;
        }
    }

    private static String buttonLetterToIdName(String buttonLetter) {
        if (buttonLetter.equals("/")) {
            return "SLASH";
        } else if (buttonLetter.equals(",")) {
            return "COMMA";
        } else if (buttonLetter.equals(".")) {
            return "PERIOD";
        } else if (buttonLetter.equals("?")) {
            return "QUESTION";
        } else if (buttonLetter.equals("=")) {
            return "EQUALS";
        } else {
            return buttonLetter;
        }
    }

    private float calcWeightSumForRow(ImmutableList<KeyConfig> row) {
        float total = 0;
        for (KeyConfig pair : row) {
            total += pair.weight;
        }
        return total;
    }

    private View makeButton(String keyName, KeyConfig keyConfig) {
        View button;
        if (keyConfig.type == KeyConfig.KeyType.DELETE_KEY) {
            ImageButton deleteKey = new ImageButton(context);
            Drawable drawable = context.getDrawable(R.drawable.ic_backspace);
            deleteKey.setImageDrawable(drawable);
            button = deleteKey;
        } else if (keyConfig.type == KeyConfig.KeyType.SPACE_KEY) {
            ImageButton spaceKey = new ImageButton(context);
            Drawable drawable = context.getDrawable(R.drawable.ic_space_bar);
            spaceKey.setImageDrawable(drawable);
            button = spaceKey;
        } else {
            button = new Button(context);
            ((Button)button).setText(keyName);
        }
        button.setHapticFeedbackEnabled(true);

        int buttonId = context.getResources().getIdentifier(getButtonIdName(keyName), "id", context.getPackageName());
        button.setId(buttonId);

        if (keyConfig.isPlayable) {
            button.setTag("inplay");
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(layoutParams);

        if (buttonLongClickListener != null) {
            button.setOnClickListener(v -> {
                button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                buttonOnClickListener.onClick(v);
            });
        }
        button.setOnLongClickListener(this.buttonLongClickListener);

        this.buttonCallback.accept(button, keyConfig);

        return button;
    }

    private View makeProgressBar(View button, String keyName) {
        int density= context.getResources().getDisplayMetrics().densityDpi;
        ProgressDots progressBar;
        progressBar = new ProgressDots(context, null, density);
        progressBar.setTag("progressBar");

        int progressBarId = context.getResources().getIdentifier(getProgressBarIdName(keyName), "id", context.getPackageName());
        progressBar.setId(progressBarId);
        int dimension = Math.round(context.getResources().getDimension(R.dimen.progressBarHeight));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dimension);
        int topMargin = Math.round(context.getResources().getDimension(R.dimen.progressBarTopMargin));
        int startEndMargin = Math.round(context.getResources().getDimension(R.dimen.progressBarStartEndMargin));
        layoutParams.setMargins(startEndMargin, topMargin, startEndMargin, 0);

        this.progressBarCallback.accept(button, progressBar);

        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }
}
