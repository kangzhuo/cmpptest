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

            //cmppMain.submitSms("123", "13003226512", "http://c.cmfree.cn/07/01016.mms", 5);
            cmppMain.submitSms("123", "18858100583", "http://c.cmfree.cn/07/01016.mms", 1);

            Thread.sleep(10000000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
