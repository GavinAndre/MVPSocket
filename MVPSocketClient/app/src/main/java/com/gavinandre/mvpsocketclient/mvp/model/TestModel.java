package com.gavinandre.mvpsocketclient.mvp.model;

import android.util.Log;

import com.gavinandre.mvpsocketclient.mvp.presenter.TestPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by gavinandre on 18-1-7.
 */
public class TestModel extends BaseSocketModel<TestPresenter> {

    private static final String TAG = TestModel.class.getSimpleName();

    public TestModel(TestPresenter presenter) {
        super(presenter);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public <T> void onMessageEvent(T event) {
        Log.i(TAG, "onMessageEvent: " + event);
        if (mPresenter != null) {
            mPresenter.showData((String) event);
        }
    }

    @Override
    public void detachModel() {
        EventBus.getDefault().unregister(this);
        super.detachModel();
    }
}
