package com.gavinandre.mvpsocketclient.socket;

import android.text.TextUtils;
import android.util.Log;

import com.gavinandre.mvpsocketclient.socket.interfaces.SocketClientResponseInterface;
import com.gavinandre.mvpsocketclient.socket.thread.SocketClientThread;

/**
 * Created by gavinandre on 18-3-13.
 */

public class SocketClient {

    private static final String TAG = SocketClient.class.getSimpleName();

    private SocketClientThread socketClientThread;

    public SocketClient(SocketClientResponseInterface socketClientResponseInterface) {
        socketClientThread = new SocketClientThread("socketClientThread", socketClientResponseInterface);
        socketClientThread.start();
        //ThreadPoolUtil.getInstance().addExecuteTask(socketClientThread);
    }

    public <T> void sendData(T data) {
        //convert to string or serialize object
        String s = (String) data;
        if (TextUtils.isEmpty(s)) {
            Log.i(TAG, "sendData: 消息不能为空");
            return;
        }
        if (socketClientThread != null) {
            socketClientThread.sendMsg(s);
        }
    }

    public void stopSocket() {
        //一定要在子线程内执行关闭socket等IO操作
        new Thread(() -> {
            socketClientThread.setReConnect(false);
            socketClientThread.stopThread();
        }).start();
        //ThreadPoolUtil.getInstance().addExecuteTask(() -> {
        //    socketClientThread.setReConnect(false);
        //    socketClientThread.stopThread();
        //});
        //ThreadPoolUtil.getInstance().shutdown();
    }
}
