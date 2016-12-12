package com.cmpp;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppUtil {
    private static Logger logger = Logger.getLogger(CmppUtil.class);

    public static byte[] int2byte(int p_iRes) {
        byte[] l_iTarget = new byte[4];

        l_iTarget[3] = (byte) (p_iRes & 0xff);// 最低位
        l_iTarget[2] = (byte) ((p_iRes >> 8) & 0xff);// 次低位
        l_iTarget[1] = (byte) ((p_iRes >> 16) & 0xff);// 次高位
        l_iTarget[0] = (byte) (p_iRes >>> 24);// 最高位,无符号右移。
        return l_iTarget;
    }

    public static int byte2int(byte p_b1, byte p_b2, byte p_b3, byte p_b4) {
        return (p_b4 & 0xFF) | ((p_b3 & 0xFF) << 8) | ((p_b2 & 0xFF) << 16) | ((p_b1 & 0xFF) << 24);
    }

    /**
     * 对字符串md5加密
     *
     */
    public static byte[] MD5(String str1) {
        try {
            byte[] buffer = str1.getBytes();
            String s;
            char hexDigist[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] datas = md.digest(); //16个字节的长整数
            /*char[] str = new char[2*16];
            int k = 0;
            for(int i=0;i<16;i++){
                byte b   = datas[i];
                str[k++] = hexDigist[b>>>4 & 0xf];//高4位
                str[k++] = hexDigist[b & 0xf];//低4位
            }
            s = new String(str);
            return s;*/
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] str2Byte(String p_strSrc, int p_iLength) {
        if (p_strSrc.length() > p_iLength) {
            return p_strSrc.substring(0, p_iLength).getBytes();
        } else if (p_strSrc.length() < p_iLength) {
            byte[] l_rets = new byte[p_iLength];
            int l_iAdd = p_iLength - p_strSrc.length();
            int i, j;
            for (i = 0; i < l_iAdd; i++)
                l_rets[i] = (byte) 0x00;
            for (j = 0; j < p_strSrc.length(); j++)
                l_rets[i + j] = (byte) p_strSrc.charAt(j);
            return l_rets;
        }
        return p_strSrc.getBytes();
    }

    public static void printHexString(Object[] p_bytes)
    {
        for (Object l_byte : p_bytes) {
            String hex = Integer.toHexString((byte)l_byte & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
        System.out.println();
    }

    public static void printHexStringForByte(byte[] p_bytes)
    {
        for (byte l_byte : p_bytes) {
            String hex = Integer.toHexString(l_byte & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
        System.out.println();
    }
}
