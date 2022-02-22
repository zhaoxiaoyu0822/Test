package com.hat.util;

public class ByteUtils {
    public static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
        return sb.toString();
    }

    public static byte[] fromShort(short n) {
        return new byte[] {
                (byte) n, (byte) (n >>> 8)
        };
    }

}

