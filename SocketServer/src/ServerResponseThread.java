import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by gavinandre on 18-2-24.
 */
public class ServerResponseThread implements Runnable {

    private ReceiveThread receiveThread;
    private SendThread sendThread;
    private SocketStatusThread socketStatusThread;
    private Socket socket;
    private SocketServerResponseInterface socketServerResponseInterface;

    private volatile ConcurrentLinkedQueue<String> dataQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Socket> onLineClient = new ConcurrentHashMap<>();

    private long lastReceiveTime = System.currentTimeMillis();

    private String userIP;

    public String getUserIP() {
        return userIP;
    }

    public ServerResponseThread(Socket socket, SocketServerResponseInterface socketServerResponseInterface) {
        this.socket = socket;
        this.socketServerResponseInterface = socketServerResponseInterface;
        this.userIP = socket.getInetAddress().getHostAddress();
        onLineClient.put(userIP, socket);
        System.out.println("用户：" + userIP
                + " 加入了聊天室,当前在线人数:" + onLineClient.size());
    }

    @Override
    public void run() {
        try {
            //开启接收线程
            receiveThread = new ReceiveThread();
            receiveThread.bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            receiveThread.start();

            //开启发送线程
            sendThread = new SendThread();
            sendThread.printWriter = new PrintWriter(socket.getOutputStream(), true);
            sendThread.start();

            //开启判断心跳包超时线程
            socketStatusThread = new SocketStatusThread();
            socketStatusThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开socket连接
     */
    public void stop() {
        try {
            System.out.println("stop");
            if (receiveThread != null) {
                receiveThread.isCancel = true;
                receiveThread.interrupt();
                if (receiveThread.bufferedReader != null) {
                    SocketUtil.inputStreamShutdown(socket);
                    System.out.println("before closeBufferedReader");
                    SocketUtil.closeBufferedReader(receiveThread.bufferedReader);
                    System.out.println("after closeBufferedReader");
                    receiveThread.bufferedReader = null;
                }
                receiveThread = null;
                System.out.println("stop receiveThread");
            }

            if (sendThread != null) {
                sendThread.isCancel = true;
                toNotifyAll(sendThread);
                sendThread.interrupt();
                if (sendThread.printWriter != null) {
                    //防止写数据时停止，写完再停
                    synchronized (sendThread.printWriter) {
                        sendThread.printWriter = null;
                    }
                }
                sendThread = null;
                System.out.println("stop sendThread");
            }
            if (socketStatusThread != null) {
                socketStatusThread.isCancel = true;
                toNotifyAll(socketStatusThread);
                socketStatusThread.interrupt();
                System.out.println("stop socketStatusThread");
            }
            onLineClient.remove(socket);
            System.out.println("用户：" + userIP
                    + " 退出,当前在线人数:" + onLineClient.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     */
    public void addMessage(String data) {
        if (!isConnected()) {
            return;
        }

        dataQueue.offer(data);
        //有新增待发送数据，则唤醒发送线程
        toNotifyAll(dataQueue);
    }

    /**
     * 获取已接连的客户端
     */
    public Socket getConnectdClient(String clientID) {
        return onLineClient.get(clientID);
    }

    /**
     * 打印已经连接的客户端
     */
    public static void printAllClient() {
        if (onLineClient == null) {
            return;
        }
        Iterator<String> inter = onLineClient.keySet().iterator();
        while (inter.hasNext()) {
            System.out.println("client:" + inter.next());
        }
    }

    /**
     * 阻塞线程,millis为0则永久阻塞,知道调用notify()
     */
    public void toWaitAll(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * notify()调用后，并不是马上就释放对象锁的，而是在相应的synchronized(){}语句块执行结束，自动释放锁后
     */
    public void toNotifyAll(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    /**
     * 判断本地socket连接状态
     */
    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            onLineClient.remove(userIP);
            ServerResponseThread.this.stop();
            System.out.println("socket closed...");
            return false;
        }
        return true;
    }

    /**
     * 数据接收线程
     */
    public class ReceiveThread extends Thread {

        private BufferedReader bufferedReader;
        private boolean isCancel;

        @Override
        public void run() {
            try {
                while (!isCancel) {
                    if (!isConnected()) {
                        isCancel = true;
                        break;
                    }

                    String msg = SocketUtil.readFromStream(bufferedReader);
                    if (msg != null) {
                        if ("bye".equals(msg)) {
                            ServerResponseThread.this.stop();
                            System.out.println("用户" + userIP + " : bye");
                            socketServerResponseInterface.clientOffline();
                            break;
                        } else if ("ping".equals(msg)) {
                            System.out.println("收到心跳包");
                            lastReceiveTime = System.currentTimeMillis();
                            socketServerResponseInterface.clientOnline(userIP);
                        } else {
                            msg = "用户" + userIP + " : " + msg;
                            System.out.println(msg);
                            addMessage(msg);
                            socketServerResponseInterface.clientOnline(userIP);
                        }
                    } else {
                        System.out.println("client is offline...");
                        ServerResponseThread.this.stop();
                        socketServerResponseInterface.clientOffline();
                        break;
                    }
                    System.out.println("ReceiveThread");
                }

                SocketUtil.inputStreamShutdown(socket);
                SocketUtil.closeBufferedReader(bufferedReader);
                System.out.println("ReceiveThread is finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据发送线程,当没有发送数据时让线程等待
     */
    public class SendThread extends Thread {

        private PrintWriter printWriter;
        private boolean isCancel;

        @Override
        public void run() {
            try {
                while (!isCancel) {
                    if (!isConnected()) {
                        isCancel = true;
                        break;
                    }

                    String msg = dataQueue.poll();
                    if (msg == null) {
                        toWaitAll(dataQueue);
                    } else if (printWriter != null) {
                        synchronized (printWriter) {
                            SocketUtil.write2Stream(msg, printWriter);
                        }
                    }
                    System.out.println("SendThread");
                }

                SocketUtil.outputStreamShutdown(socket);
                SocketUtil.closePrintWriter(printWriter);
                System.out.println("SendThread is finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 客户端状态监控线程,判断客户端心跳包是否发送
     */
    class SocketStatusThread extends Thread {

        private boolean isCancel;

        @Override
        public void run() {
            while (!isCancel) {
                if (!isConnected()) {
                    isCancel = true;
                    break;
                }
                System.out.println("SocketStatusThread");
                if (!socket.isClosed()) {
                    if (System.currentTimeMillis() - lastReceiveTime > 10000) {
                        System.out.println("timeout");
                        //关闭输入流后bufferedReader.readLine()会返回null
                        SocketUtil.inputStreamShutdown(socket);
                        break;
                    }
                }
                try {
                    synchronized (this) {
                        this.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("SocketStatusThread is finish");
        }
    }

}