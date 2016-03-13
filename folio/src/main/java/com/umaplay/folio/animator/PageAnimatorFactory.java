package com.umaplay.folio.animator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;

import java.io.Serializable;

/**
 * Interface for creating Animator instances for goTo() and goBack() transitions.
 */
public interface PageAnimatorFactory extends Serializable {
    Animator createInAnimator(View view);
    Animator createOutAnimator(View view);
    void undoOutAnimation(View view);
}
