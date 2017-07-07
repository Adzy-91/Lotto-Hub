package ie.adampurser.lottohub;


import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.daimajia.numberprogressbar.NumberProgressBar;

public class ProgressBarAnimation extends Animation {
    private NumberProgressBar mProgressBar;
    private float mFrom;
    private float mTo;

    public ProgressBarAnimation(NumberProgressBar progressBar, float from, float to) {
        mProgressBar = progressBar;
        mFrom = from;
        mTo = to;
        this.setDuration(1000);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float progress = mFrom + (mTo - mFrom) * interpolatedTime;
        mProgressBar.setProgress((int) progress);
    }
}
