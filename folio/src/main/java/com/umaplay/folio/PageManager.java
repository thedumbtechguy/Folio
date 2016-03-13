package com.umaplay.folio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.umaplay.folio.animator.NoAnimationFactory;
import com.umaplay.folio.animator.PageAnimatorFactory;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import static com.umaplay.folio.Preconditions.checkNotNull;

/**
 * This manages a navigation stack by representing each item in the mPageStack as a Page, which is
 * responsible for View creation. All standard Java Stack operations are supported, with additional
 * methods for pushing and popping with animated transitions.
 */
public final class PageManager {

    private static final String STACK_TAG = "STACK_TAG";
    private static final String TAG = "Folio::PageManager";

    private final Stack<Page> mPageStack = new Stack<>();
    private final Stack<PageFactory> mFactoryStack = new Stack<>();
    private final ViewGroup mPageContainer;
    private final PageStackDelegate delegate;
    private final List<StackChangedListener> listeners = new ArrayList<>();
    private boolean mHasStarted;
    private boolean mHasResumed;
    private boolean mDeferNotification;
    private boolean mDontAnimatePop = false;
    private int nesting = 0;

    /**
     * Constructor for PageManager instances
     *
     * @param container Any ViewGroup mPageContainer for navigation Views. Typically a FrameLayout
     * @param delegate  A PageStackDelegate responsible for "finishing" the navigation
     */
    public PageManager(ViewGroup container, PageStackDelegate delegate, Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: " + nesting);
        checkNotNull(container, "mPageContainer == null");
        checkNotNull(delegate, "delegate == null");

        this.mPageContainer = container;
        this.delegate = delegate;

        if(savedInstanceState != null) restoreFromSavedInstanceState(savedInstanceState);
    }

    /**
     * Constructor for child PageManager instances
     * Used internally to give Pages the ability to have nested pages
     *
     * @param container Any ViewGroup mPageContainer for navigation Views. Typically a FrameLayout
     */
    private PageManager(ViewGroup container, int nesting) {
        Log.d(TAG, "onCreateNested: " + nesting);
        checkNotNull(container, "container == null");

        this.mPageContainer = container;
        this.delegate = null;
        this.nesting = nesting;
    }

    public PageManager getNestedPageManager(ViewGroup container) {
        return new PageManager(container, nesting + 1);
    }



    @SuppressWarnings("unchecked")
    protected void restoreFromSavedInstanceState(Bundle bundle) {
        checkNotNull(bundle, "bundle == null");

        Stack<PageFactory> savedStack = (Stack<PageFactory>) bundle.getSerializable(STACK_TAG);
        checkNotNull(savedStack, "Bundle doesn't contain any PageManager state.");

        Page topPage = null;
        for (PageFactory pageFactory : savedStack) {
            topPage = push(pageFactory, pageFactory.getAnimatorFactory());
            topPage.getView().setVisibility(View.GONE);
        }
        notifyListeners();

        if(topPage != null) {
            topPage.getView().setVisibility(View.VISIBLE);
            topPage.onPageIsVisible();
            topPage.onPageHasFocus();
        }
    }


    /**
     * Pushes a Page, created by the provided PageFactory, onto the navigation stack
     *
     * @param factory responsible for the creation of the next Page in the navigation stack
     * @return the provided Page 
     */
    public Page goTo(PageFactory factory) {
        return goTo(factory, new NoAnimationFactory());
    }

