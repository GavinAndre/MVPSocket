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
                serverResponseThread = new ServerResponseThread(socket,
                        new ResponseCallback() {

                            @Override
                            public void targetIsOffline() {// 对方不在线
                                System.out.println("offline");
                            }

                            @Override
                            public void targetIsOnline(String clientIp) {
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
                    if (serverSocket != null)
                        serverResponseThread.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
