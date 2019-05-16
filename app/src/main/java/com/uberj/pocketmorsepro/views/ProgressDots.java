package com.uberj.pocketmorsepro.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.uberj.pocketmorsepro.ProgressGradient;
import com.uberj.pocketmorsepro.R;

public class ProgressDots extends ConstraintLayout {
    private final boolean highDensity;
    private Boolean showSegments = false;
    private View progressSegment0;
    private View progressSegment1;
    private View progressSegment2;
    private View progressSegment3;
    private View progressSegment4;
    private View progressSegment5;
    private View progressSegment6;
    private View progressSegment7;

    public ProgressDots(Context context, @Nullable AttributeSet attrs, int density) {
        super(context, attrs);
        if (density >= 240) {
            // Enough density
            highDensity = true;
        } else {
            // Low density
            highDensity = false;
        }
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.progress_dots, this);
        progressSegment0 = findViewById(R.id.progressSegment0);
        progressSegment1 = findViewById(R.id.progressSegment1);
        progressSegment2 = findViewById(R.id.progressSegment2);
        progressSegment3 = findViewById(R.id.progressSegment3);
        progressSegment4 = findViewById(R.id.progressSegment4);
        progressSegment5 = findViewById(R.id.progressSegment5);
        progressSegment6 = findViewById(R.id.progressSegment6);
        progressSegment7 = findViewById(R.id.progressSegment7);
        if (!highDensity) {
            progressSegment5.setVisibility(GONE);
            progressSegment6.setVisibility(GONE);
            progressSegment7.setVisibility(GONE);
        }

        if (showSegments) {
            setCompetencyWeight(0);
        } else {
            setAllInvisible();
        }

    }

    public void setShowSegments(Boolean showSegments) {
        this.showSegments = showSegments;
    }

    public Boolean getShowSegments() {
        return showSegments;
    }

    private void setAllInvisible() {
        progressSegment0.setVisibility(INVISIBLE);
        progressSegment1.setVisibility(INVISIBLE);
        progressSegment2.setVisibility(INVISIBLE);
        progressSegment3.setVisibility(INVISIBLE);
        progressSegment4.setVisibility(INVISIBLE);
        if (highDensity) {
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        }
    }

    public void setCompetencyWeight(int competencyWeight) {
        if (highDensity) {
            setHighDensityCompetencyWeight(competencyWeight);
        } else {
            setLowDensityCompetencyWeight(competencyWeight);
        }
    }

    private void setLowDensityCompetencyWeight(int competencyWeight) {
        if (competencyWeight < 5) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(INVISIBLE);
            progressSegment2.setVisibility(INVISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
        } else if (competencyWeight < 20) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(INVISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
        } else if (competencyWeight < 30) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
        } else if (competencyWeight < 40) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
        } else if (competencyWeight >= 50) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(VISIBLE);
        }

        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressSegment0.setBackgroundColor(color);
        progressSegment1.setBackgroundColor(color);
        progressSegment2.setBackgroundColor(color);
        progressSegment3.setBackgroundColor(color);
        progressSegment4.setBackgroundColor(color);
    }

    private void setHighDensityCompetencyWeight(int competencyWeight) {
        if (competencyWeight < 5) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(INVISIBLE);
            progressSegment2.setVisibility(INVISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 10) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(INVISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 15) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(INVISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 20) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(INVISIBLE);
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 25) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(VISIBLE);
            progressSegment5.setVisibility(INVISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 30) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(VISIBLE);
            progressSegment5.setVisibility(VISIBLE);
            progressSegment6.setVisibility(INVISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight < 40) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(VISIBLE);
            progressSegment5.setVisibility(VISIBLE);
            progressSegment6.setVisibility(VISIBLE);
            progressSegment7.setVisibility(INVISIBLE);
        } else if (competencyWeight >= 50) {
            progressSegment0.setVisibility(VISIBLE);
            progressSegment1.setVisibility(VISIBLE);
            progressSegment2.setVisibility(VISIBLE);
            progressSegment3.setVisibility(VISIBLE);
            progressSegment4.setVisibility(VISIBLE);
            progressSegment5.setVisibility(VISIBLE);
            progressSegment6.setVisibility(VISIBLE);
            progressSegment7.setVisibility(VISIBLE);
        }

        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressSegment0.setBackgroundColor(color);
        progressSegment1.setBackgroundColor(color);
        progressSegment2.setBackgroundColor(color);
        progressSegment3.setBackgroundColor(color);
        progressSegment4.setBackgroundColor(color);
        progressSegment5.setBackgroundColor(color);
        progressSegment6.setBackgroundColor(color);
        progressSegment7.setBackgroundColor(color);
    }
}
