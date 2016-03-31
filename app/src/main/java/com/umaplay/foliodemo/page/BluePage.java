package com.umaplay.foliodemo.page;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.umaplay.folio.Page;
import com.umaplay.folio.BasePageFactory;
import com.umaplay.folio.animator.AnimatorUtils;
import com.umaplay.folio.animator.PageAnimatorFactory;
import com.umaplay.foliodemo.R;

/**
 * Created by user on 2/28/2016.
 */
public class BluePage extends Page {
    @Override
    public View onCreateView(Context context, ViewGroup container) {
        return inflate(R.layout.view_blue, context, container);
    }

    @Override
    public void onViewMounted(final View view) {
        super.onViewMounted(view);

        FrameLayout nested = (FrameLayout) view.findViewById(R.id.nestedContainer);
        getNestedPageManager(nested).goTo(new RedPage.RedPageFactory());
    }

    public static class BluePageFactory extends BasePageFactory {
        @Override
        public Page getPage() {
            return new BluePage();
        }
    }

    public static class AnimatorFactory implements PageAnimatorFactory {
        @Override
        public Animator createInAnimator(View view) {
            return AnimatorUtils.createSlideInFromRightAnimator(view);
        }

        @Override
        public Animator createOutAnimator(View view) {
            return AnimatorUtils.createSlideOutToRightAnimator(view);
        }

        @Override
        public void undoOutAnimation(View view) {
            AnimatorUtils.undoSlideLeftRight(view);
        }

    }
}
