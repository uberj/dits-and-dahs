package com.uberj.pocketmorsepro;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentActivity;

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

import org.apache.commons.lang3.tuple.Pair;

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
            Preconditions.checkNotNull(rootView);
            Preconditions.checkNotNull(keys);
            if (drawProgressBar) {
                Preconditions.checkNotNull(progressBarCallback);
            }
            Preconditions.checkNotNull(buttonCallback);
            return new DynamicKeyboard(context, keys, buttonOnClickListener, buttonLongClickListener, buttonCallback, progressBarCallback, rootView, drawProgressBar);
        }
    }

    public void buildConstraintLayoutAtRoot() {
        ImmutableList.Builder<ImmutableList<Pair<View, Float>>> views = ImmutableList.builder();
        int spaceCount = 0;
        for (ImmutableList<KeyConfig> row : keys) {
            ImmutableList.Builder<Pair<View, Float>> viewRow = ImmutableList.builder();
            for (KeyConfig keyConfig : row) {
                Float weight = keyConfig.weight;
                String keyName = keyConfig.textName;
                if (keyConfig.type == KeyConfig.KeyType.HALF_SPACE) {
//                    Space space = makeSpace(context, spaceCount);
//                    spaceCount += 1;
//                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    space.setLayoutParams(params);
//                    viewRow.add(Pair.of(space, weight));
                } else {
                    keyName = keyName.toUpperCase();
                    View button = makeButton(keyName, keyConfig);
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, 50);
                    button.setLayoutParams(params);
                    viewRow.add(Pair.of(button, weight));
                }
            }
            views.add(viewRow.build());
        }

        ConstraintLayout constraintLayout = convertToConstraints(views.build());
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.addView(constraintLayout);
    }

    private Space makeSpace(FragmentActivity context, int spaceCount) {
        Space space = new Space(context);
        int spaceId = context.getResources().getIdentifier(getSpaceIdName(spaceCount), "id", context.getPackageName());
        space.setId(spaceId);
        return space;
    }

    private String getSpaceIdName(int spaceCount) {
        if (spaceCount > 11) {
            throw new RuntimeException("I only have 11 space ids. Add more then bump this number");
        }
        return "space" + spaceCount;
    }

    private ConstraintLayout convertToConstraints(ImmutableList<ImmutableList<Pair<View, Float>>> views) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setId(R.id.keys_container);
        ConstraintSet constraintSet = new ConstraintSet();
        addAllViews(constraintLayout, views);
        connectVerticalChains(constraintLayout, constraintSet, views);
        connectHorizontalChains(constraintLayout, constraintSet, views);
        constraintSet.applyTo(constraintLayout);
        return constraintLayout;
    }

    private void addAllViews(ConstraintLayout constraintLayout, ImmutableList<ImmutableList<Pair<View, Float>>> views) {
        for (ImmutableList<Pair<View, Float>> row : views) {
            for (Pair<View, Float> view : row) {
                constraintLayout.addView(view.getLeft());
            }
        }
    }

    private void connectHorizontalChains(ConstraintLayout constraintLayout, ConstraintSet constraintSet, ImmutableList<ImmutableList<Pair<View, Float>>> views) {

        // For each row, align keys and chain
        for (ImmutableList<Pair<View, Float>> row : views) {
            View prev = null;
            for (int i = 0; i < row.size(); i++) {
                View cur = row.get(i).getLeft();
                Float weight = row.get(i).getRight();
                constraintSet.setHorizontalWeight(cur.getId(), weight);
                if (i >= row.size() - 1) {
                    // This is the last element. Connect it to the right of the constraint layout
                    constraintSet.connect(cur.getId(), ConstraintSet.START, prev.getId(), ConstraintSet.END);
                    constraintSet.connect(cur.getId(), ConstraintSet.END, constraintLayout.getId(), ConstraintSet.END);
                    constraintSet.connect(cur.getId(), ConstraintSet.BOTTOM, prev.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(cur.getId(), ConstraintSet.TOP, prev.getId(), ConstraintSet.TOP);

                } else if (i == 0) {
                    // This is the first element. Connect it to the left of constraint layout, bottom to next
                    View next = row.get(i + 1).getLeft();
                    constraintSet.connect(cur.getId(), ConstraintSet.END, next.getId(), ConstraintSet.START);
                    constraintSet.connect(cur.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START);
                } else {
                    // Connect it to the prev right and the next left
                    View next = row.get(i + 1).getLeft();
                    constraintSet.connect(cur.getId(), ConstraintSet.END, next.getId(), ConstraintSet.START);
                    constraintSet.connect(cur.getId(), ConstraintSet.START, prev.getId(), ConstraintSet.END);
                    constraintSet.connect(cur.getId(), ConstraintSet.BOTTOM, prev.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(cur.getId(), ConstraintSet.TOP, prev.getId(), ConstraintSet.TOP);
                }
                prev = cur;
            }
        }
    }

    private void connectVerticalChains(ConstraintLayout constraintLayout, ConstraintSet constraintSet, ImmutableList<ImmutableList<Pair<View, Float>>> views) {
        View prev = null;
        for (int i = 0; i < views.size(); i++) {
            ImmutableList<Pair<View, Float>> row = views.get(i);
            View cur = row.get(0).getLeft();
            // Set first element in row to 'spread_inside'
            constraintSet.setVerticalChainStyle(cur.getId(), ConstraintSet.CHAIN_SPREAD_INSIDE);

            // connect last first to constraint bottom
            if (i >= views.size() - 1) {
                // This is the last element. Connect it to the bottom of the constraint layout. Top to prev
                constraintSet.connect(cur.getId(), ConstraintSet.TOP, prev.getId(), ConstraintSet.BOTTOM);
                constraintSet.connect(cur.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
            } else if (i == 0) {
                // connect first first to constraint top
                // This is the first element. Connect it to the top of constraint layout, bottom to next
                View next = views.get(i + 1).get(0).getLeft();
                constraintSet.connect(cur.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
                constraintSet.connect(cur.getId(), ConstraintSet.BOTTOM, next.getId(), ConstraintSet.TOP);
            } else {
                // Connect it to the prev bottom and the next time
                View next = views.get(i + 1).get(0).getLeft();
                constraintSet.connect(cur.getId(), ConstraintSet.TOP, prev.getId(), ConstraintSet.BOTTOM);
                constraintSet.connect(cur.getId(), ConstraintSet.BOTTOM, next.getId(), ConstraintSet.TOP);
            }
            prev = cur;
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

    public View getLetterProgressBar(String letter) {
        String progressBarIdName = getProgressBarIdName(letter);
        int progressBarId = context.getResources().getIdentifier(progressBarIdName, "id", context.getApplicationContext().getPackageName());
        return rootView.findViewById(progressBarId);
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

        button.setOnClickListener(v -> {
            button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
            buttonOnClickListener.onClick(v);
        });
        button.setOnLongClickListener(this.buttonLongClickListener);

        this.buttonCallback.accept(button, keyConfig);

        return button;
    }

    private View makeProgressBar(View button, String keyName) {
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
