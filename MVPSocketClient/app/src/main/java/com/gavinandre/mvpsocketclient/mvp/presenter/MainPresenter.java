package com.gavinandre.mvpsocketclient.mvp.presenter;

import com.gavinandre.mvpsocketclient.mvp.base.IBasePresenter;
import com.gavinandre.mvpsocketclient.mvp.model.MainModel;
import com.gavinandre.mvpsocketclient.mvp.view.IMainView;

/**
 * Created by gavinandre on 18-1-8.
 */
public class MainPresenter implements IBasePresenter<IMainView> {

    private IMainView mView;
    private MainModel mModel;

    public MainPresenter(IMainView mView) {
        attachView(mView);
        this.mModel = new MainModel(this);
    }

    @Override
    public void attachView(IMainView view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
    }

    public void showData(String s) {
        if (mView == null) {
            return;
        }
        mView.dismiss();
        mView.showData(s);
    }

    public void showMessage(String msg) {
        if (mView == null) {
            return;
        }
        mView.showMessage(msg);
    }

    public <T> void sendData(T data) {
        mModel.sendData(data);
    }

    public void stopSocket() {
        mModel.stopSocket();
    }
}
