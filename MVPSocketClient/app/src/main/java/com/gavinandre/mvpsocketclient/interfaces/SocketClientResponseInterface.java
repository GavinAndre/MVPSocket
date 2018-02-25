package com.gavinandre.mvpsocketclient.interfaces;

/**
 * Created by gavinandre on 18-2-25.
 */
public interface SocketClientResponseInterface<T> {

    void onSocketConnect();

    void onSocketReceive(T socketResult, int code);

    void onSocketDisable(String msg, int code);

}
