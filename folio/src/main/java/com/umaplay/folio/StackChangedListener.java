package com.umaplay.folio;

/**
 * Listener interface for stack-changed events
 */
public interface StackChangedListener {
    /**
     * Called when BOTH the PageManager's size AND the top View in the ViewGroup container have
     * changed. For a goTo with an animation, this happens before the animation starts (right after
     * the new View is added to the container). For a goBack with an animation, this happens after the
     * animation completes (right after the old view is removed from the container).
     */
    void onStackChanged();
}
