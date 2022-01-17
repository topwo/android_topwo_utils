package com.topwo.utils;

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
        void onHttpURLConnection(HttpURLConnection conn);
        /**
         * 请求成功，返回输出流接收outputStream.write(buffer, 0, len);
         * @param buffer
         * @param len
         */
        void onResponseSuccess(byte[] buffer, int len);

        /**
         * 请求失败，返回错误码;
         * @param code
         */
        void onResponseFail(int code);
    }

    /**
     * 在回调里使用ByteArrayOutputStream、FileOutputStream等OutputStream输出流接收（outputStream.write(buffer, 0, len);）
     * @param url_str
     * @param req_byte
     * @param listener
     * @throws Exception
     */
    public static void httpURLConnection(String url_str, byte[] req_byte, TopwoHttpURLConnectionListener listener) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection conn = null;
        Exception ex = null;
        int code = 0;
        try {
            URL url = new URL(url_str);
            conn = (HttpURLConnection)url.openConnection();
            if (listener != null) {
                listener.onHttpURLConnection(conn);
            }
            if (req_byte != null) {
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
            }
            code = conn.getResponseCode();
            if (code == 200) {
                inputStream = conn.getInputStream();

                int len = 0;
                byte[] buffer = new byte[TopwoHttpURLConnection.BUFFER];
                while ((len = inputStream.read(buffer)) != -1) {
                    if (listener != null) {
                        listener.onResponseSuccess(buffer, len);
                    }
                }
            }
            else {
                listener.onResponseFail(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ex = e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ex = e;
            }
        }
        if(ex != null){
            throw ex;
        }
    }
}
