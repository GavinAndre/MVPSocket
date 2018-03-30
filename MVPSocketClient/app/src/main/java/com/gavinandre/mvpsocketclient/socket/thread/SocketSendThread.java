package com.gavinandre.mvpsocketclient.socket.thread;

import android.util.Log;

import com.gavinandre.mvpsocketclient.socket.utils.SocketUtil;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by gavinandre on 18-3-13.
 * 数据发送线程,当没有发送数据时让线程等待
 */
public class SocketSendThread extends Thread {

    private static final String TAG = SocketSendThread.class.getSimpleName();

    private volatile String name;

    private volatile boolean isCancel = false;
    private boolean closeSendTask;
    private final PrintWriter printWriter;

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

                String dataContent = dataQueue.poll();
                if (dataContent == null) {
                    //没有发送数据则等待
                    SocketUtil.toWait(dataQueue, 0);
                    if (closeSendTask) {
                        //notify()调用后，并不是马上就释放对象锁的，所以在此处中断发送线程
                        close();
                    }
                } else if (printWriter != null) {
                    synchronized (printWriter) {
                        SocketUtil.write2Stream(dataContent, printWriter);
                    }
                }
            }
        } finally {
            //循环结束则退出输出流
            if (printWriter != null) {
                synchronized (printWriter) {
                    SocketUtil.closePrintWriter(printWriter);
                }
            }
            currentThread.setName(oldName);
            Log.i(TAG, "SocketSendThread finish");
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
            }
        }
    }

    public void wakeSendTask() {
        closeSendTask = true;
        SocketUtil.toNotifyAll(dataQueue);
    }

    public void setCloseSendTask(boolean closeSendTask) {
        this.closeSendTask = closeSendTask;
    }
}
