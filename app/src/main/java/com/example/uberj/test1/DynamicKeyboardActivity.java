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

import org.apache.commons.lang3.tuple.Pair;

/*
This class is for exploring dynamicly creating a keyboard at runtime using the java layout apis
 */
public class DynamicKeyboardActivity extends Activity {
    private static final Pair<Float, String> HSM = Pair.of(0.5f, null);
    ImmutableList<ImmutableList<Pair<Float, String>>> keys = ImmutableList.of(
            ImmutableList.of(s1("1"), s1("2"), s1("3"), s1("4"), s1("5"), s1("6"), s1("7"), s1("8"), s1("9"), s1("0"), s1("/"), s1("=")),
            ImmutableList.of(HSM, s1("q"), s1("w"), s1("e"), s1("r"), s1("t"), s1("y"), s1("u"), s1("i"), s1("o"), s1("p"), s1("."), HSM),
            ImmutableList.of(HSM, HSM, s1("a"), s1("s"), s1("d"), s1("f"), s1("g"), s1("h"), s1("j"), s1("k"), s1("l"), s1("?"), HSM, HSM),
            ImmutableList.of(HSM, HSM, HSM, s1("z"), s1("x"), s1("c"), s1("v"), s1("b"), s1("n"), s1("m"), s1(","), s1("."), HSM, HSM, HSM)
    );

    private Pair<Float, String> s1(String s) {
        return Pair.of(1f, s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity_v2);
        LinearLayout rootView = findViewById(R.id.dynamic_keyboard);
        for (ImmutableList<Pair<Float, String>> row : keys) {
            LinearLayout curRow = new LinearLayout(this);
            curRow.setOrientation(LinearLayout.HORIZONTAL);
            float weightSum = calcWeightSumForRow(row);
            curRow.setWeightSum(weightSum);
            for (Pair<Float, String> cell : row) {
                Float weight = cell.getLeft();
                String keyName = cell.getRight();
                if (keyName == null) {
                    Space space = new Space(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                    space.setLayoutParams(params);
                    curRow.addView(space);
                } else {
                    LinearLayout buttonProgressBarContainer = new LinearLayout(this);
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
