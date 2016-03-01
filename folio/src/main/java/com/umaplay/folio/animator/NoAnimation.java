package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by user on 2/28/2016.
 */
public class NoAnimation implements PageAnimatorFactory {
    @Override
    public Animator createAnimator(View view) {
        Animator anim = ValueAnimator.ofInt(0, 1);
        anim.setDuration(1);

        return anim;
    }
}