    /**
     * Pushes a Page, created by the provided PageFactory, onto the navigation stack and animates
     * it using the Animator created by the provided PageAnimatorFactory
     *
     * @param factory responsible for the creation of the next Page in the navigation stack
     * @param pageAnimatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto and off the navigation stack
     * @return the provided Page 
     */
    public Page goTo(final PageFactory factory, final PageAnimatorFactory pageAnimatorFactory) {
        checkNotNull(factory, "factory == null");
        checkNotNull(pageAnimatorFactory, "pageAnimatorFactory == null");


        final Page newTopPage = push(factory, pageAnimatorFactory);
        View newTopView = newTopPage.getView();

        if(!mDeferNotification)
            notifyListeners();

        newTopView.getViewTreeObserver().addOnGlobalLayoutListener(new FirstLayoutListener(newTopView) {
            @Override
            public void onFirstLayout(View topView) {
                // We have to wait until the View's first layout pass to start the animation,
                // otherwise the view's width and height would be zero.

                final View belowView = peekViewBelow();
                final Page belowPage = peekBelow();
                AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (belowView != null) {
                            assert belowPage != null;

                            belowView.setVisibility(View.GONE);
                            belowPage.onPageLostFocus();
                            belowPage.onPageIsInvisible();
                        }

                        if (!newTopPage.isVisible()) newTopPage.onPageIsVisible();
                        if (!newTopPage.hasFocus()) newTopPage.onPageHasFocus();
                    }
                };

                Animator in = pageAnimatorFactory.createInAnimator(topView);
                if(belowView != null) {
                    assert belowPage != null;

                    Animator out = belowPage.getAnimatorFactory().createOutAnimator(belowView);
                    startAnimation(listener, in, out);
                }
                else {
                    startAnimation(listener, in);
                }


                //fingers crossed!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    topView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                else {
                    topView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });


