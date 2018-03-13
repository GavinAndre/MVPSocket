package com.gavinandre.mvpsocketclient.socket.thread;

import android.util.Log;

import com.gavinandre.mvpsocketclient.socket.utils.SocketUtil;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by gavinandre on 18-3-13.
 * 数据发送线程,当没有发送数据时让线程等待
 */
public class SocketSendThread extends Thread {

    private static final String TAG = SocketSendThread.class.getSimpleName();

    private volatile String name;

    private volatile boolean isCancel = false;
    private boolean closeSendTask;
    private PrintWriter printWriter;

    private CountDownLatch latch = new CountDownLatch(1);

    protected volatile ConcurrentLinkedQueue<String> dataQueue = new ConcurrentLinkedQueue<>();

    public SocketSendThread(String name, PrintWriter printWriter) {
        this.name = name;
        this.printWriter = printWriter;
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

                String dataContent = dataQueue.poll();
                if (dataContent == null) {
                    //没有发送数据则等待
                    SocketUtil.toWait(dataQueue, 0);
                    if (closeSendTask) {
                        //notify()调用后，并不是马上就释放对象锁的，所以在此处中断发送线程
                        close();
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

    /**
     * 发送消息
     */
    public void sendMsg(String data) {
        dataQueue.add(data);
        //有新增待发送数据，则唤醒发送线程
        SocketUtil.toNotifyAll(dataQueue);
    }

    /**
     * 清除数据
     */
    public void clearData() {
        dataQueue.clear();
    }

    public void close() {
        isCancel = true;
        this.interrupt();
        if (printWriter != null) {
            //防止写数据时停止，写完再停
            synchronized (printWriter) {
                SocketUtil.closePrintWriter(printWriter);
                printWriter = null;
            }
        }
    }

    public void wakeSendTask() {
        try {
            closeSendTask = true;
            SocketUtil.toNotifyAll(dataQueue);
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setCloseSendTask(boolean closeSendTask) {
        this.closeSendTask = closeSendTask;
    }
}
