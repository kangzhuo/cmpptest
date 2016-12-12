package com.test;

import com.cmpp.CmppPackData;
import com.cmpp.CmppSocketClient;
import com.cmpp.CmppUtil;
import com.cmpp.GetProperties;
import org.apache.log4j.BasicConfigurator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kangbo on 2016/12/9.
 */
public class SmsTest {
    public static void main (String[] args) {
        CmppPackData cmppPackData = new CmppPackData();
        try {
            BasicConfigurator.configure();

            byte[] l_byteMsg = cmppPackData.makeCmppSubmitReq(123, "18858100583", 1, "短信测试");
            CmppUtil.printHexStringForByte(l_byteMsg);
            System.out.println("===================================================================");
            int l_iRet = CmppSocketClient.sendAndRetSocket(l_byteMsg);
            System.out.println(l_iRet);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
