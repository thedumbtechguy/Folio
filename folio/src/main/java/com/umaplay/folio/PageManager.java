package com.umaplay.folio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.leakcanary.RefWatcher;
import com.umaplay.folio.animator.NoAnimationFactory;
import com.umaplay.folio.animator.PageAnimatorFactory;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import static com.umaplay.folio.Preconditions.checkNotNull;

/**
 * This manages a navigation stack by representing each item in the mPageStack as a Page, which is
 * responsible for View creation. All standard Java Stack operations are supported, with additional
 * methods for pushing and popping with animated transitions.
 */
public class PageManager {

    private static final String STACK_TAG = "PageManager.STACK_TAG";
    private static final String STATE_TAG = "PageManager.STATE_TAG";
    public static final String VIEW_STATE_KEY = "PageManager.VIEW_STATE_KEY";
    private static final String TAG = "Folio.PageManager";

    private final Stack<Page> mPageStack = new Stack<>();
    private final Stack<PageFactory> mFactoryStack = new Stack<>();
    private final ViewGroup mPageContainer;
    private final PageStackDelegate mPageStackDelegate;
    private final List<StackChangedListener> listeners = new ArrayList<>();
    private final RefWatcher mRefWatcher;
    private Bundle mPageStates = new Bundle();
    protected boolean mHasStarted;
    protected boolean mHasResumed;
    private boolean mDeferNotification;
    private boolean mDontAnimatePop = false;
    protected int nesting = 0;

    /**
     * Constructor for PageManager instances
     *
     * @param container Any ViewGroup mPageContainer for navigation Views. Typically a FrameLayout
     * @param delegate  A PageStackDelegate responsible for "finishing" the navigation
     */
    public PageManager(ViewGroup container, PageStackDelegate delegate, RefWatcher refWatcher,
                       Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        checkNotNull(container, "container == null");
        checkNotNull(refWatcher, "refWatcher == null");

        this.mPageContainer = container;
        this.mRefWatcher = refWatcher;
        this.mPageStackDelegate = delegate;

        if(savedInstanceState != null) _onRestoreInstanceState(savedInstanceState);
    }

    public NestedPageManager getNestedPageManager(ViewGroup container, Bundle stateBundle) {
        return new NestedPageManager(container, mRefWatcher, stateBundle, nesting + 1);
    }

