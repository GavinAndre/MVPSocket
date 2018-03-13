package com.gavinandre.mvpsocketclient.socket.thread;

import com.gavinandre.mvpsocketclient.socket.utils.SocketUtil;
import com.gavinandre.mvpsocketclient.socket.interfaces.SocketCloseInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 心跳实现，频率5秒
 * Created by gavinandre on 18-3-13.
 */
public class SocketHeartBeatThread extends Thread {

    private static final String TAG = SocketHeartBeatThread.class.getSimpleName();

    private volatile String name;

    private static final int REPEAT_TIME = 5000;
    private boolean isCancel = false;
    private PrintWriter printWriter;
    private Socket mSocket;

    private SocketCloseInterface socketCloseInterface;

    public SocketHeartBeatThread(String name, PrintWriter printWriter,
                                 Socket mSocket, SocketCloseInterface socketCloseInterface) {
        this.name = name;
        this.printWriter = printWriter;
        this.mSocket = mSocket;
        this.socketCloseInterface = socketCloseInterface;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName("Processing-" + name);
        try {
            while (!isCancel) {
                //if (!isConnected()) {
                //    break;
                //}

                try {
                    mSocket.sendUrgentData(0xFF);
                } catch (IOException e) {
                    if (socketCloseInterface != null) {
                        socketCloseInterface.onSocketDisconnection();
                    }
                    break;
                }

                if (printWriter != null) {
                    SocketUtil.write2Stream("ping", printWriter);
                }

                try {
                    Thread.sleep(REPEAT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            //循环结束则退出输入流
            SocketUtil.closePrintWriter(printWriter);
            currentThread.setName(oldName);
        }
    }

    public void close() {
        isCancel = true;
        if (printWriter != null) {
            SocketUtil.closePrintWriter(printWriter);
            printWriter = null;
        }
    }

}
