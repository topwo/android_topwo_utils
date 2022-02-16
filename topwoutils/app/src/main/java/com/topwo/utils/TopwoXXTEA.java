package com.topwo.utils;


import com.topwo.libs.xxtea.XXTEA;

import java.util.Arrays;

public class TopwoXXTEA {
    //xxtea
    private static byte[] xxtea_sign;
    private static String xxtea_key = "";

    private TopwoXXTEA() {
    }

    public static void setSignAndKey(String str_sign, String str_key) {
        xxtea_sign = str_sign.getBytes();
        xxtea_key = str_key;
    }

    /**
     * 是否使用xxtea加密
     * @param file_buffer 字节组
     */
    public static boolean isSign (byte[] file_buffer) {
        return Arrays.equals(xxtea_sign, Arrays.copyOfRange(file_buffer, 0, xxtea_sign.length));
    }

    /**
     * xxtea解析字节组
     * @param file_buffer 字节组
     */
    public static byte[] decrypt (byte[] file_buffer) {
        if(isSign(file_buffer)) {
            file_buffer = Arrays.copyOfRange(file_buffer, xxtea_sign.length, file_buffer.length);
            file_buffer = XXTEA.decrypt(file_buffer, xxtea_key);
        }
        return file_buffer;
    }
}
