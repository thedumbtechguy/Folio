package com.umaplay.folio.animator;

import android.animation.Animator;
import android.view.View;

/**
 * Created by user on 2/28/2016.
 */
public class NoAnimationFactory implements PageAnimatorFactory {
    @Override
    public Animator createInAnimator(View view) {
        return AnimatorUtils.createNoAnimationAnimator(view);
    }

    @Override
    public Animator createOutAnimator(View view) {
        return AnimatorUtils.createNoAnimationAnimator(view);
    }

    @Override
    public void undoOutAnimation(View view) {
        //do nothing
    }
}
