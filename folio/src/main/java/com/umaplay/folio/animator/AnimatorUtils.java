package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by user on 3/1/2016.
 */
public class AnimatorUtils {
    private static DisplayMetrics metrics;

    public static DisplayMetrics getDisplayMetrics(Context context) {
        if(metrics == null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
        }

        return metrics;
    }

    public static Animator createNoAnimationAnimator(View view) {
        ValueAnimator anim = ValueAnimator.ofInt(0, 1);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(1);

        return anim;
    }

    public static Animator createFadeInAnimator(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(250);

        return anim;
    }

    public static Animator createFadeOutAnimator(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(250);

        return anim;
    }

    public static Animator createSlideInFromBottomAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", metrics.heightPixels, 0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideOutToBottomAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", 0f, metrics.heightPixels);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideInFromLeftAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX", -metrics.widthPixels, 0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideOutToLeftAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX", 0f, -metrics.widthPixels);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideInFromRightAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX", metrics.widthPixels, 0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideOutToRightAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX", 0f, metrics.widthPixels);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideInFromTopAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", -metrics.heightPixels,
                0f);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }

    public static Animator createSlideOutToTopAnimator(View view) {
        DisplayMetrics metrics = AnimatorUtils.getDisplayMetrics(view.getContext());

        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", 0f, -metrics.heightPixels);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(300);

        return anim;
    }


    public static void undoSlideUpDown(View view) {
        view.setTranslationY(0f);
    }

    public static void undoFade(View view) {
        view.setAlpha(1f);
    }

    public static void undoSlideLeftRight(View view) {
        view.setTranslationX(0f);
    }
}
