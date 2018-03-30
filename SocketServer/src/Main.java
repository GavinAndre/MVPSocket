import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gavinandre on 18-2-24.
 */
public class Main {

    private static boolean isStart = true;
    private static ServerResponseThread serverResponseThread;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        System.out.println("服务端 " + SocketUtil.getIP() + " 运行中...\n");
        try {
            serverSocket = new ServerSocket(SocketUtil.PORT);
            while (isStart) {
                Socket socket = serverSocket.accept();
                //设定输入流读取阻塞超时时间(10秒收不到客户端消息判定断线)
                socket.setSoTimeout(10000);
                serverResponseThread = new ServerResponseThread(socket,
                        new SocketServerResponseInterface() {

                            @Override
                            public void clientOffline() {// 对方不在线
                                System.out.println("offline");
                            }

                            @Override
                            public void clientOnline(String clientIp) {
                                System.out.println(clientIp + " is online");
                                System.out.println("-----------------------------------------");
                            }
                        });

                if (socket.isConnected()) {
                    executorService.execute(serverResponseThread);
                }
            }

            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    isStart = false;
                    serverSocket.close();
                    serverResponseThread.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
