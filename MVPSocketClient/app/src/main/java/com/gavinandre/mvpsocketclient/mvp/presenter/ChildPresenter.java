package com.gavinandre.mvpsocketclient.mvp.presenter;

import com.gavinandre.mvpsocketclient.mvp.base.IBasePresenter;
import com.gavinandre.mvpsocketclient.mvp.model.ChildModel;
import com.gavinandre.mvpsocketclient.mvp.view.IChildView;

/**
 * Created by gavinandre on 18-1-8.
 */
public class ChildPresenter implements IBasePresenter<IChildView> {

    private IChildView mView;
    private ChildModel mModel;

    public ChildPresenter(IChildView mView) {
        attachViewModel(mView);
    }

    @Override
    public void attachViewModel(IChildView view) {
        this.mView = view;
        this.mModel = new ChildModel(this);
    }

    @Override
    public void detachViewModel() {
        this.mView = null;
        this.mModel.detachModel();
        this.mModel = null;
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
