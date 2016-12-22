package com.test;

/**
 * Created by kangbo on 2016/12/9.
 */
public class OtherTest {
    public static void main (String[] args) {
        byte[] l_bytes = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        String l_strTmp = new String(l_bytes);
        System.out.println(l_strTmp);
        System.out.println(l_strTmp.length());
    }
}
