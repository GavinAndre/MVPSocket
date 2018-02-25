package com.gavinandre.mvpsocketclient.mvp.model;

import android.text.TextUtils;

import com.gavinandre.mvpsocketclient.mvp.base.BaseModel;
import com.gavinandre.mvpsocketclient.mvp.presenter.MainPresenter;
import com.gavinandre.mvpsocketclient.thread.SocketClientThread;
import com.gavinandre.mvpsocketclient.utils.SocketUtil;
import com.gavinandre.mvpsocketclient.utils.ThreadPoolUtil;

/**
 * Created by gavinandre on 18-1-7.
 */
public class MainModel extends BaseModel<MainPresenter> implements SocketUtil.SocketConnectInterface,
        SocketUtil.SocketReceiveInterface, SocketUtil.SocketDisableInterface {

    private static final String TAG = MainModel.class.getSimpleName();
    private SocketClientThread socketClientThread;

    public MainModel(MainPresenter presenter) {
        super(presenter);
        socketClientThread = new SocketClientThread("socketClientThread");
        socketClientThread.setSocketConnectInterface(this);
        socketClientThread.setSocketReceiveInterface(this);
        socketClientThread.setSocketDisableInterface(this);
        ThreadPoolUtil.getInstance().addExecuteTask(socketClientThread);
    }

    @Override
    public void onSocketConnect() {
        mPresenter.showData("连接成功");
    }

    @Override
    public void onSocketReceive(Object socketResult, int code) {
        mPresenter.showData("收到消息 ,  data: " + socketResult + " , code: " + code);
    }

    @Override
    public void onSocketDisable(String msg, int code) {
        mPresenter.showMessage("连接断开 , msg: " + msg + " , code: " + code);
    }

    public <T> void sendData(T data) {
        //convert to string or serialize object
        String s = (String) data;
        if (TextUtils.isEmpty(s)) {
            mPresenter.showMessage("消息不能为空");
            return;
        }
        if (socketClientThread != null) {
            socketClientThread.addRequest(s);
        }
    }

    public void stopSocket() {
        ThreadPoolUtil.getInstance().addExecuteTask(() -> {
            socketClientThread.setReConnect(false);
            socketClientThread.stopThread();
        });
        ThreadPoolUtil.getInstance().shutdown();
    }
}
