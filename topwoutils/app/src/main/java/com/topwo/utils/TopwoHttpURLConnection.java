package com.topwo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TopwoHttpURLConnection {
    private static final String TAG = TopwoHttpURLConnection.class.getSimpleName();

    public static final int BUFFER = 4096;

    public interface TopwoHttpURLConnectionListener {
        /**
         * 成功连接;
         * conn.setRequestProperty("Accept-Encoding", "identity");
         * conn.setRequestProperty("Content-Type", "application/json");
         * conn.setRequestMethod("POST");
         * @param conn
         */
        void onHttpURLConnection(HttpURLConnection conn) throws Exception;
        /**
         * 请求成功，返回输出流接收outputStream.write(buffer, 0, len);
         * @param buffer
         * @param len
         */
        void onResponseRead(byte[] buffer, int len) throws Exception;
        /**
         * 请求成功，并且输出流接收完毕
         */
        void onResponseSuccess() throws Exception;

        /**
         * 请求失败，返回错误码;
         * @param code
         */
        void onResponseFail(int code) throws Exception;
    }

    /**
     * 在回调里使用ByteArrayOutputStream、FileOutputStream等OutputStream输出流接收（outputStream.write(buffer, 0, len);）
     * @param url_str
     * @param listener
     * @throws Exception
     */
    public static void httpURLConnection(String url_str, TopwoHttpURLConnectionListener listener) throws Exception {
        Exception ex = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(url_str);
            conn = (HttpURLConnection)url.openConnection();
            if (listener != null) {
                listener.onHttpURLConnection(conn);
            }
            int code = conn.getResponseCode();
            if (code == 200) {
                if (listener != null) {
                    inputStream = conn.getInputStream();

                    int len = 0;
                    byte[] buffer = new byte[TopwoHttpURLConnection.BUFFER];
                    while ((len = inputStream.read(buffer)) != -1) {
                        listener.onResponseRead(buffer, len);
                    }
                    listener.onResponseSuccess();
                }
            }
            else {
                if (listener != null) {
                    listener.onResponseFail(code);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ex = e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        if(ex != null){
            throw ex;
        }
    }

    /**
     * 发送请求体
     * @param conn
     * @param req_byte
     * @throws IOException
     */
    public static void sendRequestBody(HttpURLConnection conn, byte[] req_byte) throws IOException {
        IOException ex = null;
        OutputStream outputStream = null;
        try {
            conn.setDoOutput(true);
            //获取conn的输出流
            outputStream = conn.getOutputStream();
            //将请求体写入到conn的输出流中
            outputStream.write(req_byte);
            //记得调用输出流的flush方法
            outputStream.flush();
            //关闭输出流
            outputStream.close();
            //置空
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
            ex = e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        if(ex != null){
            throw ex;
        }
    }
}
