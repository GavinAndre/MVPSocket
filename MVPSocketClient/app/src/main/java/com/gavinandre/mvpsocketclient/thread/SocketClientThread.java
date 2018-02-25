package com.gavinandre.mvpsocketclient.thread;

import android.util.Log;

import com.gavinandre.mvpsocketclient.interfaces.SocketClientResponseInterface;
import com.gavinandre.mvpsocketclient.utils.SocketUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import javax.net.SocketFactory;

/**
 * 写数据采用死循环，没有数据时wait，有新消息时notify
 * Created by gavinandre on 18-1-8.
 */
public class SocketClientThread extends Thread {

    private static final String TAG = SocketClientThread.class.getSimpleName();

    private static final int SUCCESS = 100;
    private static final int FAILED = -1;
    private volatile String name;

    private boolean isLongConnection = true;
    private boolean isReConnect = true;
    private SocketSendThread mSocketSendThread;
    private SocketReceiveThread mSocketReceiveThread;
    private SocketHeartBeatThread mSocketHeartBeatThread;
    private Socket mSocket;

    private CountDownLatch latch = new CountDownLatch(1);

    private boolean isSocketAvailable;
    private boolean closeSendTask;

    private SocketClientResponseInterface socketClientResponseInterface;

    protected volatile ConcurrentLinkedQueue<String> dataQueue = new ConcurrentLinkedQueue<>();

    public SocketClientThread(String name, SocketClientResponseInterface socketClientResponseInterface) {
        this.name = name;
        this.socketClientResponseInterface = socketClientResponseInterface;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName("Processing-" + name);
        try {
            initSocket();
            Log.i(TAG, "run: SocketClientThread end");
        } finally {
            currentThread.setName(oldName);
        }
    }

    /**
     * 初始化socket客户端
     */
    private void initSocket() {
        try {
            mSocket = SocketFactory.getDefault().createSocket();
            SocketAddress socketAddress = new InetSocketAddress(SocketUtil.ADDRESS, SocketUtil.PORT);
            mSocket.connect(socketAddress, 10000);

            isSocketAvailable = true;
            closeSendTask = false;

            //开启接收线程
            mSocketReceiveThread = new SocketReceiveThread("SocketReceiveThread");
            mSocketReceiveThread.bufferedReader = new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
            mSocketReceiveThread.start();

            //开启发送线程
            mSocketSendThread = new SocketSendThread("SocketSendThread");
            mSocketSendThread.printWriter = new PrintWriter(mSocket.getOutputStream(), true);
            mSocketSendThread.start();

            //开启心跳线程
            if (isLongConnection) {
                mSocketHeartBeatThread = new SocketHeartBeatThread("SocketHeartBeatThread");
                mSocketHeartBeatThread.printWriter = new PrintWriter(mSocket.getOutputStream(), true);
                mSocketHeartBeatThread.start();
            }

            if (socketClientResponseInterface != null) {
                socketClientResponseInterface.onSocketConnect();
            }
        } catch (ConnectException e) {
            failedMessage("服务器连接异常，请检查网络", FAILED);
            e.printStackTrace();
            stopThread();
        } catch (IOException e) {
            failedMessage("网络发生异常，请稍后重试", FAILED);
            e.printStackTrace();
            stopThread();
        }
    }

    /**
     * 发送消息
     */
    public void addRequest(String data) {
        dataQueue.add(data);
        //有新增待发送数据，则唤醒发送线程
        toNotifyAll(dataQueue);
    }

    /**
     * 关闭socket客户端
     */
    public synchronized void stopThread() {
        //关闭接收线程
        closeReceiveTask();
        //唤醒发送线程并关闭
        wakeSendTask();
        //关闭心跳线程
        closeHeartBeatTask();
        //关闭socket
        closeSocket();
        //清除数据
        clearData();
        failedMessage("断开连接", FAILED);
        if (isReConnect) {
            toWait(this, 15000);
            initSocket();
            Log.i(TAG, "stopThread: " + Thread.currentThread().getName());
        }
    }

