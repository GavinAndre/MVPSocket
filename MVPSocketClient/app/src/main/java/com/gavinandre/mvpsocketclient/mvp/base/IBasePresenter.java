package com.gavinandre.mvpsocketclient.mvp.base;

/**
 * Created by gavinandre on 18-1-8.
 */
public interface IBasePresenter<V extends IBaseView> {

    /**绑定接口*/
    void attachViewModel(V view);

    /**释放接口*/
    void detachViewModel();

}
