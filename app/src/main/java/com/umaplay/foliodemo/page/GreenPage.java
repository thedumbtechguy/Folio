package com.umaplay.foliodemo.page;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.BasePage;
import com.umaplay.folio.animator.FadeOutAnimator;
import com.umaplay.foliodemo.R;
import com.umaplay.foliodemo.animation.CircularReveal;

/**
 * Created by user on 2/28/2016.
 */
public class GreenPage extends BasePage {
    @Override
    public View onCreateView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.view_green, container, false);
    }

    @Override
    public void onPageMounted(View view) {
        super.onPageMounted(view);

        view.findViewById(R.id.green_button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "GreenView popping itself");
                getPageManager().goBack();
            }
        });

        view.findViewById(R.id.green_button_go_to_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "GreenView pushing BlueView");
                getPageManager().goTo(new BluePage(), new CircularReveal(), new FadeOutAnimator());
            }
        });
    }
}
