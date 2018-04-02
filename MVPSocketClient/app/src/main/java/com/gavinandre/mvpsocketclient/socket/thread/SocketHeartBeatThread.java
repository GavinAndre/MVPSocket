package com.gavinandre.mvpsocketclient.socket.thread;

import android.util.Log;

import com.gavinandre.mvpsocketclient.socket.interfaces.SocketCloseInterface;
import com.gavinandre.mvpsocketclient.socket.utils.SocketUtil;

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
    private final PrintWriter printWriter;
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
                if (!isConnected()) {
                    break;
                }

                //去除sendUrgentData,防止windows系统下发送多次后断开的问题
                //try {
                //    mSocket.sendUrgentData(0xFF);
                //} catch (IOException e) {
                //    if (socketCloseInterface != null) {
                //        socketCloseInterface.onSocketDisconnection();
                //    }
                //    break;
                //}
                if (printWriter != null) {
                    synchronized (printWriter) {
                        SocketUtil.write2Stream("ping", printWriter);
                    }
                }
                //Log.i(TAG, "run: SocketHeartBeatThread");
                try {
                    Thread.sleep(REPEAT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            //循环结束则退出输入流
            if (printWriter != null) {
                synchronized (printWriter) {
                    SocketUtil.closePrintWriter(printWriter);
                }
            }
            currentThread.setName(oldName);
            Log.i(TAG, "SocketHeartBeatThread finish");
        }
    }

    /**
     * 判断本地socket连接状态
     */
    private boolean isConnected() {
        if (mSocket.isClosed() || !mSocket.isConnected() ||
                mSocket.isInputShutdown() || mSocket.isOutputShutdown()) {
            if (socketCloseInterface != null) {
                socketCloseInterface.onSocketDisconnection();
            }
            return false;
        }
        return true;
    }

    public void close() {
        isCancel = true;
        if (printWriter != null) {
            synchronized (printWriter) {
                SocketUtil.closePrintWriter(printWriter);
            }
        }
    }

}
