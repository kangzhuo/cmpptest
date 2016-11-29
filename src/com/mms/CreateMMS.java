package com.mms;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/28.
 */
public class CreateMMS {

    private FileOutputStream g_fos = null;
    private Date g_now;

    public int create (String p_strFileName, String p_strXml, Map<String,String> p_mapParam, String p_strFrom, String p_strTime) throws Exception{
        g_fos = new FileOutputStream(p_strFileName);

        if (p_strFrom == null || p_strFrom.length() == 0) {
            p_strFrom = "106575261107";
        }

        if (p_strTime == null || p_strTime.length() == 0) {
            g_now = new Date();
            SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
            p_strTime = df.format(g_now);
        }

        writeHead(p_strFrom, p_strTime);

        g_fos.write((byte)(p_mapParam.size() + 1));

        writeXML(p_strXml);

        for (Map.Entry<String, String> entry : p_mapParam.entrySet()) {
            if (entry.getKey().contains("TEXT")) {
                writeText(entry.getKey().replace("TEXT|",""), entry.getValue());
            } else if (entry.getKey().contains("IMAGE")) {
                writeImage(entry.getKey().replace("IMAGE|",""), entry.getValue());
            } else if (entry.getKey().contains("VIDEO")) {
                writeVideo(entry.getKey().replace("VIDEO|",""), entry.getValue());
            }
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

        }

        g_fos.flush();
        g_fos.close();
        return 0;
    }

    private void writeHead(String p_strFrom, String p_strTime) throws IOException{
        byte[] message = {(byte)0x8C, (byte)0x84, //message-type: m-send-conf = 0x81 m-notification-ind = 0x82 m-notifyresp-ind = 0x83 m-retrieve-conf = 0x84 m-acknowledge-ind = 0x85 m-delivery-ind = 0x86 m-read-rec-ind = 0x87 m-read-orig-ind = 0x88 m-forward-req = 0x89 m-forward-conf = 0x90
                (byte)0x8D, (byte)0x90,  //mms-version
                (byte)0x8A, (byte)0x80, //message-class: Personal = 0x80 Advertisement = 0x81
                (byte)0x8F, (byte)0x81, (byte)0x86, (byte)0x80, (byte)0x90, (byte)0x80,  //unknow
                (byte)0x98, (byte)0x33, (byte)0x36, (byte)0x35, (byte)0x38, (byte)0x32, (byte)0x34, (byte)0x00,  //X-Mms-Transaction-ID byte[] mesg_tran = {(byte)0x98, (byte)0x65, (byte)0x30, (byte)0x62, (byte)0x66, (byte)0x64, (byte)0x30, (byte)65, (byte)0x37, (byte)0x2D, (byte)0x65, (byte)0x64, (byte)0x37, (byte)0x33, (byte)0x2D, (byte)0x34, (byte)0x63, (byte)0x65, (byte)0x61, (byte)0x2D, (byte)0x39, (byte)0x64, (byte)0x64, (byte)0x38, (byte)0x2D, (byte)0x32, (byte)0x30, (byte)0x33, (byte)0x65, (byte)0x63, (byte)0x63, (byte)0x61, (byte)0x62, (byte)0x32, (byte)0x35, (byte)0x31, (byte)0x32, (byte)0x2D, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x39, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x33, (byte)0x33, (byte)0x00};
                (byte)0x8B}; //message-id

        byte[] message_id_date = {(byte)0x46, (byte)0x34, (byte)0x6B, (byte)0x4A, (byte)0x76, (byte)0x66, (byte)0x48, (byte)0x45, (byte)0x62, (byte)0x67, (byte)0x79, (byte)0x7A, (byte)0x00, //message-id-other
                (byte)0x85, (byte)0x04}; //date

        byte[] message_from = {(byte)0x89, (byte)0x0C, (byte)0x80}; //(byte)0x31, (byte)0x30, (byte)0x36, (byte)0x35, (byte)0x37, (byte)0x35, (byte)0x32, (byte)0x36, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x37,

        byte[] message_to_subject_type = {(byte)0x00, //from-other
                (byte)0x97, (byte)0x2B, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x2F, (byte)0x54, (byte)0x59, (byte)0x50, (byte)0x45, (byte)0x3D, (byte)0x50, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x00, //to +0000000000/TYPE=PLMN
                (byte)0x96, (byte)0x0E, (byte)0xEA, (byte)0xE4, (byte)0xBF, (byte)0xA1, (byte)0xE6, (byte)0x81, (byte)0xAF, (byte)0xE6, (byte)0x8E, (byte)0xA8, (byte)0xE9, (byte)0x80, (byte)0x81, (byte)0x00,
                (byte)0x84, (byte)0x1D, (byte)0xB3, (byte)0x8A, (byte)0x6D, (byte)0x6D, (byte)0x73, (byte)0x2E, (byte)0x73, (byte)0x6D, (byte)0x69, (byte)0x6C, (byte)0x00,
                (byte)0x89, (byte)0x61, (byte)0x70, (byte)0x70, (byte)0x6C, (byte)0x69, (byte)0x63, (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x2F, (byte)0x73, (byte)0x6D, (byte)0x69, (byte)0x6C, (byte)0x00};

        g_fos.write(message);
        g_fos.write(p_strTime.getBytes());
        g_fos.write(message_id_date);
        g_fos.write(longToByteArray(System.currentTimeMillis()/1000 ));
        g_fos.write(message_from);
        g_fos.write(p_strFrom.getBytes());
        g_fos.write(message_to_subject_type);
    }

    private void writeXML(String p_strXml) throws IOException{
        byte[] xmlHead = {(byte)0x61, (byte)0x70, (byte)0x70, (byte)0x6C, (byte)0x69, (byte)0x63, (byte)0x61, (byte)0x74,
                (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x2F, (byte)0x73, (byte)0x6D, (byte)0x69, (byte)0x6C,
                (byte)0x00, (byte)0xC0, (byte)0x6D, (byte)0x6D, (byte)0x73, (byte)0x2E, (byte)0x73, (byte)0x6D,
                (byte)0x69, (byte)0x6C, (byte)0x00};
        g_fos.write((byte)0x1B); //27
        g_fos.write(encodeUintvar(p_strXml.getBytes().length + 1));
        g_fos.write(xmlHead);
        g_fos.write(p_strXml.getBytes());
        g_fos.write((byte)0x0A);
    }

    private void writeText(String p_strName, String p_strText) throws IOException{
        byte[] xmlHead_1 = {(byte)0x03, (byte)0x83, (byte)0x81, (byte)0xEA, (byte)0xC0};
        byte[] xmlHead_2 = {(byte)0x00, (byte)0x8E};
        byte[] xmlHead_3 = {(byte)0x00};

        int l_iHeadSize = 5 + 2 + 1 + p_strName.getBytes().length + p_strName.getBytes().length;

        g_fos.write((byte)l_iHeadSize);
        g_fos.write(encodeUintvar(p_strText.getBytes().length));
        g_fos.write(xmlHead_1);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_2);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_3);
        g_fos.write(p_strText.getBytes());
    }

