package com.gavinandre.mvpsocketclient.mvp.base;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.gavinandre.mvpsocketclient.ui.activity.BaseActivity;

/**
 * Created by gavinandre on 18-1-8.
 */
public abstract class AbstractMvpActivity<V extends IBaseView, P extends IBasePresenter<V>> extends
        BaseActivity implements IBaseDelegate<V, P> {

    protected P mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPresenter == null) {
            mPresenter = createPresenter();
        }
    }

    @NonNull
    @Override
    public P getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onDestroy() {
        //解除绑定
        if (mPresenter != null) {
            mPresenter.detachViewModel();
            mPresenter = null;
        }
        super.onDestroy();
    }
}
