package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;

/**
 * Interface for creating Animator instances for goTo() and goBack() transitions.
 */
public interface PageAnimatorFactory {
    Animator createAnimator(View view);
}
