package com.test;

import com.cmpp.CmppPackData;
import com.cmpp.CmppSocketClient;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by kangbo on 2016/12/9.
 */
public class SmsTest {
    public static void main (String[] args) {
        CmppPackData cmppPackData = new CmppPackData();
        try {
            BasicConfigurator.configure();

            byte[] l_byteMsg = cmppPackData.makeCmppSubmitReq(123, "18858100583", 1, "短信测试");
            int l_iRet = CmppSocketClient.sendAndRetSocket(l_byteMsg);

            Thread.sleep(300000);

            l_iRet = CmppSocketClient.sendAndRetSocket(l_byteMsg);
            System.out.println(l_iRet);

            Thread.sleep(10000000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
