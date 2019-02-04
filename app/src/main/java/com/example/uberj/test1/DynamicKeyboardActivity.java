package com.example.uberj.test1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

/*
This class is for exploring dynamicly creating a keyboard at runtime using the java layout apis
 */
public class DynamicKeyboardActivity extends Activity {
    private static final String HSM = "HALF_SPACE_MARKER";
    ImmutableList<ImmutableList<String>> keys = ImmutableList.of(
                           ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "/", "="),
                      ImmutableList.of(HSM, "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", ".", HSM),
                 ImmutableList.of(HSM, HSM, "a", "s", "d", "f", "g", "h", "j", "k", "l", "?", HSM, HSM),
            ImmutableList.of(HSM, HSM, HSM, "z", "x", "c", "v", "b", "n", "m", ",", ".", HSM, HSM, HSM)
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity_v2);
        LinearLayout rootView = findViewById(R.id.dynamic_keyboard);
        for (ImmutableList<String> row : keys) {
            LinearLayout curRow = new LinearLayout(this);
            curRow.setOrientation(LinearLayout.HORIZONTAL);
            float weightSum = calcWeightSumForRow(row);
            curRow.setWeightSum(weightSum);
            for (String keyName : row) {
                if (keyName.equals(HSM)) {
                    Space space = new Space(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
                    space.setLayoutParams(params);
                    curRow.addView(space);
                } else {
                    LinearLayout buttonProgressBarContainer = new LinearLayout(this);
                    LinearLayout.LayoutParams buttonProgressBarContainerParams = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
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

    private float calcWeightSumForRow(ImmutableList<String> row) {
        float total = 0;
        for (String s : row) {
            if (s.equals(HSM)) {
                total += 0.5;
            } else {
                total += 1;
            }
        }

        return total;
    }

    private Button makeButton(String keyName) {
        Button button = new Button(this);
        /* <Button
            android:onClick="keyboardButtonClicked"
            android:tag="inplay"
            android:text="1" />
         */
        // android:id="@+id/key1"
        int buttonId = getResources().getIdentifier("key" + keyName, "id", this.getPackageName());
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
        View progressBar = new View(this);
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
        int progressBarId = getResources().getIdentifier("progressBarForKey" + keyName, "id", this.getPackageName());
        progressBar.setId(progressBarId);
        // android:layout_width="match_parent"
        // android:layout_height="@dimen/progressBarHeight"
        int dimension = Math.round(getResources().getDimension(R.dimen.progressBarHeight));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dimension);
        int topMargin = Math.round(getResources().getDimension(R.dimen.progressBarTopMargin));
        int startEndMargin = Math.round(getResources().getDimension(R.dimen.progressBarStartEndMargin));
        /*
                android:layout_marginTop="@dimen/progressBarTopMargin"
                android:layout_marginStart="@dimen/progressBarStartEndMargin"
                android:layout_marginEnd="@dimen/progressBarStartEndMargin"
         */
        layoutParams.setMargins(startEndMargin, topMargin, startEndMargin, 0);

        // android:background="@drawable/progress_bar"/>
        progressBar.setBackground(getResources().getDrawable(R.drawable.progress_bar, getTheme()));

        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }
}
