package com.topwo.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TopwoHttpURLConnection {
    private static final String TAG = TopwoHttpURLConnection.class.getSimpleName();

    public static final int BUFFER = 4096;

    public interface ResponseListener {
        /**
         * 输出流接收outputStream.write(buffer, 0, len);
         * @param buffer
         * @param len
         */
        void onResponseRead(byte[] buffer, int len);
    }

    /**
     * 在回调里使用ByteArrayOutputStream、FileOutputStream等OutputStream输出流接收（outputStream.write(buffer, 0, len);）
     * @param url_str
     * @param req_byte
     * @param responseListener
     * @throws Exception
     */
    public static void httpURLConnection(String url_str, byte[] req_byte, ResponseListener responseListener) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection conn = null;
        Exception ex = null;
        try {
            URL url = new URL(url_str);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(false);
            conn.setRequestProperty("Accept-Encoding", "identity");
            if(req_byte != null){
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
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
            int code = conn.getResponseCode();
            if (code == 200) {
                inputStream = conn.getInputStream();

                int len = 0;
                byte[] buffer = new byte[TopwoHttpURLConnection.BUFFER];
                while ((len = inputStream.read(buffer)) != -1) {
                    if (responseListener != null) {
                        responseListener.onResponseRead(buffer, len);
                    }
                }
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
