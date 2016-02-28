package com.umaplay.folio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * This is an activity that is PageManager aware.
 * It handles the lifecycle methods.
 */
public class PagedActivity extends AppCompatActivity implements PageStackDelegate {
    protected PageManager mPageManager;

    @Override
    protected void onStart() {
        super.onStart();
        mPageManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPageManager.onResume();
    }

    @Override
    protected void onPause() {
        mPageManager.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPageManager.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPageManager.onDestroy();
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        mPageManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public PageManager pageStack() {
        return mPageManager;
    }

    @Override
    public void onBackPressed() {
        mPageManager.goBack();
    }

    @Override
    public void onStackEmpty() {
        finish();
    }
}
