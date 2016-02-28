package com.umaplay.folio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.animator.NoAnimation;
import com.umaplay.folio.animator.PageAnimatorFactory;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import static com.umaplay.folio.Preconditions.checkNotNull;

/**
 * This manages a navigation stack by representing each item in the stack as a Page, which is
 * responsible for View creation. All standard Java Stack operations are supported, with additional
 * methods for pushing and popping with animated transitions.
 */
public final class PageManager {

    private static final String STACK_TAG = "STACK_TAG";
    private final Stack<Page> stack = new Stack<>();
    private final ViewGroup container;
    private final PageStackDelegate delegate;
    private final List<StackChangedListener> listeners = new ArrayList<>();
    private boolean mHasStarted;
    private boolean mHasResumed;
    private boolean mDeferNotification;

    /**
     * Constructor for PageManager instances
     *
     * @param container Any ViewGroup container for navigation Views. Typically a FrameLayout
     * @param delegate  A PageStackDelegate responsible for "finishing" the navigation stack
     * @return A new PageManager instance
     */
    public PageManager(ViewGroup container, PageStackDelegate delegate, Bundle savedInstanceState) {
        checkNotNull(container, "container == null");
        checkNotNull(delegate, "delegate == null");

        this.container = container;
        this.delegate = delegate;

        if(savedInstanceState != null)
            restoreFromSavedInstanceState(savedInstanceState);
    }

    /**
     * Constructor for child PageManager instances
     * Used internally to give Pages the ability to have nested pages
     *
     * @param container Any ViewGroup container for navigation Views. Typically a FrameLayout
     * @return A new PageManager instance
     */
    private PageManager(ViewGroup container) {
        checkNotNull(container, "container == null");

        this.container = container;
        this.delegate = null;
    }

    public PageManager getNestedPageManager(ViewGroup container) {
        return new PageManager(container);
    }



    @SuppressWarnings("unchecked")
    protected void restoreFromSavedInstanceState(Bundle bundle) {
        checkNotNull(bundle, "bundle == null");

        Stack<Page> savedStack = (Stack<Page>) bundle.getSerializable(STACK_TAG);
        checkNotNull(savedStack, "Bundle doesn't contain any PageManager state.");

        mDeferNotification = true;
        for (Page page : savedStack) {
            goTo(page);
        }
        mDeferNotification = false;

        notifyListeners();
    }


    /**
     * Pushes a View, created by the provided Page, onto the navigation stack
     *
     * @param page responsible for the creation of the next View in the navigation stack
     * @return the provided Page 
     */
    public Page goTo(Page page) {
        return goTo(page, new NoAnimation(),
                page.getOutAnimator() == null ? new NoAnimation() : page.getOutAnimator());
    }

    /**
     * Pushes a View, created by the provided Page, onto the navigation stack and animates
     * it using the Animator created by the provided PageAnimatorFactory
     *
     * @param page responsible for the creation of the next View in the navigation stack
     * @param inPageAnimatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto the navigation stack
     * @return the provided Page 
     */
    public Page goTo(final Page page, final PageAnimatorFactory inPageAnimatorFactory) {
        return goTo(page, inPageAnimatorFactory,
                page.getOutAnimator() == null ? new NoAnimation() : page.getOutAnimator());
    }

