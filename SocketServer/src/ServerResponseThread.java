import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by gavinandre on 18-2-24.
 */
public class ServerResponseThread implements Runnable {

    private ReceiveThread receiveThread;
    private SendThread sendThread;
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
                        SocketUtil.closePrintWriter(sendThread.printWriter);
                        sendThread.printWriter = null;
                    }
                }
                sendThread = null;
                System.out.println("stop sendThread");
            }
            onLineClient.remove(userIP);
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
                        if (msg.contains("ping")) {
                            // System.out.println("收到心跳包");
                            lastReceiveTime = System.currentTimeMillis();
                            // socketServerResponseInterface.clientOnline(userIP);
                        } else {
                            // msg = "用户" + userIP + " : " + msg;
                            System.out.println(msg);
                            // addMessage(msg);
                            // socketServerResponseInterface.clientOnline(userIP);
                            if (msg.contains("test_fw_version")) {
                                addMessage("{\"action\":\"test_fw_version\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"version\":\"1.1\"},{\"deviceCode\":\"20033002\",\"version\":\"1.2\"},{\"deviceCode\":\"20033003\",\"version\":\"1.3\"},{\"deviceCode\":\"20033004\",\"version\":\"1.4\"}]}}");
                            } else if (msg.contains("test_battery_capacity")) {
                                addMessage("{\"action\":\"test_battery_capacity\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"battery\":60}]}}");
                            } else if (msg.contains("test_power")) {
                                addMessage("{\"action\":\"test_power\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"plugPower\":true}]}}");
                            } else if (msg.contains("test_magnet")) {
                                addMessage("{\"action\":\"test_magnet\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"magnetState\":\"open\",\"time\":\"2020-03-29T16:15:19.843+0000\"}]}}");
                            } else if (msg.contains("test_take_photo")) {
                                addMessage("{\"action\":\"test_take_photo\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"image\":\"base64\",\"time\":\"2020-03-29T16:15:19.843+0000\"}]}}");
                            } else if (msg.contains("test_upload_photo")) {
                                addMessage("{\"action\":\"test_upload_photo\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"upload\":true}]}}");
                            } else if (msg.contains("test_temp")) {
                                addMessage("{\"action\":\"test_temp\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"fridgeTemp\":35}]}}");
                            } else if (msg.contains("test_4g_signal")) {
                                addMessage("{\"action\":\"test_4g_signal\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"dbm\":-79,\"connectionState\":true}]}}");
                            } else if (msg.contains("test_get_location") && msg.contains("\"type\":1")) {
                                addMessage("{\"action\":\"test_get_location\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"longitude\":22.661409,\"latitude\":114.03497,\"accuracy\":50,\"time\":\"2020-03-30T01:07:25.668+0800\",\"type\":1}]}}");
                            } else if (msg.contains("test_get_location") && msg.contains("\"type\":2")) {
                                addMessage("{\"action\":\"test_get_location\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"longitude\":22.661409,\"latitude\":114.03497,\"accuracy\":50,\"time\":\"2020-03-30T01:07:25.668+0800\",\"type\":2}]}}");
                            } else if (msg.contains("test_upload_location")) {
                                addMessage("{\"action\":\"test_upload_location\",\"data\":{\"deviceItems\":[{\"deviceCode\":\"20033001\",\"upload\":true}]}}");
                            } else if (msg.contains("iot_registry") && msg.contains("\"type\":0")) {
                                Thread.sleep(1000);
                                if (new Random().nextBoolean()) {
                                    addMessage("{\"RequestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"Action\":\"iot_registry\",\"Code\":0,\"Message\":\"register success.\",\"Data\":{\"SmartId\":\"45ec0230-6f17-4df7-b9f1-0e72904423ae\",\"SmartCode\":\"SW0320200327104747731949\"}}");
                                } else {
                                    addMessage("{\"RequestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"Action\":\"iot_registry\",\"Code\":1,\"Message\":\"fail msg from server\"}");
                                }
                            } else if (msg.contains("iot_registry") && msg.contains("\"type\":1")) {
                                Thread.sleep(1000);
                                if (new Random().nextBoolean()) {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"iot_registry\",\"code\":0,\"message\":\"register success.\",\"data\":{\"smartId\":\"45ec0230-6f17-4df7-b9f1-0e72904423ae\",\"smartCode\":\"SW0320200327104747731949\"}}");
                                } else {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"iot_registry\",\"code\":1,\"message\":\"fail msg from server\"}");
                                }
                            } else if (msg.contains("iot_replace") && msg.contains("\"replaceType\":0")) {
                                Thread.sleep(1000);
                                if (new Random().nextBoolean()) {
                                    addMessage("{\"RequestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"Action\":\"iot_replace\",\"Code\":0,\"Message\":\"register success.\"}");
                                } else {
                                    addMessage("{\"RequestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"Action\":\"iot_replace\",\"Code\":1,\"Message\":\"fail msg from server\"}");
                                }
                            } else if (msg.contains("iot_replace") && msg.contains("\"replaceType\":1")) {
                                Thread.sleep(1000);
                                if (new Random().nextBoolean()) {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"iot_replace\",\"code\":0,\"message\":\"register success.\",\"data\":{\"smartId\":\"45ec0230-6f17-4df7-b9f1-0e72904423ae\",\"smartCode\":\"SW0320200327104747731949\"}}");
                                } else {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"iot_replace\",\"code\":1,\"message\":\"fail msg from server\"}");
                                }
                            } else if (msg.contains("get_registry_info")) {
                                if (new Random().nextBoolean()) {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"get_registry_info\",\"code\":0,\"data\":{\"smartId\":\"45ec0230-6f17-4df7-b9f1-0e72904423ae\",\"type\":0,\"hostCode\":\"babdbcbcbdbdbd\",\"hostType\":1,\"latitude\":0,\"longitude\":0,\"smartType\":4,\"deviceItems\":[{\"deviceCode\":\"S30JSA0117D20030014\",\"deviceType\":2,\"devicePosition\":0,\"deviceLocation\":0},{\"deviceCode\":\"S30JSA0117D20030015\",\"deviceType\":2,\"devicePosition\":0,\"deviceLocation\":0},{\"deviceCode\":\"S30SZA0228P2003000E-E688\",\"deviceType\":6}],\"iotInfo\":{\"env\":\"dev\",\"idScope\":\"0cn0000990D\",\"globalDeviceEndpoint\":\"global.azure-devices-provisioning.cn\",\"groupPrimaryKey\":\"vMe4Yzb3ILOgiafKhU3enhXIUbW8WWQkR/SnxI3axiwv9JpD0wUV1PtAs+ou5JNkv0+OyOdfWHq+CvmlbkO5+A==\",\"groupSecondaryKey\":\"A9A37P0HZDAD5OiE9i229JEyeUoTCe3vOyTL4RW0Ju7Fdg4yVbTEzLSob1dhuF4XxkN0beOJZSVuF/hBZTeLzg==\"}}}");
                                } else {
                                    addMessage("{\"requestId\":\"46b3b269-9dc3-42e5-a880-3cd021314750\",\"action\":\"get_registry_info\",\"code\":1,\"message\":\"not registry\"}");
                                }
                            }
                        }
                    } else {
                        System.out.println("client is offline...");
                        ServerResponseThread.this.stop();
                        socketServerResponseInterface.clientOffline();
                        break;
                    }
                    // System.out.println("ReceiveThread");
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
                    // System.out.println("SendThread");
                }

                SocketUtil.outputStreamShutdown(socket);
                SocketUtil.closePrintWriter(printWriter);
                System.out.println("SendThread is finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}