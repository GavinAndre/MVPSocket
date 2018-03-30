package com.gavinandre.mvpsocketclient.mvp.model;

import com.gavinandre.mvpsocketclient.mvp.presenter.MainPresenter;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by gavinandre on 18-1-7.
 */
public class MainModel extends BaseSocketModel<MainPresenter> {

    private static final String TAG = MainModel.class.getSimpleName();

    public MainModel(MainPresenter presenter) {
        super(presenter);
    }

    @Override
    public void onSocketConnect() {
        //if (mPresenter != null) {
        //    mPresenter.showData("连接成功");
        //}
        EventBus.getDefault().post("连接成功");
    }

    @Override
    public void onSocketReceive(Object socketResult, int code) {
        //if (mPresenter != null) {
        //    mPresenter.showData("收到消息 ,  data: " + socketResult + " , code: " + code);
        //}
        //发送消息给childModel
        EventBus.getDefault().post("收到消息 ,  data: " + socketResult + " , code: " + code);
    }

    @Override
    public void onSocketDisable(String msg, int code) {
        //if (mPresenter != null) {
        //    mPresenter.showMessage("连接断开 , msg: " + msg + " , code: " + code);
        //}
        EventBus.getDefault().post("连接断开 ,  msg: " + msg + " , code: " + code);
    }

    @Override
    public void detachModel() {
        stopSocket();
        super.detachModel();
    }
}