    /**
     * Pushes a View, created by the provided Page, onto the navigation stack and animates
     * it using the Animator created by the provided PageAnimatorFactory
     *
     * @param page responsible for the creation of the next View in the navigation stack
     * @param inPageAnimatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto the navigation stack
     * @param outPageAnimatorFactory responsible for the creation of an Animator to animate the current View
     *                        off the navigation stack
     * @return the provided Page 
     */
    public Page goTo(final Page page, final PageAnimatorFactory inPageAnimatorFactory, final PageAnimatorFactory outPageAnimatorFactory) {
        checkNotNull(page, "page == null");

        page.onPageWillMount();

        page.setPageManager(this);
        page.setOutAnimator(outPageAnimatorFactory);
        stack.push(page);

        View view = page.onCreateView(container.getContext(), container);
        page.setView(view);
        container.addView(view);

        setBelowLostFocus();
        page.onPageMounted(view);


        page.setAnimated(true);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new FirstLayoutListener(view) {
            @Override
            public void onFirstLayout(View view) {
                // We have to wait until the View's first layout pass to start the animation,
                // otherwise the view's width and height would be zero.
                startAnimation(inPageAnimatorFactory, view, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        setBelowGone();

                        if (!page.isVisible()) page.onPageIsVisible();
                        if (!page.hasFocus()) page.onPageHasFocus();
                    }
                });
            }
        });



        if(!mDeferNotification)
            notifyListeners();

        return page;
    }

    protected void setBelowGone() {
        if (container.getChildCount() > 1) {
            container.getChildAt(container.getChildCount() - 2).setVisibility(View.GONE);
            stack.get(size() - 2).onPageIsInvisible();
        }
    }

    protected void setBelowVisible() {
        if (container.getChildCount() > 1) {
            container.getChildAt(container.getChildCount() - 2).setVisibility(View.VISIBLE);
        }
    }

    protected void setBelowLostFocus() {
        if(size() > 1) stack.get(size() - 2).onPageLostFocus();
    }

    /**
     * Pops the top View off the navigation stack
     *
     * @return the Page instance that was used for the creation of the top View on the
     * navigation stack
     */
    public Page goBack() {
       return goBack(null);
    }

    /**
     * Pops the top View off the navigation stack and animates it using the Animator created by the
     * provided PageAnimatorFactory
     *
     * @param outPageAnimatorFactory responsible for the creation of an Animator to animate the current
     *                        View off the navigation stack
     * @return the Page instance that was used for the creation of the top View on the
     * navigation stack
     */
    public Page goBack(PageAnimatorFactory outPageAnimatorFactory) {
        if (!shouldPop()) return null;

        final Page popped = stack.pop();
        final View view = peekView();

        if(outPageAnimatorFactory == null)
            outPageAnimatorFactory = popped.getOutAnimator();

        if(popped.hasFocus())
            popped.onPageLostFocus();

        if(popped.isVisible())
            popped.onPageIsInvisible();

        popped.onPageWillUnMount();

        setBelowVisible();
        startAnimation(outPageAnimatorFactory, peekView(), new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
            container.removeView(view);

            Page page = peek();
            page.onPageIsVisible();
            page.onPageHasFocus();

            if(popped.peekNestedPageManager() != null)
                popped.peekNestedPageManager().onDestroy();


            if(!mDeferNotification) notifyListeners();
            }
        });


        return popped;
    }

    /**
     * @return the Page responsible for creating the top View on the navigation stack
     */
    public Page peek() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return stack.peek();
    }

    /**
     * @return the View child at the top of the navigation stack
     */
    public View peekView() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return container.getChildAt(container.getChildCount() - 1);
    }

    /**
     * @return the size of the navigation stack
     */
    public int size() {
        return stack.size();
    }

    protected void clear() {
        mDeferNotification = true;
        while (goBack() != null)
            ;//intended empty body
        mDeferNotification = false;

        notifyListeners();
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


    private void startAnimation(PageAnimatorFactory pageAnimatorFactory, View view, Animator.AnimatorListener listener) {
        Animator animator = pageAnimatorFactory.createAnimator(view);
        animator.addListener(listener);
        animator.start();
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
        // The activity is about to become visible.
        mHasStarted = true;

        Page page = peek();
        if(!page.isVisible()) page.onPageIsVisible();
    }

    protected void onResume() {
        // The activity has become visible (it is now "resumed").
        mHasResumed = true;

        Page page = peek();
        if(!page.hasFocus()) page.onPageHasFocus();
    }

    protected void onPause() {
        // Another activity is taking focus (this activity is about to be "paused").
        mHasResumed = false;


        Page page = peek();
        if(page.hasFocus()) page.onPageLostFocus();
    }

    protected void onStop() {
        // The activity is no longer visible (it is now "stopped")
        mHasStarted = false;

        Page page = peek();
        if(page.isVisible()) page.onPageIsInvisible();
    }

    protected void onDestroy() {
        // The activity is about to be destroyed.
        clear();
    }

    /**
     * Saves the PageManager state (an ordered stack of ViewFactories) to the provided Bundle using
     * the provided tag
     *
     * @param outState The Bundle in which to save the serialized Stack of ViewFactories
     */
    public void onSaveInstanceState(Bundle outState) {
        checkNotNull(outState, "bundle == null");
        //outState.putSerializable(STACK_TAG, stack);
    }

}
