package com.umaplay.folio;

import android.support.annotation.CallSuper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.animator.PageAnimatorFactory;

/**
 * Created by user on 2/27/2016.
 */
public abstract class BasePage implements Page {

    private static final String TAG = "Folio::BasePage";

    private PageManager mNestedPageManager;
    private PageManager mStack;
    private boolean mIsAnimated;
    private boolean mIsMounted;
    private boolean mHasFocus;
    private boolean mIsVisible;
    private PageAnimatorFactory mOutAnimator;
    private View mView;

    @Override
    @CallSuper
    public void onPageHasFocus() {
        mHasFocus = true;
        Log.d(TAG, this.getClass().getName() + "::onPageHasFocus");
    }

    @Override
    @CallSuper
    public void onPageLostFocus() {
        mHasFocus = false;
        Log.d(TAG, this.getClass().getName() + "::onPageLostFocus");
    }

    @Override
    @CallSuper
    public void onPageWillMount() {
        mIsMounted = false;
        Log.d(TAG, this.getClass().getName() + "::onPageWillMount");
    }

    @Override
    @CallSuper
    public void onPageMounted(View view) {
        mIsMounted = false;
        Log.d(TAG, this.getClass().getName() + "::onPageMounted");
    }

    @Override
    @CallSuper
    public void onPageIsVisible() {
        mIsVisible = true;
        Log.d(TAG, this.getClass().getName() + "::onPageIsVisible");
    }

    @Override
    @CallSuper
    public void onPageIsInvisible() {
        mIsVisible = false;
        Log.d(TAG, this.getClass().getName() + "::onPageIsInvisible");
    }

    @Override
    @CallSuper
    public void onPageWillUnMount() {
        mIsMounted = false;
        Log.d(TAG, this.getClass().getName() + "::onPageWillUnMount");
    }

    @Override
    @CallSuper
    public boolean isMounted() {
        return mIsMounted;
    }

    @Override
    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public boolean hasFocus() {
        return mHasFocus;
    }


    @Override
    @CallSuper
    public void setView(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    @CallSuper
    public void setPageManager(PageManager stack) {
        mStack = stack;
    }

    @Override
    public PageManager getPageManager() {
        return mStack;
    }

    @Override
    public PageManager peekNestedPageManager() {
        return mNestedPageManager;
    }

    @Override
    public PageManager getNestedPageManager(ViewGroup container) {
        if(peekNestedPageManager() != null)
            mNestedPageManager = mStack.getNestedPageManager(container);

        return mNestedPageManager;
    }

    @Override
    public void setAnimated(boolean b) {
        mIsAnimated = b;
    }

    @Override
    public boolean isAnimated() {
        return mIsAnimated;
    }

    @Override
    public void setOutAnimator(PageAnimatorFactory outPageAnimatorFactory) {
        mOutAnimator = outPageAnimatorFactory;
    }

    @Override
    public PageAnimatorFactory getOutAnimator() {
        return mOutAnimator;
    }
}