    /**
     * 唤醒发送线程并关闭
     */
    private void wakeSendTask() {
        try {
            closeSendTask = true;
            toNotifyAll(dataQueue);
            if (mSocketSendThread != null && mSocketSendThread.isAlive()) {
                latch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭接收线程
     */
    private void closeReceiveTask() {
        if (mSocketReceiveThread != null) {
            mSocketReceiveThread.interrupt();
            mSocketReceiveThread.isCancel = true;
            if (mSocketReceiveThread.bufferedReader != null) {
                try {
                    if (isSocketAvailable && !mSocket.isClosed() && mSocket.isConnected()) {
                        //解决java.net.SocketException问题，需要先shutdownInput
                        mSocket.shutdownInput();
                        SocketUtil.closeBufferedReader(mSocketReceiveThread.bufferedReader);
                    }
                    mSocketReceiveThread.bufferedReader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            mSocketReceiveThread = null;
        }
    }

    /**
     * 关闭发送线程
     */
    private void closeSendTask() {
        if (mSocketSendThread != null) {
            mSocketSendThread.isCancel = true;
            mSocketSendThread.interrupt();
            if (mSocketSendThread.printWriter != null) {
                if (isSocketAvailable && !mSocket.isClosed() && mSocket.isConnected()) {
                    //防止写数据时停止，写完再停
                    synchronized (mSocketSendThread.printWriter) {
                        SocketUtil.closePrintWriter(mSocketSendThread.printWriter);
                        mSocketSendThread.printWriter = null;
                    }
                }
            }
            mSocketSendThread = null;
        }
    }

    /**
     * 关闭心跳线程
     */
    private void closeHeartBeatTask() {
        if (mSocketHeartBeatThread != null) {
            mSocketHeartBeatThread.isCancel = true;
            if (mSocketHeartBeatThread.printWriter != null) {
                SocketUtil.closePrintWriter(mSocketHeartBeatThread.printWriter);
                mSocketHeartBeatThread.printWriter = null;
            }
            mSocketHeartBeatThread = null;
        }
    }

    /**
     * 关闭socket
     */
    private void closeSocket() {
        if (mSocket != null) {
            if (!mSocket.isClosed() && mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isSocketAvailable = false;
            mSocket = null;
        }
    }

    /**
     * 清除数据
     */
    private void clearData() {
        dataQueue.clear();
    }

    /**
     * 阻塞线程,millis为0则永久阻塞,知道调用notify()
     */
    private void toWait(Object o, long millis) {
        synchronized (o) {
            try {
                o.wait(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * notify()调用后，并不是马上就释放对象锁的，而是在相应的synchronized(){}语句块执行结束，自动释放锁后
     *
     * @param o
     */
    protected void toNotifyAll(Object o) {
        synchronized (o) {
            o.notifyAll();
        }
    }

    /**
     * 连接失败回调
     */
    private void failedMessage(String msg, int code) {
        if (socketClientResponseInterface != null) {
            socketClientResponseInterface.onSocketDisable(msg, code);
        }
    }

    /**
     * 接收消息回调
     */
    private void successMessage(String data) {
        if (socketClientResponseInterface != null) {
            socketClientResponseInterface.onSocketReceive(data, SUCCESS);
        }
    }

    /**
     * 判断本地socket连接状态
     */
    private boolean isConnected() {
        if (mSocket.isClosed() || !mSocket.isConnected()) {
            SocketClientThread.this.stopThread();
            return false;
        }
        return true;
    }

    /**
     * 数据接收线程
     */
    public class SocketReceiveThread extends Thread {

        private volatile String name;

        private volatile boolean isCancel = false;
        private BufferedReader bufferedReader;

        public SocketReceiveThread(String name) {
            this.name = name;
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

                    if (bufferedReader != null) {
                        String receiverData = SocketUtil.readFromStream(bufferedReader);
                        if (receiverData != null) {
                            successMessage(receiverData);
                        } else {
                            Log.i(TAG, "run: receiverData==null");
                            break;
                        }
                    }
                }
            } finally {
                //循环结束则退出输入流
                SocketUtil.closeBufferedReader(bufferedReader);
                currentThread.setName(oldName);
            }
        }
    }

    /**
     * 数据发送线程,当没有发送数据时让线程等待
     */
    public class SocketSendThread extends Thread {

        private volatile String name;

        private volatile boolean isCancel = false;
        private PrintWriter printWriter;

        public SocketSendThread(String name) {
            this.name = name;
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

                    String dataContent = dataQueue.poll();
                    if (dataContent == null) {
                        //没有发送数据则等待
                        toWait(dataQueue, 0);
                        if (closeSendTask) {
                            //notify()调用后，并不是马上就释放对象锁的，所以在此处中断发送线程
                            closeSendTask();
                            latch.countDown();
                        }
                    } else if (printWriter != null) {
                        synchronized (printWriter) {
                            Log.i(TAG, "before: write2Stream");
                            SocketUtil.write2Stream(dataContent, printWriter);
                            Log.i(TAG, "after: write2Stream");
                        }
                    }
                }
            } finally {
                //循环结束则退出输出流
                SocketUtil.closePrintWriter(printWriter);
                currentThread.setName(oldName);
            }
        }
    }

    /**
     * 心跳实现，频率5秒
     */
    public class SocketHeartBeatThread extends Thread {

        private volatile String name;

        private static final int REPEAT_TIME = 5000;
        private boolean isCancel = false;
        private PrintWriter printWriter;

        public SocketHeartBeatThread(String name) {
            this.name = name;
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

                    try {
                        mSocket.sendUrgentData(0xFF);
                    } catch (IOException e) {
                        isSocketAvailable = false;
                        Log.e(TAG, "before: SocketHeartBeatThread stopThread");
                        SocketClientThread.this.stopThread();
                        Log.e(TAG, "after: SocketHeartBeatThread stopThread");
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
    }

    /**
     * 设置是否断线重连
     */
    public void setReConnect(boolean reConnect) {
        isReConnect = reConnect;
    }

}
