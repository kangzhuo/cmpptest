package com.test;

/**
 * Created by kangbo on 2016/12/9.
 */
public class OtherTest {
    public static void main (String[] args) {
        String l_str = ",9999";
        String[] l_ll = l_str.split(",");
        System.out.println("=====" + l_ll.length);
    }

    static void printHexStringForByte(byte[] p_bytes) {
        for (byte l_byte : p_bytes) {
            String hex = Integer.toHexString(l_byte & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
        System.out.println("");
    }
}
