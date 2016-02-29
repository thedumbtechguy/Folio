package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by user on 2/28/2016.
 */
public class FadeOutAnimator implements PageAnimatorFactory {
    @Override
    public Animator createAnimator(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setDuration(300);

        return anim;
    }
}