    protected Bundle getPageState(Page page) {
        Bundle state = mPageStates.getBundle(page.getId());
        mPageStates.remove(page.getId());//we no longer need it

        return state;
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
        View newTopView = mountTopPage(newTopPage, null);

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
                        if (belowPage != null) {
                            unmountPage(belowPage, true);
                        }

                        newTopPage.onPageIsVisible();
                        newTopPage.onPageHasFocus();
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


                //fingers crossed! Maybe this bug has been fixed
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
    public Page replaceAll(PageFactory factory) {
        return replaceAll(factory, new NoAnimationFactory());
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
    public Page replaceAll(final PageFactory factory, final PageAnimatorFactory pageAnimatorFactory) {
        checkNotNull(factory, "factory == null");
        checkNotNull(pageAnimatorFactory, "inPageAnimatorFactory == null");

        int limit = size() - 1;
        for(int i = 0; i < limit; i++) {//we remove all the pages except the top
            Page popped = remove(i);

            unmountPage(popped, false);//this shouldn't even do anything since the pages are
            // already unmounted and invisible
            destroyPage(popped);
        }

        final Page currentPage = size() == 1 ? pop() : null;
        final View currentView = size() == 1 ? peekView() : null;

        final Page newTopPage = push(factory, pageAnimatorFactory);
        View newTopView = mountTopPage(newTopPage, null);

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
                        if (currentPage != null) {
                            unmountPage(currentPage, true);
                            destroyPage(currentPage);//we are done with this
                        }

                        newTopPage.onPageIsVisible();
                        newTopPage.onPageHasFocus();
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

        if(size() <= 1) return goTo(factory, pageAnimatorFactory);//only one page let's go to the next

        int limit = size() - 1;
        for(int i = 1; i < limit; i++) {//we remove all the pages in between first and the top
            Page popped = remove(i);

            unmountPage(popped, false);//this shouldn't even do anything since the pages are
            // already unmounted and invisible
            destroyPage(popped);
        }

        push(factory, pageAnimatorFactory);

        final Page page = add(1, factory, pageAnimatorFactory);

        mountBottomPage(page, null);

        goBack();

        return page;
    }

    /**
     * Go to first page in the stack
     * Removes all but the first Page in the stack
     * We need to be aware that when using this, the page between the top and first are destroyed
     * first. Then the top page is removed leaving the first.
     */
    public void gotoFirst() {
        if(size() == 1) return;//we are on the first page already

        int limit = size() - 1;
        for(int i = 1; i < limit; i++) {//we remove all the pages in between first and the top
            Page popped = remove(i);

            unmountPage(popped, false);//this shouldn't even do anything since the pages are
            // already unmounted and invisible
            destroyPage(popped);
        }

        mDontAnimatePop = true;
        goBack();
        mDontAnimatePop = false;
    }

//    protected void setBelowLostFocus() {
//        Page below = peekBelow();
//        if(below != null) below.onPageLostFocus();
//    }

    /**
     * Pops the top Page off the navigation stack
     *
     * @return the Page instance that was used for the creation of the top View on the
     * navigation stack
     */
    public Page goBack() {
        if (!shouldPop()) return null;

        final Page pageBelow = peekBelow();

        //shouldn't be null because of shouldPop
        assert pageBelow != null;

        //recreate the page's view
        View belowView = mountBottomPage(pageBelow, getPageState(pageBelow));

        final View currentTopView = peekView();
        final Page currentTopPage = pop();
        PageAnimatorFactory pageAnimatorFactory = currentTopPage.getAnimatorFactory();
//        mPageStates.remove(currentTopPage.getId());

        belowView.setVisibility(View.VISIBLE);
        final Runnable popFinisher = new Runnable() {
            @Override
            public void run() {
                unmountPage(currentTopPage, false);
                destroyPage(currentTopPage);

                pageBelow.onPageIsVisible();
                pageBelow.onPageHasFocus();
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
        Page page = addPage(factory, pageAnimatorFactory);

        mFactoryStack.push(factory);
        mPageStack.push(page);

        return page;
    }

    protected Page add(int index, PageFactory factory, PageAnimatorFactory pageAnimatorFactory) {
        Page page = addPage(factory, pageAnimatorFactory);

        mFactoryStack.add(index, factory);
        mPageStack.add(index, page);

        return page;
    }

    protected Page addPage(PageFactory factory, PageAnimatorFactory pageAnimatorFactory) {
        if(factory.getId() == null) {
            factory.setId(UUID.randomUUID().toString());
        }

        final Page page = factory.getPage();

        page.setPageManager(this);
        page.setAnimatorFactory(pageAnimatorFactory);
        factory.setAnimatorFactory(pageAnimatorFactory);

        page.setId(factory.getId());

        page.onCreate();

        return page;
    }

    protected Page pop() {
        mFactoryStack.pop();//let's remove the factory
        final Page popped = mPageStack.pop();

        mPageStates.remove(popped.getId());//size - 1 is usually it, but we already popped

        return popped;
    }

    protected Page remove(int index) {
        mFactoryStack.remove(index);
        final Page popped = mPageStack.remove(index);

        mPageStates.remove(popped.getId());

        return popped;
    }

    protected View createView(Page page, Bundle state) {
        View view = page.onCreateView(mPageContainer.getContext(), mPageContainer);
        if(state != null) {
            view.restoreHierarchyState(state.getSparseParcelableArray(VIEW_STATE_KEY));
        }

        return view;
    }

    protected View mountTopPage(Page page, Bundle state) {
        View newTopView = createView(page, state);
        mPageContainer.addView(newTopView);
        page.onViewMounted(newTopView);
        if(state != null)
            page.onRestoreState(state);

        return page.getView();
    }

    protected View mountBottomPage(Page page, Bundle state) {
        View view = createView(page, state);
        mPageContainer.addView(view, 0);
        page.onViewMounted(view);
        if(state != null)
          page.onRestoreState(state);

        return page.getView();
    }


    protected void unmountPage(Page page, boolean canBeRestored) {
        if(page.hasFocus()) page.onPageLostFocus();
        if(page.isVisible()) page.onPageIsInvisible();

        if(page.isMounted()) {
            View view = page.getView();
            view.setVisibility(View.GONE);
            if(canBeRestored) {
                savePageState(page);
            }

            page.onViewUnmounted();
            mPageContainer.removeView(view);
            mRefWatcher.watch(view);//let's make sure the view is collected
        }
    }

    private void savePageState(Page page) {
        int pageIndex = mPageStack.indexOf(page);
        if(pageIndex == -1) throw new IllegalStateException("Cannot save state of " +
                "Page which is not in the stack");

        SparseArray<Parcelable> viewState = new SparseArray<>();
        View view = page.getView();
        view.saveHierarchyState(viewState);
        Bundle state = new Bundle();
        page.onSaveState(state);
        state.putSparseParcelableArray(VIEW_STATE_KEY, viewState);
        mPageStates.putBundle(page.getId(), state);
    }


    private void destroyPage(Page page) {
        page.onDestroy();

        mRefWatcher.watch(page);
    }

    protected void clear() {
        Page page;
        while (!mFactoryStack.isEmpty()) {
            page = pop();
            unmountPage(page, false);
            destroyPage(page);
        }
    }


//    protected Page popAll() {
//        Page last = null;
//        while (!mFactoryStack.isEmpty()) last = remove();
//
//        return last;
//    }

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
            if(mPageStackDelegate != null)
                mPageStackDelegate.onStackEmpty();

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

    /**
     * Saves the PageManager state (an ordered stack of PageFactories) to the provided Bundle using
     * the provided tag
     *
     * @param outState The Bundle in which to save the serialized Stack of ViewFactories
     */
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: " + nesting);
        checkNotNull(outState, "bundle == null");

        Page page = peek();
        savePageState(page);

        outState.putSerializable(STACK_TAG, (Stack) mFactoryStack.clone());
        outState.putBundle(STATE_TAG, (Bundle) mPageStates.clone());
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
        // The host is about to be destroyed.
        clear();

        mPageStates = null;
    }

    @SuppressWarnings("unchecked")
    protected void _onRestoreInstanceState(Bundle bundle) {
        checkNotNull(bundle, "bundle == null");

        Stack<PageFactory> savedStack = (Stack<PageFactory>) bundle.getSerializable(STACK_TAG);
        mPageStates = bundle.getBundle(STATE_TAG);
        checkNotNull(savedStack, "Bundle doesn't contain PageManager state.");
        checkNotNull(mPageStates, "Bundle doesn't contain PageManager state.");

        Page topPage = null;
        for (PageFactory pageFactory : savedStack) {
            topPage = push(pageFactory, pageFactory.getAnimatorFactory());
        }

        if(topPage != null) {
            mountTopPage(topPage, getPageState(topPage));
            notifyListeners();

            topPage.onPageIsVisible();
            topPage.onPageHasFocus();
        }
        else
            notifyListeners();
    }


    public static class NestedPageManager extends PageManager {
        private static final String TAG = "Folio.NestedPageManager";
        /**
         * Constructor for child PageManager instances
         * Used internally to give Pages the ability to have nested pages
         *
         * @param container Any ViewGroup mPageContainer for navigation Views. Typically a FrameLayout
         * @param stateBundle
         */
        private NestedPageManager(ViewGroup container, RefWatcher refWatcher, Bundle stateBundle, int
                nesting) {
            super(container, null, refWatcher, stateBundle);

            Log.d(TAG, "onCreateNested: " + nesting);

            this.nesting = nesting;
        }


        protected void onStart() {
            Log.d(TAG, "onStart: " + nesting);
            // The activity is about to become visible.
            mHasStarted = true;

            if(size() > 0) {
                Page page = peek();
                if (!page.isVisible()) page.onPageIsVisible();
            }
        }

        protected void onResume() {
            Log.d(TAG, "onResume: " + nesting);
            // The activity has become visible (it is now "resumed").
            mHasResumed = true;

            if(size() > 0) {
                Page page = peek();
                if (!page.hasFocus()) page.onPageHasFocus();
            }
        }


        public void onRestoreInstanceState(Bundle bundle) {
            _onRestoreInstanceState(bundle);
        }
    }

}
