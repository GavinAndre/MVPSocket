package com.gavinandre.mvpsocketclient.mvp.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gavinandre.mvpsocketclient.ui.fragment.BaseFragment;

/**
 * Created by gavinandre on 18-1-8.
 */
public abstract class AbstractMvpFragment<V extends IBaseView, P extends IBasePresenter<V>> extends
        BaseFragment implements IBaseDelegate<V, P> {

    private static final String TAG = AbstractMvpFragment.class.getSimpleName();

    protected P mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onDestroy() {
        //解除绑定
        if (mPresenter != null) {
            mPresenter.detachViewModel();
            mPresenter = null;
        }
        super.onDestroy();
    }

}
