package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;

/**
 * Created by user on 2/28/2016.
 */
public class NoAnimation implements PageAnimatorFactory {
    @Override
    public Animator createAnimator(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 1f);
        anim.setDuration(1);

        return anim;
    }
}
