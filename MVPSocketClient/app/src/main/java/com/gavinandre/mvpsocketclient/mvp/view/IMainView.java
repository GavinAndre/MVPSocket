package com.gavinandre.mvpsocketclient.mvp.view;

import com.gavinandre.mvpsocketclient.mvp.base.IBaseView;

/**
 * Created by gavinandre on 18-1-8.
 */
public interface IMainView extends IBaseView {

    /**
     * 显示数据
     */
    void showData(String s);

    /**
     * 检测数据
     */
    void showMessage(String msg);
}
