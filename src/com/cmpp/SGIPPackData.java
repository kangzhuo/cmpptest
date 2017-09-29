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
class SGIPPackData {
    private static GetProperties config = new GetProperties();
    private static SimpleDateFormat sequenceDate = new SimpleDateFormat("mmddhhmmss");
    static final int SGIP_CONNECT = 1;
    static final int SGIP_SUBMIT = 3;
    static final int SGIP_DELIVER = 4;
    static final int SGIP_REPORT = 5;

    private byte[] makeHead(int p_iType, int p_iBodyLength, byte[] p_strSeqId) throws IOException {
        byte[] totalLength = CmppUtil.int2byte(20 + p_iBodyLength);
        byte[] commandId = CmppUtil.int2byte(p_iType);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(totalLength);
        bos.write(commandId);
        long nodeid = (3000000000L + Long.parseLong("0240") * 100000L + Long.parseLong(SGIPPackData.config.spId));
        long time = Long.valueOf(SGIPPackData.sequenceDate.format(new Date()));
        bos.write(CmppUtil.long2byte(nodeid)[7]);
        bos.write(CmppUtil.long2byte(nodeid)[6]);
        bos.write(CmppUtil.long2byte(nodeid)[5]);
        bos.write(CmppUtil.long2byte(nodeid)[4]);
        bos.write(CmppUtil.long2byte(time)[7]);
        bos.write(CmppUtil.long2byte(time)[6]);
        bos.write(CmppUtil.long2byte(time)[5]);
        bos.write(CmppUtil.long2byte(time)[4]);
        bos.write(p_strSeqId);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeSGIPConnectReq() throws Exception {
        byte[] l_loginType = {(byte)0x01};
        byte[] l_loginName = CmppUtil.str2ByteHead(SGIPPackData.config.sourceAddr, 16);
        byte[] l_loginPassword = CmppUtil.str2ByteHead(SGIPPackData.config.pwd, 16);
        byte[] l_reserve = SGIPPackData.config.reserve;

        byte[] head = makeHead(SGIPPackData.SGIP_CONNECT, 41, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(l_loginType);
        bos.write(l_loginName);
        bos.write(l_loginPassword);
        bos.write(l_reserve);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeSGIPSubmitReq(int p_iSeq, String p_strTel, int p_iMsgType, String p_strMsg) throws IOException {
        byte[] spNumber, chargeNumber, userCount, userNumber, corpId, serviceType,  feeType, feeValue,
                givenValue, agentFlag, morelatetoMTFlag, priority, expireTime, scheduleTime, reportFlag,
                tpPid, tpUdhi, messageCoding, messageType, messageLength, messageContent, reserve, head;

        spNumber = CmppUtil.str2ByteHead(SGIPPackData.config.srcId, 21);
        chargeNumber = CmppUtil.str2ByteHead(SGIPPackData.config.srcId, 21);
        userCount = new byte[] {(byte)0x01};
        userNumber = CmppUtil.str2ByteHead(p_strTel, 21);
        corpId = SGIPPackData.config.spId.getBytes();
        serviceType = CmppUtil.str2ByteHead(SGIPPackData.config.serviceType, 10);
        feeType = new byte[] {(byte)0x01};
        feeValue = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        givenValue = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        agentFlag = new byte[] {(byte)0x01};
        morelatetoMTFlag = new byte[] {(byte)0x02};
        priority = new byte[] {(byte)0x00};
        expireTime = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        scheduleTime = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        reportFlag = new byte[] {(byte)0x01};
        tpPid = new byte[] {(byte)0x00};
        tpUdhi = 4 == p_iMsgType ? new byte[] {(byte)0x00} : new byte[] {(byte)0x01};
        messageCoding = 4 == p_iMsgType ? new byte[] {(byte)0x0f} : (5 == p_iMsgType? new byte[] {(byte)0x04} : new byte[] {(byte)0x18});
        messageType = new byte[] {(byte)0x00};

        if (4 == p_iMsgType) { //普通短信
            messageContent = p_strMsg.getBytes("GBK");
        } else if (5 == p_iMsgType) { //超级短信
            messageContent = makeMMSBody(p_strTel, p_strMsg);
            messageContent = new byte[] {0x06,0x05,0x04,0x0b,(byte)0x84,0x23,(byte)0xf0,0x00,0x06,0x22,0x61,0x70,0x70,0x6c,0x69,
                    0x63,0x61,0x74,0x69,0x6f,0x6e,0x2f,0x76,0x6e,0x64,0x2e,0x77,0x61,0x70,0x2e,0x6d,0x6d,0x73,0x2d,0x6d,
                    0x65,0x73,0x73,0x61,0x67,0x65,0x00,(byte)0xaf,(byte)0x84,(byte)0x8c,(byte)0x82,(byte)0x98,0x4a,0x74,
                    0x62,0x00,(byte)0x8d,(byte)0x90,(byte)0x89,0x1b,
                    (byte)0x80,0x31,0x30,0x36,0x35,0x35,0x30,0x32,0x34,0x30,0x30,0x31,0x36,0x00,
                    (byte)0x8a,(byte)0x80,(byte)0x8e,0x04,0x00,0x15,0x24,(byte)0x8a,(byte)0x88,0x05,(byte)0x81,0x03,0x01,
                    0x51,(byte)0x80,(byte)0x86,(byte)0x80,(byte)0x83,
                    0x68,0x74,0x74,0x70,0x3a,0x2f,0x2f,0x63,0x2e,0x63,0x6d,0x66,0x72,0x65,0x65,0x2e,0x63,0x6e,0x2f,0x30,
                    0x38,0x2f,0x30,0x30,0x36,0x37,0x34,0x2e,0x6d,0x6d,0x73,0x3f,0x62,0x3d,0x31,0x26,0x61,0x3d,
                    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
            System.arraycopy(p_strTel.getBytes(), 0, messageContent, 125, 11);
            //http://c.cmfree.cn/08/00674.mms?a=1&b=1
            //System.out.println("================" + messageContent.length);
        } else if (6 == p_iMsgType) { //闪信
            messageContent = p_strMsg.getBytes("Unicode");
        } else {
            return null;
        }

        messageLength = CmppUtil.int2byte(messageContent.length);
        reserve = SGIPPackData.config.reserve;

        head = makeHead(SGIPPackData.SGIP_SUBMIT, 144 + messageContent.length, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(spNumber);
        bos.write(chargeNumber);
        bos.write(userCount);
        bos.write(userNumber);
        bos.write(corpId);
        bos.write(serviceType);
        bos.write(feeType);
        bos.write(feeValue);
        bos.write(givenValue);
        bos.write(agentFlag);
        bos.write(morelatetoMTFlag);
        bos.write(priority);
        bos.write(expireTime);
        bos.write(scheduleTime);
        bos.write(reportFlag);
        bos.write(tpPid);
        bos.write(tpUdhi);
        bos.write(messageCoding);
        bos.write(messageType);
        bos.write(messageLength);
        bos.write(messageContent);
        bos.write(reserve);
        bos.flush();

        return bos.toByteArray();
    }

    byte[] makeSGIPDeliverResp(int p_iSeq) throws IOException {

        byte[] head = makeHead(SGIPPackData.SGIP_DELIVER | 0x80000000, 9, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(new byte[] {(byte)0x00});
        bos.write(SGIPPackData.config.reserve);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeSGIPReportResp(int p_iSeq) throws IOException {

        byte[] head = makeHead(SGIPPackData.SGIP_REPORT | 0x80000000, 9, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(new byte[] {(byte)0x00});
        bos.write(SGIPPackData.config.reserve);
        bos.flush();
        return bos.toByteArray();
    }

    Map<String,Object> readSGIPConnectResp(byte[] p_respDatas) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_respDatas.length < 9)
            return null;

        int l_iResult = p_respDatas[0];

        l_mapRet.put("status", l_iResult);
        return l_mapRet;
    }

    Map<String,Object> readSGIPSubmitResp(int p_iSeq, byte[] p_reqBodys) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_reqBodys.length < 9)
            return null;

        int l_iResult = p_reqBodys[0];

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("result", l_iResult);
        return l_mapRet;
    }

    Map<String,Object> readSGIPDeliverReq(int p_iSeq, byte[] p_reqBodys) {
        Map<String,Object> l_mapRet = new HashMap<>();

        byte[] userNumber = {p_reqBodys[0], p_reqBodys[1], p_reqBodys[2], p_reqBodys[3], p_reqBodys[4],
                p_reqBodys[5], p_reqBodys[6], p_reqBodys[7], p_reqBodys[8], p_reqBodys[9],
                p_reqBodys[10], p_reqBodys[11], p_reqBodys[12], p_reqBodys[13], p_reqBodys[14],
                p_reqBodys[15], p_reqBodys[16], p_reqBodys[17], p_reqBodys[18], p_reqBodys[19],
                p_reqBodys[20]};
        byte[] spNumber = {p_reqBodys[21], p_reqBodys[22], p_reqBodys[23], p_reqBodys[24], p_reqBodys[25],
                p_reqBodys[26], p_reqBodys[27], p_reqBodys[28], p_reqBodys[29], p_reqBodys[30],
                p_reqBodys[31], p_reqBodys[32], p_reqBodys[33], p_reqBodys[34], p_reqBodys[35],
                p_reqBodys[36], p_reqBodys[37], p_reqBodys[38], p_reqBodys[39], p_reqBodys[40],
                p_reqBodys[41]};
        int tpPid = p_reqBodys[42];
        int tpUdhi = p_reqBodys[43];
        int messageCoding = p_reqBodys[44];
        int messageLength = CmppUtil.byte2int(p_reqBodys[45], p_reqBodys[46], p_reqBodys[47], p_reqBodys[48]);
        byte[] msgContent = new byte[messageLength];
        System.arraycopy(p_reqBodys, 49, msgContent, 0, messageLength);
        byte[] reserved = new byte[8];
        System.arraycopy(p_reqBodys, 49 + messageLength, reserved, 0, 8);

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("userNumber", userNumber);
        l_mapRet.put("spNumber", spNumber);
        l_mapRet.put("tpPid", tpPid);
        l_mapRet.put("tpUdhi", tpUdhi);
        l_mapRet.put("messageCoding", messageCoding);
        l_mapRet.put("messageLength", messageLength);
        l_mapRet.put("msgContent", msgContent);
        l_mapRet.put("reserved", reserved);

        return l_mapRet;
    }

    Map<String,Object> readSGIPReportReq(int p_iSeq, byte[] p_reqBodys) {
        Map<String,Object> l_mapRet = new HashMap<>();

        byte[] submitSequenceNumber = {p_reqBodys[0], p_reqBodys[1], p_reqBodys[2], p_reqBodys[3], p_reqBodys[4],
                p_reqBodys[5], p_reqBodys[6], p_reqBodys[7], p_reqBodys[8], p_reqBodys[9],
                p_reqBodys[10], p_reqBodys[11]};
        int reportType = p_reqBodys[12];
        byte[] userNumber = {p_reqBodys[13], p_reqBodys[14], p_reqBodys[15], p_reqBodys[16], p_reqBodys[17],
                p_reqBodys[18], p_reqBodys[19], p_reqBodys[20], p_reqBodys[21], p_reqBodys[22],
                p_reqBodys[23], p_reqBodys[24], p_reqBodys[25], p_reqBodys[26], p_reqBodys[27],
                p_reqBodys[28], p_reqBodys[29], p_reqBodys[30], p_reqBodys[31], p_reqBodys[32],
                p_reqBodys[33]};
        int state = p_reqBodys[34];
        int errorCode = p_reqBodys[35];
        byte[] messageCoding = {p_reqBodys[36], p_reqBodys[37], p_reqBodys[38], p_reqBodys[39],
                p_reqBodys[40], p_reqBodys[41], p_reqBodys[42], p_reqBodys[43]};

        return l_mapRet;
    }

    private byte[] makeMMSBody(String p_strTel, String p_strMsg) throws IOException {
        StringBuilder l_strSendMsg = new StringBuilder ();
        if (p_strMsg.contains("?")) {
            l_strSendMsg.append("&abc=").append(p_strTel);
        }else {
            l_strSendMsg.append("?abc=").append(p_strTel);
        }
        byte wdp_HeaderLength = (byte)0x06;
        byte wdp_PortNumbers = (byte)0x05;
        byte wdp_IELength = (byte)0x04;
        byte[] wdp_DestPort = new byte[] {(byte)0x0B, (byte)0x84};
        byte[] wdp_OrigPort = new byte[] {(byte)0x23, (byte)0xF0};
        byte[] WDP = new byte[] {wdp_HeaderLength, wdp_PortNumbers, wdp_IELength, wdp_DestPort[0], wdp_DestPort[1],
                wdp_OrigPort[0], wdp_OrigPort[1]};

        byte wsp_tid = (byte)0xBF;
        byte wsp_PDUType = (byte)0x06;
        byte wsp_HeaderLength = (byte)0x08;
        byte[] wsp_Hd_contenttype = new byte[] {(byte)0x03, (byte)0xBE};
        byte[] wsp_Hd_charset = new byte[] {(byte)0x81, (byte)0xEA};
        byte[] wsp_PushTag = new byte[] {(byte)0xB4, (byte)0x87};
        byte[] wsp_ApplicationId = new byte[] {(byte)0xAF, (byte)0x84};
        byte[] WSP = new byte[] {wsp_tid, wsp_PDUType, wsp_HeaderLength, wsp_Hd_contenttype[0], wsp_Hd_contenttype[1],
                wsp_Hd_charset[0], wsp_Hd_charset[1], wsp_PushTag[0], wsp_PushTag[1], wsp_ApplicationId[0], wsp_ApplicationId[1]};

        byte[] x_mms_message_type = new byte[] {(byte)0x8C, (byte)0x82};
        byte[] x_mms_transaction_id = new byte[] {(byte)0x98, (byte)0x33, (byte)0x36, (byte)0x35, (byte)0x38, (byte)0x32, (byte)0x34, (byte)0x00};
        byte[] x_mms_mms_version = new byte[] {(byte)0x8D, (byte)0x90};
        byte[] x_mms_from = new byte[] {(byte)0x89, (byte)0x17, (byte)0x80};
        byte[] x_mms_src = CmppUtil.str2Byte(SGIPPackData.config.srcId, SGIPPackData.config.srcId.length());
        byte[] x_mms_type = new byte[] {(byte)0x2f, (byte)0x54, (byte)0x59, (byte)0x50, (byte)0x45, (byte)0x3d, (byte)0x50, (byte)0x4c, (byte)0x4d, (byte)0x4e, (byte)0x00};
        byte[] x_mms_message_class = new byte[] {(byte)0x8A, (byte)0x80};
        byte[] x_mms_message_size = new byte[] {(byte)0x8E, (byte)0x03, (byte)0x01, (byte)0x6e, (byte)0xad};
        byte[] x_mms_expiry = new byte[] {(byte)0x88, (byte)0x05, (byte)0x81, (byte)0x03, (byte)0x00, (byte)0x05, (byte)0x00};
        byte[] x_mms_content_location = new byte[] {(byte)0x83};
        byte[] x_mms_url = CmppUtil.str2Byte(l_strSendMsg.toString(), l_strSendMsg.length());
        byte[] x_mms_end = new byte[] {(byte)0x00};

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(WDP);
        bos.write(WSP);
        bos.write(x_mms_message_type);
        bos.write(x_mms_transaction_id);
        bos.write(x_mms_mms_version);
        bos.write(x_mms_from);
        bos.write(x_mms_src);
        bos.write(x_mms_type);
        bos.write(x_mms_message_class);
        bos.write(x_mms_message_size);
        bos.write(x_mms_expiry);
        bos.write(x_mms_content_location);
        bos.write(x_mms_url);
        bos.write(x_mms_end);
        bos.flush();
        return bos.toByteArray();
    }
}
