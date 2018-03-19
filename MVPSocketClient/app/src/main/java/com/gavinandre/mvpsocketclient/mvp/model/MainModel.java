package com.gavinandre.mvpsocketclient.mvp.model;

import com.gavinandre.mvpsocketclient.mvp.base.BaseModel;
import com.gavinandre.mvpsocketclient.mvp.presenter.MainPresenter;
import com.gavinandre.mvpsocketclient.socket.SocketClient;
import com.gavinandre.mvpsocketclient.socket.interfaces.SocketClientResponseInterface;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by gavinandre on 18-1-7.
 */
public class MainModel extends BaseModel<MainPresenter> implements SocketClientResponseInterface {

    private static final String TAG = MainModel.class.getSimpleName();
    private static SocketClient socketClient;

    public MainModel(MainPresenter presenter) {
        super(presenter);
        if (socketClient == null) {
            socketClient = new SocketClient(this);
        }
    }

    public <T> void sendData(T data) {
        if (socketClient != null) {
            socketClient.sendData(data);
        }
    }

    public void stopSocket() {
        if (socketClient != null) {
            socketClient.stopSocket();
            socketClient = null;
        }
    }

    @Override
    public void onSocketConnect() {
        //mPresenter.showData("连接成功");
        EventBus.getDefault().post("连接成功");
        //Log.i(TAG, "onSocketConnect: 连接成功");
    }

    @Override
    public void onSocketReceive(Object socketResult, int code) {
        //mPresenter.showData("收到消息 ,  data: " + socketResult + " , code: " + code);
        EventBus.getDefault().post("收到消息 ,  data: " + socketResult + " , code: " + code);
        //Log.i(TAG, "onSocketReceive: 收到消息 ,  data: " + socketResult + " , code: " + code);
    }

    @Override
    public void onSocketDisable(String msg, int code) {
        mPresenter.showMessage("连接断开 , msg: " + msg + " , code: " + code);
        //Log.i(TAG, "onSocketDisable: 连接断开 , msg: " + msg + " , code: " + code);
    }
}
