package com.umaplay.foliodemo.page;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.BasePage;
import com.umaplay.folio.BasePageFactory;
import com.umaplay.folio.Page;
import com.umaplay.folio.PageFactory;
import com.umaplay.foliodemo.BlankActivity;
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
    public void onPageMounted(final View view) {
        super.onPageMounted(view);

        view.findViewById(R.id.blue_button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "BlueView launching new activity");
                getContext().startActivity(new Intent(getContext(), BlankActivity.class));
            }
        });
    }

    public static class BluePageFactory extends BasePageFactory {
        @Override
        public Page getPage() {
            return new BluePage();
        }
    }
}
