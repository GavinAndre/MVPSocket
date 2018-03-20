package com.gavinandre.mvpsocketclient.mvp.model;

import com.gavinandre.mvpsocketclient.mvp.base.BaseModel;
import com.gavinandre.mvpsocketclient.socket.SocketClient;
import com.gavinandre.mvpsocketclient.socket.interfaces.SocketClientResponseInterface;

/**
 * Created by gavinandre on 18-1-7.
 */
public abstract class BaseSocketModel<SubP> extends BaseModel<SubP> implements SocketClientResponseInterface {

    private static final String TAG = BaseSocketModel.class.getSimpleName();
    private static SocketClient socketClient;

    public BaseSocketModel(SubP presenter) {
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
    }

    @Override
    public void onSocketReceive(Object socketResult, int code) {
    }

    @Override
    public void onSocketDisable(String msg, int code) {
    }

}
