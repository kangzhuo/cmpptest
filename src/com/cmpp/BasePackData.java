package com.cmpp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/29.
 */
class BasePackData {
    private static GetProperties config = new GetProperties();


    byte[] makeConnectReq() throws Exception {
        if (BasePackData.config.protocol.equals("YD")) {
            CmppPackData cmppPackData = new CmppPackData();
            return cmppPackData.makeCmppConnectReq();
        } else if (BasePackData.config.protocol.equals("LT")) {
            SGIPPackData sgipPackData = new SGIPPackData();
            return sgipPackData.makeSGIPConnectReq();
        }
        return null;
    }

    Map<String,Object> readConnectResp(byte[] p_respDatas) {
        if (BasePackData.config.protocol.equals("YD")) {
            CmppPackData cmppPackData = new CmppPackData();
            return cmppPackData.readCmppConnectResp(p_respDatas);
        } else if (BasePackData.config.protocol.equals("LT")) {
            SGIPPackData sgipPackData = new SGIPPackData();
            return sgipPackData.readSGIPConnectResp(p_respDatas);
        }
        return null;
    }

    byte[] makeTestReq() throws Exception {
        if (BasePackData.config.protocol.equals("YD")) {
            CmppPackData cmppPackData = new CmppPackData();
            return cmppPackData.makeCmppActiveTestResp(3);
        } else if (BasePackData.config.protocol.equals("LT")) {
            return null;
        }
        return null;
    }
}
