package com.topwo.utils;


import com.topwo.libs.xxtea.XXTEA;

import java.util.Arrays;

public class TopwoXXTEA {
    //xxtea
    private static String xxtea_key = "";
    private static byte[] xxtea_sign;

    private TopwoXXTEA() {
    }

    public static void init(String str_key, String str_sign) {
        xxtea_key = str_key;
        xxtea_sign = str_sign.getBytes();
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

    public static boolean isSign (byte[] file_buffer) {
        return Arrays.equals(xxtea_sign, Arrays.copyOfRange(file_buffer, 0, xxtea_sign.length));
    }
}
