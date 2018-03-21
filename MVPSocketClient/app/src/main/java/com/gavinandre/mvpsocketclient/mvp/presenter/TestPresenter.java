package com.gavinandre.mvpsocketclient.mvp.presenter;

import com.gavinandre.mvpsocketclient.mvp.base.IBasePresenter;
import com.gavinandre.mvpsocketclient.mvp.model.TestModel;
import com.gavinandre.mvpsocketclient.mvp.view.ITestView;

/**
 * Created by gavinandre on 18-1-8.
 */
public class TestPresenter implements IBasePresenter<ITestView> {

    private static final String TAG = TestPresenter.class.getSimpleName();

    private ITestView mView;
    private TestModel mModel;

    public TestPresenter(ITestView mView) {
        attachViewModel(mView);
    }

    @Override
    public void attachViewModel(ITestView view) {
        this.mView = view;
        this.mModel = new TestModel(this);
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
