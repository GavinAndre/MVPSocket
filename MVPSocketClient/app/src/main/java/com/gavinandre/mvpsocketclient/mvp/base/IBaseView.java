package com.gavinandre.mvpsocketclient.mvp.base;

/**
 * Created by gavinandre on 18-1-8.
 */
public interface IBaseView {

    /**
     * 显示加载
     */
    void showLoading();

    /**
     * 完成加载
     */
    void dismiss();
}
