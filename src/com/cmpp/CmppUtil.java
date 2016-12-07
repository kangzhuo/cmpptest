package com.cmpp;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppUtil {
    public static byte[] int2byte(int p_iRes) {
        byte[] l_iTarget = new byte[4];

        l_iTarget[0] = (byte) (p_iRes & 0xff);// 最低位
        l_iTarget[1] = (byte) ((p_iRes >> 8) & 0xff);// 次低位
        l_iTarget[2] = (byte) ((p_iRes >> 16) & 0xff);// 次高位
        l_iTarget[3] = (byte) (p_iRes >>> 24);// 最高位,无符号右移。
        return l_iTarget;
    }

    /**
     * 对字符串md5加密
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) throws Exception{
        // 生成一个MD5加密计算摘要
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 计算md5函数
        md.update(str.getBytes());
        // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
        // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
        return new BigInteger(1, md.digest()).toString(16);
    }

    public static byte[] stringToByte(String p_strSrc, int p_iLength) {
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
    }
}
