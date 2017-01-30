package com.test;

import com.cmpp.CmppMain;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by kangbo on 2016/12/9.
 */
public class SmsTest {
    public static void main (String[] args) {
        CmppMain cmppMain = new CmppMain();
        try {
            BasicConfigurator.configure();

            cmppMain.submitSms("123", "18858100583", "http://mms.llqianbao.com/llg/llg1458227685289903.mms", 1);

            Thread.sleep(10000000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
