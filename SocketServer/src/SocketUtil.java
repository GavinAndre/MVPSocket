import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by gavinandre on 18-2-24.
 */
public class SocketUtil {

    public static int PORT = 10086;

    /**
     * 读数据
     *
     * @param bufferedReader
     */
    public static String readFromStream(BufferedReader bufferedReader) {
        try {
            String s;
            if ((s = bufferedReader.readLine()) != null) {
                return s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写数据
     *
     * @param data
     * @param printWriter
     */
    public static void write2Stream(String data, PrintWriter printWriter) {
        if (data == null) {
            return;
        }
        if (printWriter != null) {
            printWriter.println(data);
        }
    }


    /**
     * 关闭输入流
     *
     * @param socket
     */
    public static void inputStreamShutdown(Socket socket) {
        try {
            if (!socket.isClosed() && !socket.isInputShutdown()) {
                socket.shutdownInput();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭BufferedReader
     *
     * @param br
     */
    public static void closeBufferedReader(BufferedReader br) {
        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     *
     * @param socket
     */
    public static void outputStreamShutdown(Socket socket) {
        try {
            if (!socket.isClosed() && !socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭PrintWriter
     *
     * @param pw
     */
    public static void closePrintWriter(PrintWriter pw) {
        if (pw != null) {
            pw.close();
        }
    }

    /**
     * 获取本机IP地址
     */
    public static String getIP() {
        String hostIP = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIP = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIP;

    }

}