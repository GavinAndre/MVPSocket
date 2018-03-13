package com.gavinandre.mvpsocketclient.socket.interfaces;

/**
 * Created by gavinandre on 18-2-25.
 */
public interface SocketCloseInterface {

    /**
     * 客户端收到服务端消息回调
     */
    void onSocketShutdownInput();

    /**
     * 客户端关闭回调
     */
    void onSocketDisconnection();
}
