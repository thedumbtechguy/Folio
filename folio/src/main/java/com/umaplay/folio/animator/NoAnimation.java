package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;

/**
 * Created by user on 2/28/2016.
 */
public class NoAnimation implements PageAnimatorFactory {
    @Override
    public Animator createAnimator(View view) {
        return new Animator() {
            @Override
            public long getStartDelay() {
                return 0;
            }

            @Override
            public void setStartDelay(long startDelay) {

            }

            @Override
            public Animator setDuration(long duration) {
                return null;
            }

            @Override
            public long getDuration() {
                return 0;
            }

            @Override
            public void setInterpolator(TimeInterpolator value) {

            }

            @Override
            public boolean isRunning() {
                return false;
            }
        };
    }
}
