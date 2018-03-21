package com.gavinandre.mvpsocketclient.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.gavinandre.mvpsocketclient.R;
import com.gavinandre.mvpsocketclient.mvp.base.AbstractMvpActivity;
import com.gavinandre.mvpsocketclient.mvp.presenter.ChildPresenter;
import com.gavinandre.mvpsocketclient.mvp.view.IChildView;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by gavinandre on 18-3-18.
 */

public class ChildActivity extends AbstractMvpActivity<IChildView, ChildPresenter> implements
        IChildView {

    private static final String TAG = ChildActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        ButterKnife.bind(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismiss() {

    }

    @Override
    public void showData(String s) {
        Log.i(TAG, "showData: " + s);
        runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }

    @NonNull
    @Override
    public ChildPresenter createPresenter() {
        return new ChildPresenter(this);
    }

    @Override
    public void showMessage(String msg) {
        Log.i(TAG, "showMessage: " + msg);
        //runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        getPresenter().sendData("ChildActivity");
    }
}
