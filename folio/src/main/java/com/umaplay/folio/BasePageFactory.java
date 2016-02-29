package com.umaplay.folio;

import com.umaplay.folio.animator.PageAnimatorFactory;

/**
 * Created by user on 2/29/2016.
 */
public abstract class BasePageFactory implements PageFactory {

    private PageAnimatorFactory mOutAnimator;

    @Override
    public void setOutAnimator(PageAnimatorFactory outPageAnimatorFactory) {
        mOutAnimator = outPageAnimatorFactory;
    }

    @Override
    public PageAnimatorFactory getOutAnimator() {
        return mOutAnimator;
    }
}
