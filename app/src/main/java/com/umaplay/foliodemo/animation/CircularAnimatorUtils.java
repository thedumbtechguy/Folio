package com.umaplay.foliodemo.animation;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;


public class CircularAnimatorUtils {

    public static Animator createInAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius)
                .setDuration(300);
    }

    public static Animator createOutAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animation (the final radius is zero)
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0)
                .setDuration(300);
    }

    public static void undoOutAnimation(View view) {
        //todo: reset the clipping
    }
}
