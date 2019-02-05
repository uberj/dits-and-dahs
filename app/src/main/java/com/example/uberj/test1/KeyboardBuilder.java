package com.example.uberj.test1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

public class KeyboardBuilder {

    private final Context context;
    private ImmutableList<ImmutableList<Pair<Float, String>>> keys;

    public KeyboardBuilder(Context context, ImmutableList<ImmutableList<Pair<Float, String>>> keys) {
        this.context = context;
        this.keys = keys;
    }

    public void buildAtRoot(LinearLayout rootView) {
        for (ImmutableList<Pair<Float, String>> row : keys) {
            LinearLayout curRow = new LinearLayout(context);
            curRow.setOrientation(LinearLayout.HORIZONTAL);
            float weightSum = calcWeightSumForRow(row);
            curRow.setWeightSum(weightSum);
            for (Pair<Float, String> cell : row) {
                Float weight = cell.getLeft();
                String keyName = cell.getRight();
                if (keyName == null) {
                    Space space = new Space(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                    space.setLayoutParams(params);
                    curRow.addView(space);
                } else {
                    LinearLayout buttonProgressBarContainer = new LinearLayout(context);
                    LinearLayout.LayoutParams buttonProgressBarContainerParams = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                    buttonProgressBarContainer.setLayoutParams(buttonProgressBarContainerParams);
                    buttonProgressBarContainer.setOrientation(LinearLayout.VERTICAL);

                    Button button = makeButton(keyName);
                    buttonProgressBarContainer.addView(button);

                    View progressBar = makeProgressBar(keyName);
                    buttonProgressBarContainer.addView(progressBar);
                    curRow.addView(buttonProgressBarContainer);
                }
            }
            rootView.addView(curRow);
        }
    }

    private float calcWeightSumForRow(ImmutableList<Pair<Float, String>> row) {
        float total = 0;
        for (Pair<Float, String> pair : row) {
            total += pair.getLeft();
        }
        return total;
    }

    private Button makeButton(String keyName) {
        Button button = new Button(context);
        /* <Button
            android:onClick="keyboardButtonClicked"
            android:tag="inplay"
            android:text="1" />
         */
        // android:id="@+id/key1"
        int buttonId = context.getResources().getIdentifier("key" + keyName, "id", context.getPackageName());
        button.setId(buttonId);

        // android:text="1"
        button.setText(keyName);
        // android:tag="inplay"
        button.setTag("inplay");

        // android:layout_width="match_parent"
        // android:layout_height="wrap_content"
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(layoutParams);

        //button.setOnClickListener(() -> todo);
        //button.setOnLongClickListener(() -> todo);

        return button;
    }

    private View makeProgressBar(String keyName) {
        View progressBar = new View(context);
        progressBar.setTag("progressBar");

        /* <View android:tag="progressbar"
                android:layout_marginTop="@dimen/progressBarTopMargin"
                android:layout_marginStart="@dimen/progressBarStartEndMargin"
                android:layout_marginEnd="@dimen/progressBarStartEndMargin"
                android:layout_width="match_parent"
                android:layout_height="@dimen/progressBarHeight"
                android:background="@drawable/progress_bar"/>
         */

        // android:id="@+id/progressBarForKey73"
        int progressBarId = context.getResources().getIdentifier("progressBarForKey" + keyName, "id", context.getPackageName());
        progressBar.setId(progressBarId);
        // android:layout_width="match_parent"
        // android:layout_height="@dimen/progressBarHeight"
        int dimension = Math.round(context.getResources().getDimension(R.dimen.progressBarHeight));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dimension);
        int topMargin = Math.round(context.getResources().getDimension(R.dimen.progressBarTopMargin));
        int startEndMargin = Math.round(context.getResources().getDimension(R.dimen.progressBarStartEndMargin));
        /*
                android:layout_marginTop="@dimen/progressBarTopMargin"
                android:layout_marginStart="@dimen/progressBarStartEndMargin"
                android:layout_marginEnd="@dimen/progressBarStartEndMargin"
         */
        layoutParams.setMargins(startEndMargin, topMargin, startEndMargin, 0);

        // android:background="@drawable/progress_bar"/>
        progressBar.setBackground(context.getResources().getDrawable(R.drawable.progress_bar, context.getTheme()));

        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }
}
