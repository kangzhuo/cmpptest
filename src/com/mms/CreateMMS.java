package com.mms;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

/**
 * Created by kangbo on 2016/11/28.
 */
public class CreateMMS {

    private FileOutputStream g_fos = null;

    public int create (String p_strFileName, String p_strSign, String p_strXml, Map<String,String> p_mapParam, String p_strFrom) throws Exception{
        g_fos = new FileOutputStream(p_strFileName);

        if (p_strFrom == null || p_strFrom.length() == 0) {
            p_strFrom = "106575261107666";
        }

        writeHead(p_strSign, p_strFrom);

        g_fos.write((byte)(p_mapParam.size() + 1));

        writeXML(p_strXml);

        for (Map.Entry<String, String> entry : p_mapParam.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            if (entry.getKey().contains(".txt")) {
                writeText(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains(".jpg") || entry.getKey().contains(".jpeg") || entry.getKey().contains(".gif")) {
                writeImage(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains(".mp4")) {
                writeVideo(entry.getKey(), entry.getValue());
            }
        }

        g_fos.flush();
        g_fos.close();

        scpFile("121.40.149.142", 22, "root", p_strFileName, "/data/mms");

        return 0;
    }

    private void writeHead(String p_strSign, String p_strFrom) throws IOException{
        byte[] message = {(byte)0x8C, (byte)0x84, //message-type: m-send-conf = 0x81 m-notification-ind = 0x82 m-notifyresp-ind = 0x83 m-retrieve-conf = 0x84 m-acknowledge-ind = 0x85 m-delivery-ind = 0x86 m-read-rec-ind = 0x87 m-read-orig-ind = 0x88 m-forward-req = 0x89 m-forward-conf = 0x90
                (byte)0x98, (byte)0x35, (byte)0x38, (byte)0x36, (byte)0x36, (byte)0x00,  //X-Mms-Transaction-ID byte[] mesg_tran = {(byte)0x98, (byte)0x65, (byte)0x30, (byte)0x62, (byte)0x66, (byte)0x64, (byte)0x30, (byte)65, (byte)0x37, (byte)0x2D, (byte)0x65, (byte)0x64, (byte)0x37, (byte)0x33, (byte)0x2D, (byte)0x34, (byte)0x63, (byte)0x65, (byte)0x61, (byte)0x2D, (byte)0x39, (byte)0x64, (byte)0x64, (byte)0x38, (byte)0x2D, (byte)0x32, (byte)0x30, (byte)0x33, (byte)0x65, (byte)0x63, (byte)0x63, (byte)0x61, (byte)0x62, (byte)0x32, (byte)0x35, (byte)0x31, (byte)0x32, (byte)0x2D, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x39, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x33, (byte)0x33, (byte)0x00};
                (byte)0x8D, (byte)0x90  //mms-version
        };

        byte[] message_id = {(byte)0x8B, (byte)0x46, (byte)0x34, (byte)0x6B, (byte)0x4A, (byte)0x76, (byte)0x66, (byte)0x48, (byte)0x45, (byte)0x62, (byte)0x67, (byte)0x79, (byte)0x7A, (byte)0x00};
        byte[] message_date = {(byte)0x85, (byte)0x00}; //date 0x85 0x04 - - - -
        byte[] message_from = {(byte)0x89, (byte)0x0C, (byte)0x80}; //(byte)0x31, (byte)0x30, (byte)0x36, (byte)0x35, (byte)0x37, (byte)0x35, (byte)0x32, (byte)0x36, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x37,
        byte[] message_from_end = {(byte)0x00};
        byte[] messaget_to = {(byte)0x97, (byte)0x2B, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x2F, (byte)0x54, (byte)0x59, (byte)0x50, (byte)0x45, (byte)0x3D, (byte)0x50, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x00}; //to +0000000000/TYPE=PLMN
        byte[] message_subject = {(byte)0x96}; //(byte)0x96, (byte)0x0E, (byte)0xEA, (byte)0xE4, (byte)0xBF, (byte)0xA1, (byte)0xE6, (byte)0x81, (byte)0xAF, (byte)0xE6, (byte)0x8E, (byte)0xA8, (byte)0xE9, (byte)0x80, (byte)0x81, (byte)0x00
        byte[] message_subject_ea = {(byte)0xEA};
        byte[] message_subject_end = {(byte)0x00};
        byte[] message_type = {(byte)0x84, (byte)0xB3};

        g_fos.write(message);
        g_fos.write(message_id);
        g_fos.write(message_date);
        //g_fos.write(longToByteArray(System.currentTimeMillis()/1000 ));
        g_fos.write(message_from);
        g_fos.write(p_strFrom.getBytes());
        g_fos.write(message_from_end);
        g_fos.write(message_subject);
        g_fos.write((byte) (p_strSign.getBytes().length + 2));
        g_fos.write(message_subject_ea);
        g_fos.write(p_strSign.getBytes());
        g_fos.write(message_subject_end);
        g_fos.write(message_type);
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
        String l_strLocalPath = "/tmp/" + md5(p_strImage);
        downloadFile(p_strImage, l_strLocalPath);
        byte[] xmlHead_1 = {(byte)0x69, (byte)0x6D, (byte)0x61, (byte)0x67, (byte)0x65, (byte)0x2F, (byte)0x6A, (byte)0x70, (byte)0x65, (byte)0x67, (byte)0x00, (byte)0xC0};
        byte[] xmlHead_2 = {(byte)0x00, (byte)0x8E};
        byte[] xmlHead_3 = {(byte)0x00};

        int l_iHeadSize = 12 + 2 + 1 + p_strName.getBytes().length + p_strName.getBytes().length;

        FileInputStream l_fis= new FileInputStream(l_strLocalPath);
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
        String l_strLocalPath = "/tmp/" + md5(p_strVideo);
        downloadFile(p_strVideo, l_strLocalPath);
        byte[] xmlHead_1 = {(byte)0x76, (byte)0x69, (byte)0x64, (byte)0x65, (byte)0x6F, (byte)0x2F, (byte)0x6D, (byte)0x70, (byte)0x34, (byte)0x00, (byte)0xC0};
        byte[] xmlHead_2 = {(byte)0x00, (byte)0x8E};
        byte[] xmlHead_3 = {(byte)0x00};

        int l_iHeadSize = 11 + 2 + 1 + p_strName.getBytes().length + p_strName.getBytes().length;

        FileInputStream l_fis= new FileInputStream(l_strLocalPath);
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

    private String md5(String str1) {
        try {
            byte[] buffer = str1.getBytes();
            String s;
            char hexDigist[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] datas;
            datas = md.digest(); //16个字节的长整数
            char[] str = new char[2*16];
            int k = 0;
            for(int i=0;i<16;i++){
                byte b   = datas[i];
                str[k++] = hexDigist[b>>>4 & 0xf];//高4位
                str[k++] = hexDigist[b & 0xf];//低4位
            }
            s = new String(str);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void downloadFile(String remoteFilePath, String localFilePath) throws IOException
    {
        URL urlfile;
        HttpURLConnection httpUrl;
        BufferedInputStream bis;
        BufferedOutputStream bos;
        File f = new File(localFilePath);

        urlfile = new URL(remoteFilePath);
        httpUrl = (HttpURLConnection)urlfile.openConnection();
        httpUrl.connect();
        bis = new BufferedInputStream(httpUrl.getInputStream());
        bos = new BufferedOutputStream(new FileOutputStream(f));
        int len = 2048;
        byte[] b = new byte[len];
        while ((len = bis.read(b)) != -1)
        {
            bos.write(b, 0, len);
        }
        bos.flush();
        bis.close();
        httpUrl.disconnect();
    }

    private void scpFile(String p_strIp, int p_iPort, String p_strUser, String p_strLocalFile, String p_strRempteFile) throws IOException
    {
        Connection con = new Connection(p_strIp, p_iPort);
        con.connect();
        String userHome = System.getProperty("user.home");
        if (con.authenticateWithPublicKey(p_strUser, new File(userHome + "/.ssh/id_rsa"), "")) {
            SCPClient scpClient = con.createSCPClient();
            scpClient.put(p_strLocalFile, p_strRempteFile, "0644");
        } else {
            throw new IOException("不能打开链接");
        }
    }

    public static void main (String[] args) {
        try {
            CreateMMS createMMS = new CreateMMS();
            String l_strXml = "" +
                "<smil xmlns=\"http://www.w3.org/2000/SMIL20/CR/Language\"> " +
                    "<head>" +
                        "<layout>" +
                            "<root-layout height=\"640\" width=\"480\" />" +
                            "<region id=\"Image\" top=\"0\" left=\"0\" height=\"1\" width=\"1\" fit=\"hidden\"/>" +
                            "<region id=\"Text\" top=\"334\" left=\"0\" height=\"306\" width=\"400\" fit=\"hidden\"/>" +
                        "</layout>" +
                    "</head>" +
                    "<body>" +
                        "<par dur=\"120000ms\">" +
                            "<text region=\"Text\" src=\"t04.txt\"/>" +
                        "</par>" +
                        "<par dur=\"120000ms\">" +
                            //"<text region=\"Text\" src=\"t05.txt\"/>" +
                            //"<text region=\"Text\" src=\"t06.txt\"/>" +
                            //"<img region=\"Image\" src=\"11.jpg\"/>" +
                            "<video src=\"12.mp4\"/>" +
                        "</par>" +
                    "</body>" +
                "</smil>";
            Map<String,String> l_mapParam = new HashMap<>();
            l_mapParam.put("t04.txt", "信息推送");
            //l_mapParam.put("t05.txt", "复制上述卡密，直接粘贴回复，即可进行充值。流量充值结果预计在1小时内以短信方式告知，敬请留意。若遇充值不成功或其他问题，请致电4007000075查询处理。");
            //l_mapParam.put("t06.txt", "更多充值优惠，可关注公众号微信钱包君（搜索微信号liuliangqbj），或保存下面二维码，微信扫一扫即可成功关注。");
            //l_mapParam.put("13.jpg", "http://sms-agent.b0.upaiyun.com/sms_agent_temp/1/58635adb79e31.jpg");
            //l_mapParam.put("12.mp4", "http://sms-agent.b0.upaiyun.com/sms_agent_temp/1/5858d9f0bdfad.mp4");

            l_mapParam.put("12.mp4", "http://file.cdn.xiaohongquan.cn/20170113/b74e6572d72fb4b78ce69e2a43611e76.mp4");
            //l_mapParam.put("11.jpg", "http://sms-agent.b0.upaiyun.com/sms_agent_temp/1/585b436f90add.jpg");
            //l_mapParam.put("12.jpg", "http://sms-agent.b0.upaiyun.com/sms_agent_temp/1/585b436f90add.jpg");

            //l_mapParam.put("12.mp4", "http://sms-agent.b0.upaiyun.com/sms_agent_temp/1/5858d9f0bdfad.mp4");

            createMMS.create ("xinwen.mms", "信息推送", l_strXml, l_mapParam, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
