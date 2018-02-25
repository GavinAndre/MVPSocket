package com.gavinandre.mvpsocketclient.mvp.base;

import android.support.annotation.NonNull;

/**
 * Created by gavinandre on 18-1-8.
 */
public interface IBaseDelegate<V extends IBaseView, P extends IBasePresenter<V>> {

    /**初始化presenter*/
    @NonNull
    P createPresenter();

    /**获取presenter*/
    @NonNull
    P getPresenter();

}

