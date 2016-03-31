package com.umaplay.foliodemo.page;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.Page;
import com.umaplay.folio.BasePageFactory;
import com.umaplay.foliodemo.R;

/**
 * Created by user on 2/28/2016.
 */
public class RedPage extends Page {
    @Override
    public View onCreateView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.view_red, container, false);
    }

    @Override
    public void onViewMounted(View view) {
        super.onViewMounted(view);

        view.findViewById(R.id.red_button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "RedPage popping itself");
                getPageManager().goBack();
            }
        });

        view.findViewById(R.id.red_button_go_to_green).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("testing", "RedPage pushing GreenView");
                getPageManager().goTo(new GreenPage.GreenPageFactory(), new GreenPage.AnimatorFactory());
            }
        });
    }

    public static class RedPageFactory extends BasePageFactory {
        @NonNull
        @Override
        public Page getPage() {
            return new RedPage();
        }
    }
}
