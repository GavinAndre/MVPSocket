package com.gavinandre.mvpsocketclient.mvp.base;

/**
 * Created by gavinandre on 18-1-8.
 */
public abstract class BaseModel<SubP> {

    protected SubP mPresenter;

    public BaseModel(SubP presenter) {
        this.mPresenter = presenter;
    }

    public void detachModel() {
        this.mPresenter = null;
    }
}

