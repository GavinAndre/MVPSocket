import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by gavinandre on 18-2-24.
 */
public class ServerResponseThread implements Runnable {

    private ReceiveThread receiveThread;
    private SendThread sendThread;
    private SocketStatusThread socketStatusThread;
    private Socket socket;
    private ResponseCallback tBack;

    private volatile ConcurrentLinkedQueue<String> dataQueue = new ConcurrentLinkedQueue<>();
    // private static ConcurrentHashMap<String, Socket> onLineClient = new ConcurrentHashMap<>();
    private static List<Socket> onLineClient = new ArrayList<Socket>();

    private long lastReceiveTime = System.currentTimeMillis();

    private String userIP;

    public String getUserIP() {
        return userIP;
    }

    public ServerResponseThread(Socket socket, ResponseCallback tBack) {
        this.socket = socket;
        this.tBack = tBack;
        this.userIP = socket.getInetAddress().getHostAddress();
        // onLineClient.put(userIP, socket);
        onLineClient.add(socket);
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

    public void addMessage(String data) {
        if (!isConnected()) {
            return;
        }

        dataQueue.offer(data);
        toNotifyAll(dataQueue);//有新增待发送数据，则唤醒发送线程
    }

    public Socket getConnectdClient(String clientID) {
        // return onLineClient.get(clientID);
        return onLineClient.get(0);
    }

    /**
     * 打印已经链接的客户端
     */
    // public static void printAllClient() {
    //     if (onLineClient == null) {
    //         return;
    //     }
    //     Iterator<String> inter = onLineClient.keySet().iterator();
    //     while (inter.hasNext()) {
    //         System.out.println("client:" + inter.next());
    //     }
    // }
    public void toWaitAll(Object o) {
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toNotifyAll(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    private boolean isConnected() {
        if (socket.isClosed() || !socket.isConnected()) {
            onLineClient.remove(userIP);
            ServerResponseThread.this.stop();
            System.out.println("socket closed...");
            return false;
        }
        return true;
    }

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
                            tBack.targetIsOffline();
                            break;
                        } else if ("ping".equals(msg)) {
                            System.out.println("收到心跳包");
                            lastReceiveTime = System.currentTimeMillis();
                            tBack.targetIsOnline(userIP);
                        } else {
                            msg = "用户" + userIP + " : " + msg;
                            System.out.println(msg);
                            addMessage(msg);
                            tBack.targetIsOnline(userIP);
                        }
                    } else {
                        System.out.println("client is offline...");
                        ServerResponseThread.this.stop();
                        tBack.targetIsOffline();
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