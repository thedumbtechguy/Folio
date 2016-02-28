package com.umaplay.folio;

/**
 * Interface for "finishing" a navigation stack. The intended implementation is calling finish()
 * in the host Activity.
 */
public interface PageStackDelegate {
    void onStackEmpty();
}