        return newTopPage;
    }


    /**
     * Replaces the stack with a Page created by the provided PageFactory, onto the navigation stack
     *
     * @param factory responsible for the creation of the new Page in the navigation stack
     * @return the provided Page
     */
    public Page replace(PageFactory factory) {
        return replace(factory, new NoAnimationFactory());
    }

    /**
     * Replaces the stack with a Page created by the provided PageFactory, onto the navigation stack and animates
     * it using the Animator created by the provided PageAnimatorFactory
     *
     * @param factory responsible for the creation of the new Page in the navigation stack
     * @param pageAnimatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto and off the navigation stack
     * @return the provided Page
     */
    public Page replace(final PageFactory factory, final PageAnimatorFactory pageAnimatorFactory) {
        checkNotNull(factory, "factory == null");
        checkNotNull(pageAnimatorFactory, "inPageAnimatorFactory == null");

        final Page currentPage = size() > 0 ? peek() : null;
        final View currentView = size() > 0 ? peekView() : null;

        popAll();

        final Page newTopPage = push(factory, pageAnimatorFactory);
        View newTopView = newTopPage.getView();


        if(!mDeferNotification)
            notifyListeners();


        newTopView.getViewTreeObserver().addOnGlobalLayoutListener(new FirstLayoutListener(newTopView) {
            @Override
            public void onFirstLayout(View topView) {
                // We have to wait until the View's first layout pass to start the animation,
                // otherwise the view's width and height would be zero.
                AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (currentView != null) {
                            mPageContainer.removeView(currentView);
                        }

                        if (!newTopPage.isVisible()) newTopPage.onPageIsVisible();
                        if (!newTopPage.hasFocus()) newTopPage.onPageHasFocus();
                    }
                };

                Animator in = pageAnimatorFactory.createInAnimator(topView);
                if (currentView != null) {
                    assert currentPage != null;

                    Animator out = currentPage.getAnimatorFactory().createOutAnimator(currentView);
                    startAnimation(listener, in, out);
                } else {
                    startAnimation(listener, in);
                }


                //fingers crossed!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    topView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                else {
                    topView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });


        return newTopPage;
    }



    /**
     * Replaces the stack except the top Page with a Page created by the provided PageFactory placed onto the navigation stack
     *
     * @param factory responsible for the creation of the new Page in the navigation stack
     * @return the provided Page
     */
    public Page replaceExceptFirst(PageFactory factory) {
        return replaceExceptFirst(factory, new NoAnimationFactory());
    }

    /**
     * Replaces the stack except the top Page with a Page created by the provided PageFactory placed onto the navigation stack and animates
     * it using the Animator created by the provided PageAnimatorFactory
     *
     * @param factory responsible for the creation of the new Page in the navigation stack
     * @param pageAnimatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto and off the navigation stack
     * @return the provided Page
     */
    public Page replaceExceptFirst(final PageFactory factory, final PageAnimatorFactory pageAnimatorFactory) {
        checkNotNull(factory, "factory == null");
        checkNotNull(pageAnimatorFactory, "inPageAnimatorFactory == null");


        if(size() <= 1) return goTo(factory, pageAnimatorFactory);


        View topView = peekView();

        while(size() > 2) {
            Page popped = pop();

            if(popped.getView() != topView)
                mPageContainer.removeView(popped.getView());
        }

        if(mPageContainer.getChildCount() == 3) {
            mPageContainer.removeViewAt(1);//remove the middle view
        }

        final Page page = factory.getPage();
        page.onPageWillMount();

        page.setPageManager(this);
        page.setAnimatorFactory(pageAnimatorFactory);
        factory.setAnimatorFactory(pageAnimatorFactory);

        mFactoryStack.add(1, factory);
        mPageStack.add(1, page);

        View view = page.onCreateView(mPageContainer.getContext(), mPageContainer);
        page.setView(view);
        mPageContainer.addView(view, 1);

        page.onPageMounted(view);

        goBack();

        return page;
    }

    /**
     * Go to first page in the stack
     * Removes all but the first Page in the stack
     */
    public void gotoFirst() {
        if(size() == 1) return;//we are on the first page already

        View topView = peekView();

        while(size() > 2) {
            Page popped = pop();

            if(popped.getView() != topView)
                mPageContainer.removeView(popped.getView());
        }

        if(mPageContainer.getChildCount() == 3) {
            mPageContainer.removeViewAt(1);//remove the middle view
        }

        mDontAnimatePop = true;
        goBack();
        mDontAnimatePop = false;
    }

    protected void setBelowLostFocus() {
        Page below = peekBelow();
        if(below != null) below.onPageLostFocus();
    }

    /**
     * Pops the top Page off the navigation stack
     *
     * @return the Page instance that was used for the creation of the top View on the
     * navigation stack
     */
    public Page goBack() {
        if (!shouldPop()) return null;


        View belowView = peekViewBelow();
        final Page pageBelow = peekBelow();

        //shouldn't be null because of shouldPop
        assert belowView != null;
        assert pageBelow != null;

        final View currentTopView = peekView();
        final Page currentTopPage = pop();
        PageAnimatorFactory pageAnimatorFactory = currentTopPage.getAnimatorFactory();


        belowView.setVisibility(View.VISIBLE);
        final Runnable popFinisher = new Runnable() {
            @Override
            public void run() {
                mPageContainer.removeView(currentTopView);

                if(!pageBelow.isVisible()) pageBelow.onPageIsVisible();
                if(!pageBelow.hasFocus()) pageBelow.onPageHasFocus();
            }
        };


        if (!mDeferNotification)
            notifyListeners();

        if(mDontAnimatePop) {
            pageBelow.getAnimatorFactory().undoOutAnimation(belowView);
            popFinisher.run();
        }
        else {
            Animator out = pageAnimatorFactory.createOutAnimator(currentTopView);
            AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    popFinisher.run();
                }
            };

            Animator in = pageBelow.getAnimatorFactory().createInAnimator(belowView);
            startAnimation(listener, out, in);
        }

        return currentTopPage;
    }

    /**
     * @return the Page responsible for creating the top View on the navigation stack
     */
    public Page peek() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return mPageStack.peek();
    }

    /**
     * @return the Page below the top Page on the navigation stack
     */
    protected Page peekBelow() {
        if (size() == 0) {
            throw new EmptyStackException();
        }

        if(size() == 1)
            return null;

        return mPageStack.get(size() - 2);
    }

    /**
     * @return the View child at the top of the navigation stack
     */
    protected View peekView() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return mPageContainer.getChildAt(mPageContainer.getChildCount() - 1);
    }

    /**
     * @return the View child below the first view on top of the navigation stack
     */
    protected View peekViewBelow() {
        if (size() == 0)
            throw new EmptyStackException();

        if(size() == 1)
            return null;

        return mPageContainer.getChildAt(mPageContainer.getChildCount() - 2);
    }

    /**
     * @return the size of the navigation stack
     */
    public int size() {
        return mPageStack.size();
    }


    protected Page push(PageFactory factory, PageAnimatorFactory pageAnimatorFactory) {
        final Page newTopPage = factory.getPage();

        newTopPage.onPageWillMount();

        newTopPage.setPageManager(this);
        newTopPage.setAnimatorFactory(pageAnimatorFactory);
        factory.setAnimatorFactory(pageAnimatorFactory);

        mFactoryStack.push(factory);
        mPageStack.push(newTopPage);

        View newTopView = newTopPage.onCreateView(mPageContainer.getContext(), mPageContainer);
        newTopPage.setView(newTopView);
        mPageContainer.addView(newTopView);

        newTopPage.onPageMounted(newTopView);

        return newTopPage;
    }

    protected void clear() {
        popAll();

        mPageContainer.removeAllViews();
    }

    protected Page pop() {
        mFactoryStack.pop();//let's remove the factory
        final Page popped = mPageStack.pop();

        if(popped.hasFocus())
            popped.onPageLostFocus();

        if(popped.isVisible())
            popped.onPageIsInvisible();

        popped.onPageWillUnMount();

        if (popped.peekNestedPageManager() != null)
            popped.peekNestedPageManager().onDestroy();

        return popped;
    }

    protected Page popAll() {
        Page last = null;
        while (!mFactoryStack.isEmpty()) last = pop();

        return last;
    }

    /**
     * Adds a StackChangedListener for stack-changed events
     *
     * @param listener A StackChangedListener
     * @return always true
     */
    public boolean addStackChangedListener(StackChangedListener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes the supplied StackChangedListener
     *
     * @param listener The StackChangedListener to remove
     * @return true if the StackChangedListener was actually removed
     */
    public boolean removeStackChangedListener(StackChangedListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Removes all StackChangedListeners
     */
    public void clearStackChangedListeners() {
        listeners.clear();
    }


    private void startAnimation(Animator.AnimatorListener listener, Animator... animators) {
        AnimatorSet set = new AnimatorSet();
        set.addListener(listener);
        set.playTogether(animators);
        set.start();
    }

    private boolean shouldPop() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        if (size() == 1) {
            if(delegate != null)
                delegate.onStackEmpty();

            return false;
        }
        return true;
    }

    private void notifyListeners() {
        for (StackChangedListener listener : listeners) {
            listener.onStackChanged();
        }
    }



    protected void onStart() {
        Log.d(TAG, "onStart: " + nesting);
        // The activity is about to become visible.
        mHasStarted = true;

        Page page = peek();
        if(!page.isVisible()) page.onPageIsVisible();
    }

    protected void onResume() {
        Log.d(TAG, "onResume: " + nesting);
        // The activity has become visible (it is now "resumed").
        mHasResumed = true;

        Page page = peek();
        if(!page.hasFocus()) page.onPageHasFocus();
    }

    protected void onPause() {
        Log.d(TAG, "onPause: " + nesting);
        // Another activity is taking focus (this activity is about to be "paused").
        mHasResumed = false;

        Page page = peek();
        if(page.hasFocus()) page.onPageLostFocus();
    }

    protected void onStop() {
        Log.d(TAG, "onStop: " + nesting);
        // The activity is no longer visible (it is now "stopped")
        mHasStarted = false;

        Page page = peek();
        if(page.isVisible()) page.onPageIsInvisible();
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + nesting);
        // The activity is about to be destroyed.
        clear();
    }

    /**
     * Saves the PageManager state (an ordered stack of PageFactories) to the provided Bundle using
     * the provided tag
     *
     * @param outState The Bundle in which to save the serialized Stack of ViewFactories
     */
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: " + nesting);
        checkNotNull(outState, "bundle == null");

        outState.putSerializable(STACK_TAG, (Stack) mFactoryStack.clone());
    }

}
