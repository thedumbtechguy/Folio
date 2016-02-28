package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by user on 2/28/2016.
 */
public class FadeInAnimator implements PageAnimatorFactory {
    @Override
    public Animator createAnimator(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        anim.setDuration(500);

        return anim;
    }
}
