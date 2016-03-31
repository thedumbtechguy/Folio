package com.umaplay.folio;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.animator.PageAnimatorFactory;

import java.io.Serializable;

/**
 * Created by user on 2/27/2016.
 */
public abstract class Page implements Serializable {

    private static final String TAG = "Folio::Page";

    private PageManager mPageManager;
    private PageManager.NestedPageManager mNestedPageManager;
    private boolean mIsMounted;
    private boolean mHasFocus;
    private boolean mIsVisible;
    private PageAnimatorFactory mAnimatorFactory;
    private View mView;
    private Bundle mDeferredPageState;
    private SparseArray<Parcelable> mDeferredViewState;
    private String mPageId;

    @CallSuper
    public void onCreate() {
        mIsMounted = false;
        Log.d(TAG, this.getClass().getName() + "::onCreate");
    }

    public abstract View onCreateView(Context context, ViewGroup container);

    @CallSuper
    public void onViewMounted(View view) {
        mIsMounted = true;
        mView = view;
        Log.d(TAG, this.getClass().getName() + "::onViewMounted");
    }

    @CallSuper
    public void onRestoreState(Bundle bundle) {
        if (peekNestedPageManager() != null) {
            peekNestedPageManager().onRestoreInstanceState(bundle);
        }
        else
            mDeferredPageState = bundle;//let's save it for later

        //user might need to manually restore view state later
        this.mDeferredViewState = bundle.getSparseParcelableArray("PageManager.VIEW_STATE_KEY");

        Log.d(TAG, this.getClass().getName() + "::onRestoreState");
    }

    @CallSuper
    public void onPageIsVisible() {
        mIsVisible = true;
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onStart();
        Log.d(TAG, this.getClass().getName() + "::onPageIsVisible");
    }

    @CallSuper
    public void onPageHasFocus() {
        mHasFocus = true;
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onResume();
        Log.d(TAG, this.getClass().getName() + "::onPageHasFocus");
    }

    @CallSuper
    public void onPageLostFocus() {
        mHasFocus = false;
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onPause();
        Log.d(TAG, this.getClass().getName() + "::onPageLostFocus");
    }

    @CallSuper
    public void onPageIsInvisible() {
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onStop();
        mIsVisible = false;
        Log.d(TAG, this.getClass().getName() + "::onPageIsInvisible");
    }

    @CallSuper
    public void onSaveState(Bundle outBundle) {
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onSaveInstanceState(outBundle);

        Log.d(TAG, this.getClass().getName() + "::onSaveState");
    }

    @CallSuper
    public void onViewUnmounted() {
        mIsMounted = false;
        mView = null;
        Log.d(TAG, this.getClass().getName() + "::onViewUnmounted");
    }

    @CallSuper
    public void onDestroy() {
        if (peekNestedPageManager() != null)
            peekNestedPageManager().onDestroy();

        mIsMounted = false;
        mDeferredPageState = null;
        mPageManager = null;
        mNestedPageManager = null;
        mAnimatorFactory = null;
        Log.d(TAG, this.getClass().getName() + "::onDestroy");
    }


    public boolean isMounted() {
        return mIsMounted;
    }
    public boolean isVisible() {
        return mIsVisible;
    }
    public boolean hasFocus() {
        return mHasFocus;
    }

    public View getView() {
        return mView;
    }

    public void setPageManager(PageManager stack) {
        mPageManager = stack;
    }
    public PageManager getPageManager() {
        return mPageManager;
    }

    public PageManager.NestedPageManager peekNestedPageManager() {
        return mNestedPageManager;
    }
    public PageManager.NestedPageManager getNestedPageManager(ViewGroup container) {
        if(!isMounted()) throw new IllegalStateException("Page has not been mounted");

        if(peekNestedPageManager() == null) {
            mNestedPageManager = mPageManager.getNestedPageManager(container, mDeferredPageState);
            if(isVisible())
                mNestedPageManager.onStart();
            if(hasFocus())
                mNestedPageManager.onResume();

            mDeferredPageState = null;
        }

        return mNestedPageManager;
    }

    public void setAnimatorFactory(PageAnimatorFactory outPageAnimatorFactory) {
        mAnimatorFactory = outPageAnimatorFactory;
    }
    public PageAnimatorFactory getAnimatorFactory() {
        return mAnimatorFactory;
    }

    public Context getContext() {
        if(!isMounted()) throw new IllegalStateException("Page has not been mounted");

        return getView().getContext();
    }

    protected View inflate(@LayoutRes int layout, Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(layout, container, false);
    }

    public void setId(String id) {
        this.mPageId = id;
    }

    public String getId() {
        return mPageId;
    }


    /**
     * This allows a page to restore the state of a page manually
     * This is especially useful for pages with scrolling views like lists or recycler to
     * remember the scroll position as the PageManager has no mechanism to detect exactly when
     * you load the list items.
     * You can call this safely each time you load the list but it will only work on the first call
     *
     */
    protected void restoreViewState() {
        if(isMounted() && mDeferredViewState != null) {
            getView().restoreHierarchyState(mDeferredViewState);
            mDeferredViewState = null;
        }
    }

//      Lifecycle
//    void onCreate();
//    View onCreateView(Context context, ViewGroup container);
//    void onViewMounted(View view);
//    void onRestoreState();
//    void onPageIsVisible();
//    void onPageHasFocus();
//    void onPageLostFocus();
//    void onPageIsInvisible();
//    void onSaveState();
//    void onViewUnmounted();
//    void onDestroy();
}
