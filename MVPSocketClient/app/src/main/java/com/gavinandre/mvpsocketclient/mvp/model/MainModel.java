package com.gavinandre.mvpsocketclient.mvp.model;

import com.gavinandre.mvpsocketclient.socket.interfaces.SocketClientResponseInterface;
import com.gavinandre.mvpsocketclient.mvp.base.BaseModel;
import com.gavinandre.mvpsocketclient.mvp.presenter.MainPresenter;
import com.gavinandre.mvpsocketclient.socket.SocketClient;

/**
 * Created by gavinandre on 18-1-7.
 */
public class MainModel extends BaseModel<MainPresenter> implements SocketClientResponseInterface {

    private static final String TAG = MainModel.class.getSimpleName();
    //private SocketClientThread socketClientThread;
    private SocketClient socketClient;

    public MainModel(MainPresenter presenter) {
        super(presenter);
        socketClient = new SocketClient(this);
        //socketClientThread = new SocketClientThread("socketClientThread", this);
        //ThreadPoolUtil.getInstance().addExecuteTask(socketClientThread);
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
        //String s = (String) data;
        //if (TextUtils.isEmpty(s)) {
        //    mPresenter.showMessage("消息不能为空");
        //    return;
        //}
        //if (socketClientThread != null) {
        //    socketClientThread.sendMsg(s);
        //}

        if (socketClient != null) {
            socketClient.sendData(data);
        }
    }

    public void stopSocket() {
        //一定要在子线程内执行关闭socket等IO操作
        //ThreadPoolUtil.getInstance().addExecuteTask(() -> {
        //    socketClientThread.setReConnect(false);
        //    socketClientThread.stopThread();
        //});
        //ThreadPoolUtil.getInstance().shutdown();

        if (socketClient != null) {
            socketClient.stopSocket();
        }
    }
}
