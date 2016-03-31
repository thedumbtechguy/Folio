package com.umaplay.foliodemo.page;

import android.animation.Animator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umaplay.folio.Page;
import com.umaplay.folio.BasePageFactory;
import com.umaplay.folio.animator.AnimatorUtils;
import com.umaplay.folio.animator.PageAnimatorFactory;
import com.umaplay.foliodemo.R;

/**
 * Created by user on 2/28/2016.
 */
public class GreenPage extends Page {
    @Override
    public View onCreateView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.view_green, container, false);
    }

    @Override
    public void onViewMounted(View view) {
        super.onViewMounted(view);

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
                getPageManager().goTo(new BluePage.BluePageFactory(), new BluePage.AnimatorFactory());
            }
        });
    }

    public static class GreenPageFactory extends BasePageFactory {
        @Override
        public Page getPage() {
            return new GreenPage();
        }
    }

    public static class AnimatorFactory implements PageAnimatorFactory {
        @Override
        public Animator createInAnimator(View view) {
            return AnimatorUtils.createSlideInFromLeftAnimator(view);
        }

        @Override
        public Animator createOutAnimator(View view) {
            return AnimatorUtils.createSlideOutToLeftAnimator(view);
        }

        @Override
        public void undoOutAnimation(View view) {
            AnimatorUtils.undoSlideLeftRight(view);
        }
    }
}
