package com.umaplay.foliodemo.page;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.BasePage;
import com.umaplay.foliodemo.R;

/**
 * Created by user on 2/28/2016.
 */
public class BluePage extends BasePage {
    @Override
    public View onCreateView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.view_blue, container, false);
    }

    @Override
    public void onPageMounted(View view) {
        super.onPageMounted(view);

        view.findViewById(R.id.blue_button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "BlueView popping itself");
                getPageManager().goBack();
            }
        });
    }
}
