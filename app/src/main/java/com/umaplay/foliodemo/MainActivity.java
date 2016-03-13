package com.umaplay.foliodemo;

import android.animation.Animator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.PageManager;
import com.umaplay.folio.PagedActivity;
import com.umaplay.folio.animator.AnimatorUtils;
import com.umaplay.folio.animator.PageAnimatorFactory;
import com.umaplay.foliodemo.animation.CircularAnimatorUtils;
import com.umaplay.foliodemo.page.RedPage;

public class MainActivity extends PagedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPageManager(new PageManager((ViewGroup) findViewById(R.id.container), this, savedInstanceState));

        if (savedInstanceState == null) {
            getPageManager().goTo(new RedPage.RedPageFactory());
        }
    }
}
