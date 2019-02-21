package com.example.uberj.test1;

import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import com.example.uberj.test1.keyboards.KeyConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DynamicKeyboard {

    private final FragmentActivity context;
    private final ImmutableList<ImmutableList<KeyConfig>> keys;
    private final Button.OnClickListener buttonOnClickListener;
    private final Button.OnLongClickListener buttonLongClickListener;
    private final Consumer<Button> buttonCallback;
    private final BiConsumer<Button, View> progressBarCallback;

    public DynamicKeyboard(FragmentActivity context, ImmutableList<ImmutableList<KeyConfig>> keys, View.OnClickListener buttonOnClickListener, View.OnLongClickListener buttonLongClickListener, Consumer<Button> buttonCallback, BiConsumer<Button, View> progressBarCallback) {
        this.context = context;
        this.keys = keys;
        this.buttonOnClickListener = buttonOnClickListener;
        this.buttonLongClickListener = buttonLongClickListener;
        this.buttonCallback = buttonCallback;
        this.progressBarCallback = progressBarCallback;
    }

    public static final class Builder {
        private FragmentActivity context;
        private ImmutableList<ImmutableList<KeyConfig>> keys;
        private View.OnClickListener buttonOnClickListener;
        private View.OnLongClickListener buttonLongClickListener;
        private Consumer<Button> buttonCallback;
        private BiConsumer<Button, View> progressBarCallback;

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

        public Builder setButtonCallback(Consumer<Button> buttonCallback) {
            this.buttonCallback = buttonCallback;
            return this;
        }

        public Builder setProgressBarCallback(BiConsumer<Button, View> progressBarCallback) {
            this.progressBarCallback = progressBarCallback;
            return this;
        }

        public DynamicKeyboard createKeyboardBuilder() {
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(keys);
            Preconditions.checkNotNull(progressBarCallback);
            Preconditions.checkNotNull(buttonCallback);
            return new DynamicKeyboard(context, keys, buttonOnClickListener, buttonLongClickListener, buttonCallback, progressBarCallback);
        }
    }

    public void buildAtRoot(LinearLayout rootView) {
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

                    Button button = makeButton(keyName, keyConfig);
                    buttonProgressBarContainer.addView(button);

                    if (keyConfig.isPlayable) {
                        View progressBar = makeProgressBar(button, keyName);
                        buttonProgressBarContainer.addView(progressBar);
                        curRow.addView(buttonProgressBarContainer);
                    }
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

    public View getLetterProgressBar(String letter) {
        String progressBarIdName = getProgressBarIdName(letter);
        int progressBarId = context.getResources().getIdentifier(progressBarIdName, "id", context.getApplicationContext().getPackageName());
        return context.findViewById(progressBarId);
    }

    public String getButtonLetter(View v) {
        String buttonId = context.getResources().getResourceEntryName(v.getId());
        if (!buttonId.startsWith("key")) {
            throw new RuntimeException("unknown button " + buttonId);
        }

        return idNameToButtonLetter(buttonId.replace("key", ""));
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

    private Button makeButton(String keyName, KeyConfig keyConfig) {
        Button button = new Button(context);
        int buttonId = context.getResources().getIdentifier(getButtonIdName(keyName), "id", context.getPackageName());
        button.setId(buttonId);

        button.setText(keyName);
        if (keyConfig.isPlayable) {
            button.setTag("inplay");
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(layoutParams);

        button.setOnClickListener(this.buttonOnClickListener);
        button.setOnLongClickListener(this.buttonLongClickListener);

        this.buttonCallback.accept(button);

        return button;
    }

    private View makeProgressBar(Button button, String keyName) {
        View progressBar = new View(context);
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

        progressBar.setBackground(context.getResources().getDrawable(R.drawable.progress_bar, context.getTheme()));


        this.progressBarCallback.accept(button, progressBar);

        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }
}