    private void writeImage(String p_strName, String p_strImage) throws IOException{
        byte[] xmlHead_1 = {(byte)0x69, (byte)0x6D, (byte)0x61, (byte)0x67, (byte)0x65, (byte)0x2F, (byte)0x6A, (byte)0x70, (byte)0x65, (byte)0x67, (byte)0x00, (byte)0xC0};
        byte[] xmlHead_2 = {(byte)0x00, (byte)0x8E};
        byte[] xmlHead_3 = {(byte)0x00};

        int l_iHeadSize = 12 + 2 + 1 + p_strName.getBytes().length + p_strName.getBytes().length;

        FileInputStream l_fis= new FileInputStream(p_strImage);
        g_fos.write((byte)l_iHeadSize);
        g_fos.write(encodeUintvar(l_fis.available()));
        g_fos.write(xmlHead_1);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_2);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_3);
        int l_iData;
        while((l_iData = l_fis.read()) != -1){
            g_fos.write(l_iData);
        }
    }

    private void writeVideo(String p_strName, String p_strVideo) throws IOException{
        byte[] xmlHead_1 = {(byte)0x76, (byte)0x69, (byte)0x64, (byte)0x65, (byte)0x6F, (byte)0x2F, (byte)0x6D, (byte)0x70, (byte)0x34, (byte)0x00, (byte)0xC0};
        byte[] xmlHead_2 = {(byte)0x00, (byte)0x8E};
        byte[] xmlHead_3 = {(byte)0x00};

        int l_iHeadSize = 11 + 2 + 1 + p_strName.getBytes().length + p_strName.getBytes().length;

        FileInputStream l_fis= new FileInputStream(p_strVideo);
        g_fos.write((byte)l_iHeadSize);
        g_fos.write(encodeUintvar(l_fis.available()));
        g_fos.write(xmlHead_1);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_2);
        g_fos.write(p_strName.getBytes());
        g_fos.write(xmlHead_3);
        int l_iData;
        while((l_iData = l_fis.read()) != -1){
            g_fos.write(l_iData);
        }
    }

    /*uintvar编码*/
    private byte[] encodeUintvar (long p_lData){
        char l_chars[] = new char[8];
        int i = 0;
        l_chars[i]=(char)(p_lData & 0x7f);   // The lowest
        p_lData = p_lData >> 7;
        i++;
        while ( p_lData > 0 )
        {
            l_chars[i] = (char)(0x80 | (p_lData & 0x7f));
            i++;
            p_lData = p_lData >> 7;
        }
        int j;
        byte l_retBytes[] = new byte[i];
        // Reverse it because it is in reverse order
        for ( j = 0; j < i; j++ )
        {
            l_retBytes[j] = (byte) l_chars[i - j - 1];
        }
        return l_retBytes;
    }

    private byte[] longToByteArray(long s) {
        byte[] targets = new byte[4];
        for (int i = 0; i < targets.length; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public static void main (String[] args) {
        try {
            CreateMMS createMMS = new CreateMMS();
            String l_strXml = "" +
                "<smil xmlns=\"http://www.w3.org/2000/SMIL20/CR/Language\"> " +
                    "<head>" +
                        "<layout>" +
                            "<root-layout height=\"640\" width=\"480\" />" +
                            "<region id=\"Image\" top=\"0\" left=\"0\" height=\"334\" width=\"400\" fit=\"meet\"/>" +
                            "<region id=\"Text\" top=\"334\" left=\"0\" height=\"306\" width=\"400\" fit=\"meet\"/>" +
                        "</layout>" +
                    "</head>" +
                    "<body>" +
                        "<par dur=\"120000ms\">" +
                            "<text region=\"Text\" src=\"t04.txt\"/>" +
                        "</par>" +
                        "<par dur=\"120000ms\">" +
                            "<video src=\"12.mp4\"/>" +
                        "</par>" +
                    "</body>" +
                "</smil>";
            Map<String,String> l_mapParam = new HashMap<>();
            l_mapParam.put("TEXT|t04.txt", "测试123");
            l_mapParam.put("VIDEO|12.mp4", "/Users/kangbo/Downloads/xinwen.mp4");

            createMMS.create ("/Users/kangbo/work/cmpptest/kbtest.mms", l_strXml, l_mapParam, null, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
