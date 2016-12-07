package com.cmpp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by kangbo on 2016/11/29.
 */
public class GetProperties {
    public String host = "211.140.12.45";
    public String port = "7890";
    public int maxSocket = 2;
    public int timeOut = 1000;
    public int sendRetry = 3;
    public int recvRetry = 10;

    public String sourceAddr = "QQ1107";
    public String spId = "911337";
    public String pwd = "rgr61107";
    public byte version = (byte)0x30;
    public byte registeredDelivery = (byte)0x01;
    public byte msgLevel = (byte)0x01;
    public byte feeUserType = (byte)0x03;
    public String feeType = "01";
    public String feeCode = "000000";
    public byte[] validTime = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    public byte[] atTime = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    public byte[] srcId = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31,(byte)0x31, (byte)0x30, (byte)0x36, (byte)0x35, (byte)0x37, (byte)0x35, (byte)0x32, (byte)0x36, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x37};
    public byte[] reserve = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

    //0000005eh: 31 30 36 35 37 35 32 36 31 31 30 37             ; ............
    //self.Timestamp=0
    //self.Pk_total=1            #信息总条数
    //self.Pk_number=1           #信息序号
    //self.Service_Id='xsms'      #业务标示
    //self.Fee_terminal_Id='15906656142'  # 魅族X6 手机号 15906656142 13816025562 18858100583
    //self.TP_pId=1        #GSM协议类型,如果是wappush需要设置为1
    //self.TP_udhi=1       #GSM协议类型 ,如果是wappush需要设置为1
    //self.Msg_Fmt=4      #信息格式：0：ASCII串；3：短信写卡操作；4：二进制信息；8：UCS2编码；15：含GB汉字
    //self.Src_Id="106575261107" #源号码。SP的服务代码

    public GetProperties() {
        Properties prop = new Properties();
        try {
                InputStream in = new BufferedInputStream (new FileInputStream("a.properties"));
                prop.load(in);     //加载属性列表
                if(prop.containsKey("host"))
                    host = prop.getProperty("host");
                if(prop.containsKey("port"))
                    port = prop.getProperty("port");
                if(prop.containsKey("maxSocket"))
                    maxSocket = Integer.parseInt(prop.getProperty("maxSocket"));
                if(prop.containsKey("timeOut"))
                    maxSocket = Integer.parseInt(prop.getProperty("timeOut"));
                if(prop.containsKey("sendRetry"))
                    maxSocket = Integer.parseInt(prop.getProperty("sendRetry"));
                if(prop.containsKey("sourceAddr"))
                    sourceAddr = prop.getProperty("sourceAddr");
                if(prop.containsKey("spId"))
                    spId = prop.getProperty("spId");
                if(prop.containsKey("pwd"))
                    pwd = prop.getProperty("pwd");
                if(prop.containsKey("version"))
                    version = (byte)Integer.parseInt(prop.getProperty("version"));
                if(prop.containsKey("registeredDelivery"))
                    registeredDelivery = (byte)Integer.parseInt(prop.getProperty("registeredDelivery"));
                if(prop.containsKey("msgLevel"))
                    msgLevel = (byte)Integer.parseInt(prop.getProperty("msgLevel"));
                if(prop.containsKey("feeUserType"))
                    feeUserType = (byte)Integer.parseInt(prop.getProperty("feeUserType"));
                if(prop.containsKey("feeType"))
                    feeType = prop.getProperty("feeType");
                if(prop.containsKey("feeCode"))
                    feeCode = prop.getProperty("feeCode");
                //if(prop.containsKey("validTime"))
                //    validTime = prop.getProperty("validTime").getBytes();
                //if(prop.containsKey("AtTime"))
                //    atTime = prop.getProperty("AtTime").getBytes();
                if(prop.containsKey("srcId"))
                    srcId = CmppUtil.str2Byte(prop.getProperty("srcId"), 21);
                //if(prop.containsKey("reserve"))
                //    reserve = prop.getProperty("reserve").getBytes();
                in.close();

                /* //保存属性到b.properties文件
                FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
                prop.setProperty("phone", "10086");
                prop.store(oFile, "The New properties file");
                oFile.close();*/

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
