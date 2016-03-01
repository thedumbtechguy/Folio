package com.umaplay.folio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * This is an activity that is PageManager aware.
 * It handles the lifecycle methods.
 */
public class PagedActivity extends AppCompatActivity implements PageStackDelegate {
    private PageManager mPageManager;

    protected void setPageManager(PageManager pageManager) {
        mPageManager = pageManager;
    }

    protected PageManager getPageManager() {
        if(mPageManager == null) throw new IllegalStateException("No PageManager instance provided");

        return mPageManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPageManager().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPageManager().onResume();
    }

    @Override
    protected void onPause() {
        getPageManager().onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        getPageManager().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getPageManager().onDestroy();
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        getPageManager().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public PageManager pageStack() {
        return mPageManager;
    }

    @Override
    public void onBackPressed() {
        getPageManager().goBack();
    }

    @Override
    public void onStackEmpty() {
        finish();
    }
}
