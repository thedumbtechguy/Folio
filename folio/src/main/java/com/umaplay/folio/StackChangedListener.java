package com.umaplay.folio;

/**
 * Listener interface for stack-changed events
 */
public interface StackChangedListener {
    /**
     * Called when BOTH the PageManager's size AND the top Page in the stack have changed.
     * Depending on the action, the top View in the container might not have changed yet.
     * This is called before the animation starts.
     * For a goTo/replace, the Page has pushed onto the stack and it's view is at the top of the container.
     * For a goBack/replaceExceptFirst/gotoFirst, the Page has been popped from the stack so we have the previous Page on top but
     *  the old view still remains and will be removed after the animation completes.
     */
    void onStackChanged();
}
