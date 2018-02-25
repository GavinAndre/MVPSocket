package com.gavinandre.mvpsocketclient.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.gavinandre.mvpsocketclient.mvp.base.IBaseDelegate;
import com.gavinandre.mvpsocketclient.mvp.base.IBasePresenter;
import com.gavinandre.mvpsocketclient.mvp.base.IBaseView;

/**
 * Created by gavinandre on 18-1-8.
 */
public abstract class BaseMVPActivity<V extends IBaseView, P extends IBasePresenter<V>> extends
        BaseActivity implements IBaseDelegate<V, P> {

    protected P mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
    }

    @NonNull
    @Override
    public P getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }
}
