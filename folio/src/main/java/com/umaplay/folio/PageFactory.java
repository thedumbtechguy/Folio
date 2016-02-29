package com.umaplay.folio;

import android.support.annotation.NonNull;

import com.umaplay.folio.animator.PageAnimatorFactory;

import java.io.Serializable;

/**
 * Interface for the creation of PageFactory instances
 */
public interface PageFactory extends Serializable {
    Page getPage();
    void setOutAnimator(PageAnimatorFactory outPageAnimatorFactory);
    PageAnimatorFactory getOutAnimator();
}
