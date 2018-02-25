package com.gavinandre.mvpsocketclient.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by gavinandre on 18-1-8.
 */
public class SocketUtil {

    private static final String TAG = SocketUtil.class.getSimpleName();
    public static String ADDRESS = "192.168.1.123";
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
     * @param pw
     */
    public static void closePrintWriter(PrintWriter pw) {
        if (pw != null) {
            Log.i(TAG, "closePrintWriter: " + Thread.currentThread().getName());
            pw.println("bye");
            pw.close();
        }
    }

}