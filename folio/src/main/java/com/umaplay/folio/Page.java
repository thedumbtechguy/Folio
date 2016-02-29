package com.umaplay.folio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.animator.PageAnimatorFactory;

import java.io.Serializable;

/**
 * Interface for deferred creation of View instances.
 */
public interface Page extends Serializable {
    void onPageWillMount();
    void onPageMounted(View view);
    void onPageIsVisible();
    void onPageHasFocus();
    void onPageLostFocus();
    void onPageIsInvisible();
    void onPageWillUnMount();
    View onCreateView(Context context, ViewGroup container);

    boolean isMounted();
    boolean isVisible();
    boolean hasFocus();

    void setView(View view);
    View getView();

    void setPageManager(PageManager stack);
    PageManager getPageManager();

    PageManager peekNestedPageManager();
    PageManager getNestedPageManager(ViewGroup container);

    void setOutAnimator(PageAnimatorFactory outPageAnimatorFactory);
    PageAnimatorFactory getOutAnimator();

    Context getContext();
}
