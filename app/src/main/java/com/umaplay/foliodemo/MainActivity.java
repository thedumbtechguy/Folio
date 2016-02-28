package com.umaplay.foliodemo;

import android.os.Bundle;
import android.view.ViewGroup;

import com.umaplay.folio.PageManager;
import com.umaplay.folio.PagedActivity;
import com.umaplay.foliodemo.page.RedPage;

public class MainActivity extends PagedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageManager = new PageManager((ViewGroup) findViewById(R.id.container), this, savedInstanceState);

        if (savedInstanceState == null) {
            mPageManager.goTo(new RedPage());
        }
    }
}
