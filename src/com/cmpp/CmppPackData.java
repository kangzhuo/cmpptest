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
public class CmppPackData {
    private static GetProperties config = new GetProperties();
    static final int CMPP_CONNECT = 1;
    static final int CMPP_SUBMIT = 4;
    static final int CMPP_DELIVER = 5;
    static final int CMPP_QUERY = 6;
    static final int CMPP_ACTIVE_TEST = 8;

    private byte[] makeHead(int p_iType, int p_iBodyLength, byte[] p_strSeqId) throws IOException {
        byte[] totalLength = CmppUtil.int2byte(12 + p_iBodyLength);
        byte[] commandId = CmppUtil.int2byte(p_iType);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(totalLength);
        bos.write(commandId);
        bos.write(p_strSeqId);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeCmppConnectReq() throws Exception {

        Date l_nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
        String l_strTime = df.format(l_nowDate);

        byte[] l_bytes = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        byte[] sourceAddr = CmppPackData.config.sourceAddr.getBytes();
        byte[] authenticatorSource = CmppUtil.MD5(CmppPackData.config.sourceAddr + new String(l_bytes) + CmppPackData.config.pwd + l_strTime);
        byte[] version = new byte[]{CmppPackData.config.version};
        byte[] timestamp = CmppUtil.int2byte(Integer.parseInt(l_strTime));

        byte[] head = makeHead(CmppPackData.CMPP_CONNECT, 27, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(sourceAddr);
        bos.write(authenticatorSource);
        bos.write(version);
        bos.write(timestamp);
        bos.flush();
        return bos.toByteArray();
    }

    public byte[] makeCmppSubmitReq(int p_iSeq, String p_strTel, int p_iMsgType, String p_strMsg) throws IOException {
        byte[] msgId, pkTotal, pkNumber, registeredDelivery, msgLevel, serviceId, feeUserType,
                feeTerminalId, tpId, tpUdhi, msgFmt, msgSrc, feeType, feeCode, validTime, atTime, srcId,
                destUsrTl, destTerminalId, msgLength, msgContent, reserve, head;
        int l_iMsgLength;

        if (1 == p_iMsgType) { //普通短信
            msgContent = p_strMsg.getBytes("GBK");
        } else if (2 == p_iMsgType) { //超级短信
            msgContent = makeMMSBody(p_strTel, p_strMsg);
        } else if (3 == p_iMsgType) { //闪信
            msgContent = p_strMsg.getBytes();
        } else {
            return null;
        }

        l_iMsgLength = msgContent.length;
        if (l_iMsgLength >= 140) //超过单条短信限制
            return null;
        msgLength = new byte[] {(byte)l_iMsgLength};

        msgId = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}; //信息标识，由SP侧短信网关本身产生，本处填空。
        pkTotal = new byte[] {(byte)0x01};                //相同Msg_Id的信息总条数，从1开始
        pkNumber = new byte[] {(byte)0x01};               //相同Msg_Id的信息序号，从1开始
        registeredDelivery = new byte[] {CmppPackData.config.registeredDelivery};    //是否要求返回状态确认报告：0：不需要 1：需要 2：产生SMC话
        msgLevel = new byte[] {CmppPackData.config.msgLevel};    //信息级别
        serviceId = CmppUtil.str2ByteHead("xsms", 10);
        feeUserType = new byte[] {CmppPackData.config.feeUserType};
        feeTerminalId = CmppUtil.str2ByteHead(p_strTel, 21);
        tpId = new byte[] {(byte)0x00};
        tpUdhi = 1 == p_iMsgType ? new byte[] {(byte)0x00} : new byte[] {(byte)0x01};
        msgFmt = 1 == p_iMsgType ? new byte[] {(byte)0x0f} : (p_iMsgType == 2 ? new byte[] {(byte)0x04} : new byte[] {(byte)0x18});
        msgSrc = CmppPackData.config.spId.getBytes();
        feeType = CmppPackData.config.feeType.getBytes();
        feeCode = CmppUtil.str2ByteHead(CmppPackData.config.feeCode, 6);
        validTime = CmppPackData.config.validTime;
        atTime = CmppPackData.config.atTime;
        srcId = CmppUtil.str2ByteHead(CmppPackData.config.srcId, 21);
        destUsrTl = new byte[] {(byte)0x01};
        destTerminalId = CmppUtil.str2ByteHead(p_strTel, 21);
        reserve = CmppPackData.config.reserve;

        head = makeHead(CmppPackData.CMPP_SUBMIT, 147 + l_iMsgLength, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(msgId);
        bos.write(pkTotal);
        bos.write(pkNumber);
        bos.write(registeredDelivery);
        bos.write(msgLevel);
        bos.write(serviceId);
        bos.write(feeUserType);
        bos.write(feeTerminalId);
        bos.write(tpId);
        bos.write(tpUdhi);
        bos.write(msgFmt);
        bos.write(msgSrc);
        bos.write(feeType);
        bos.write(feeCode);
        bos.write(validTime);
        bos.write(atTime);
        bos.write(srcId);
        bos.write(destUsrTl);
        bos.write(destTerminalId);
        bos.write(msgLength);
        bos.write(msgContent);
        bos.write(reserve);
        bos.flush();

        return bos.toByteArray();
    }

    byte[] makeCmppDeliverReq(int p_iSeq, byte[] msgId, byte result) throws IOException {

        byte[] head = makeHead(CmppPackData.CMPP_DELIVER | 0x80000000, 9, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(msgId);
        bos.write(result);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeCmppQueryReq() throws IOException {

        Date l_nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("YYYYMMDD");
        String l_strTime = df.format(l_nowDate);

        byte[] time = CmppUtil.str2Byte(l_strTime, 8);
        byte[] queryType = new byte[] {(byte)0x00};
        byte[] queryCode = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        byte[] reserve = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        byte[] head = makeHead(CmppPackData.CMPP_QUERY, 27, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(time);
        bos.write(queryType);
        bos.write(queryCode);
        bos.write(reserve);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeCmppActiveTestReq() throws IOException {
        byte[] head = makeHead(CmppPackData.CMPP_ACTIVE_TEST, 0, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.flush();
        return bos.toByteArray();
    }

    byte[] makeCmppActiveTestResp(int p_iSeq) throws IOException {
        byte[] head = makeHead(CmppPackData.CMPP_ACTIVE_TEST | 0x80000000, 1, CmppUtil.int2byte(p_iSeq));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write((byte) 0x00);
        bos.flush();
        return bos.toByteArray();
    }

    Map<String,Object> readCmppConnectResp(byte[] p_respDatas) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_respDatas.length < 30)
            return null;

        int l_iCmppType = CmppUtil.byte2int(p_respDatas[4], p_respDatas[5], p_respDatas[6], p_respDatas[7]);
        if (CmppPackData.CMPP_CONNECT != (l_iCmppType & 0x000000ff)) { //仅需要命令的最后一字节
            return null;
        }

        int totalLength = CmppUtil.byte2int(p_respDatas[0], p_respDatas[1], p_respDatas[2], p_respDatas[3]);
        int commandId = CmppUtil.byte2int(p_respDatas[4], p_respDatas[5], p_respDatas[6], p_respDatas[7]);
        int seq = CmppUtil.byte2int(p_respDatas[8], p_respDatas[9], p_respDatas[10], p_respDatas[11]);
        int status = p_respDatas[12];
        byte[] authenticatorISMG = {p_respDatas[13], p_respDatas[14], p_respDatas[15], p_respDatas[16],
                p_respDatas[17], p_respDatas[18], p_respDatas[19], p_respDatas[20],
                p_respDatas[21], p_respDatas[22], p_respDatas[23], p_respDatas[24],
                p_respDatas[25], p_respDatas[26], p_respDatas[27], p_respDatas[28]};
        int version = p_respDatas[29];

        l_mapRet.put("totalLength", totalLength);
        l_mapRet.put("commandId", commandId);
        l_mapRet.put("seq", seq);
        l_mapRet.put("status", status);
        l_mapRet.put("authenticatorISMG", authenticatorISMG);
        l_mapRet.put("version", version);
        return l_mapRet;
    }

    Map<String,Object> readCmppSubmitResp(int p_iSeq, byte[] p_respBodys) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_respBodys.length < 9)
            return null;

        byte[] msgId = {p_respBodys[0], p_respBodys[1], p_respBodys[2], p_respBodys[3],
                p_respBodys[4], p_respBodys[5], p_respBodys[6], p_respBodys[7]};
        int result = p_respBodys[8];

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("msgId", msgId);
        l_mapRet.put("result", result);

        return l_mapRet;
    }

    Map<String,Object> readCmppDeliverResp(int p_iSeq, byte[] p_respBodys) {
        Map<String,Object> l_mapRet = new HashMap<>();

        byte[] msgId = {p_respBodys[0], p_respBodys[1], p_respBodys[2], p_respBodys[3],
                p_respBodys[4], p_respBodys[5], p_respBodys[6], p_respBodys[7]};
        byte[] destId = {p_respBodys[8], p_respBodys[9], p_respBodys[10], p_respBodys[11],
                p_respBodys[12], p_respBodys[13], p_respBodys[14], p_respBodys[15],
                p_respBodys[16], p_respBodys[17], p_respBodys[18], p_respBodys[19],
                p_respBodys[20], p_respBodys[21], p_respBodys[22], p_respBodys[23],
                p_respBodys[24], p_respBodys[25], p_respBodys[26], p_respBodys[27],
                p_respBodys[28]};
        byte[] serviceId = {p_respBodys[29], p_respBodys[30], p_respBodys[31], p_respBodys[32],
                p_respBodys[33], p_respBodys[34], p_respBodys[35], p_respBodys[36],
                p_respBodys[37], p_respBodys[38]};
        int tpPid = p_respBodys[39];
        int tpUdhi = p_respBodys[40];
        int msgFmt = p_respBodys[41];
        byte[] srcTerminalId = {p_respBodys[42], p_respBodys[43], p_respBodys[44], p_respBodys[45],
                p_respBodys[46], p_respBodys[47], p_respBodys[48], p_respBodys[49],
                p_respBodys[50], p_respBodys[51], p_respBodys[52], p_respBodys[53],
                p_respBodys[54], p_respBodys[55], p_respBodys[56], p_respBodys[57],
                p_respBodys[58], p_respBodys[59], p_respBodys[60], p_respBodys[61],
                p_respBodys[62]};
        int registeredDelivery = p_respBodys[63];
        int msgLength = p_respBodys[64];
        byte[] msgContent = new byte[msgLength];
        System.arraycopy(p_respBodys, 65, msgContent, 0, msgLength);
        byte[] reserved = new byte[8];
        System.arraycopy(p_respBodys, 65 + msgLength, reserved, 0, 8);

        byte[] stat = new byte[0], submitTime = new byte[0], doneTime = new byte[0], destTerminalId = new byte[0];
        int smscSequence = 0;
        if (1 == registeredDelivery) {
            stat = new byte[] {msgContent[8], msgContent[9], msgContent[10], msgContent[11],
                    msgContent[12], msgContent[13], msgContent[14]};
            submitTime = new byte[] {msgContent[15], msgContent[16], msgContent[17], msgContent[18],
                    msgContent[19], msgContent[20], msgContent[21], msgContent[22],
                    msgContent[23], msgContent[24]};
            doneTime = new byte[] {msgContent[25], msgContent[26], msgContent[27], msgContent[28],
                    msgContent[29], msgContent[30], msgContent[31], msgContent[32],
                    msgContent[33], msgContent[34]};
            destTerminalId = new byte[] {p_respBodys[35], p_respBodys[36], p_respBodys[37], p_respBodys[38],
                    p_respBodys[39], p_respBodys[40], p_respBodys[41], p_respBodys[42],
                    p_respBodys[43], p_respBodys[44], p_respBodys[45], p_respBodys[46],
                    p_respBodys[47], p_respBodys[48], p_respBodys[49], p_respBodys[50],
                    p_respBodys[51], p_respBodys[52], p_respBodys[53], p_respBodys[54],
                    p_respBodys[55]};
            smscSequence = CmppUtil.byte2int(p_respBodys[56], p_respBodys[57], p_respBodys[58], p_respBodys[59]);
        }

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("msgId", msgId);
        l_mapRet.put("destId", destId);
        l_mapRet.put("serviceId", serviceId);
        l_mapRet.put("tpPid", tpPid);
        l_mapRet.put("tpUdhi", tpUdhi);
        l_mapRet.put("msgFmt", msgFmt);
        l_mapRet.put("srcTerminalId", srcTerminalId);
        l_mapRet.put("registeredDelivery", registeredDelivery);
        l_mapRet.put("msgLength", msgLength);
        l_mapRet.put("msgContent", msgContent);
        l_mapRet.put("reserved", reserved);
        l_mapRet.put("stat", stat);
        l_mapRet.put("submitTime", submitTime);
        l_mapRet.put("doneTime", doneTime);
        l_mapRet.put("destTerminalId", destTerminalId);
        l_mapRet.put("smscSequence", smscSequence);

        return l_mapRet;
    }

    Map<String,Object> readCmppQueryResp(int p_iSeq, byte[] p_respDatas) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_respDatas.length < 51)
            return null;

        byte[] time = {p_respDatas[0], p_respDatas[1], p_respDatas[2], p_respDatas[3],
                p_respDatas[4], p_respDatas[5], p_respDatas[6], p_respDatas[7]};
        int queryType = p_respDatas[8];
        byte[] queryCode = {p_respDatas[9], p_respDatas[10], p_respDatas[11], p_respDatas[12],
                p_respDatas[13], p_respDatas[14], p_respDatas[15], p_respDatas[16],
                p_respDatas[17], p_respDatas[18]};
        int mtTlMsg = CmppUtil.byte2int(p_respDatas[19], p_respDatas[20], p_respDatas[21], p_respDatas[22]);
        int mtTlUsr = CmppUtil.byte2int(p_respDatas[23], p_respDatas[24], p_respDatas[25], p_respDatas[26]);
        int mtScs = CmppUtil.byte2int(p_respDatas[27], p_respDatas[28], p_respDatas[29], p_respDatas[30]);
        int mtWt = CmppUtil.byte2int(p_respDatas[31], p_respDatas[32], p_respDatas[33], p_respDatas[34]);
        int mtFl = CmppUtil.byte2int(p_respDatas[35], p_respDatas[36], p_respDatas[37], p_respDatas[38]);
        int moScs = CmppUtil.byte2int(p_respDatas[39], p_respDatas[40], p_respDatas[41], p_respDatas[42]);
        int moWt = CmppUtil.byte2int(p_respDatas[43], p_respDatas[44], p_respDatas[45], p_respDatas[46]);
        int moFl = CmppUtil.byte2int(p_respDatas[47], p_respDatas[48], p_respDatas[49], p_respDatas[50]);

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("time", time);
        l_mapRet.put("queryType", queryType);
        l_mapRet.put("queryCode", queryCode);
        l_mapRet.put("mtTlMsg", mtTlMsg);
        l_mapRet.put("mtTlUsr", mtTlUsr);
        l_mapRet.put("mtScs", mtScs);
        l_mapRet.put("mtWt", mtWt);
        l_mapRet.put("mtFl", mtFl);
        l_mapRet.put("moScs", moScs);
        l_mapRet.put("moWt", moWt);
        l_mapRet.put("moFl", moFl);

        return l_mapRet;
    }

    Map<String,Object> readCmppActiveTestResp(int p_iSeq, byte[] p_respDatas) {
        Map<String,Object> l_mapRet = new HashMap<>();

        if (p_respDatas.length < 1)
            return null;

        byte[] reserved = {p_respDatas[12]};

        l_mapRet.put("seq", p_iSeq);
        l_mapRet.put("reserved", reserved);

        return l_mapRet;
    }

    private byte[] makeMMSBody(String p_strTel, String p_strMsg) throws IOException {
        String l_strSendMsg = p_strMsg.contains("?") ? (p_strMsg + "&abc=" + p_strTel) : (p_strMsg + "?abc=" + p_strTel);

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
        byte[] x_mms_src = CmppUtil.str2Byte(CmppPackData.config.srcId, CmppPackData.config.srcId.length());
        byte[] x_mms_type = new byte[] {(byte)0x2f, (byte)0x54, (byte)0x59, (byte)0x50, (byte)0x45, (byte)0x3d, (byte)0x50, (byte)0x4c, (byte)0x4d, (byte)0x4e, (byte)0x00};
        byte[] x_mms_message_class = new byte[] {(byte)0x8A, (byte)0x80};
        byte[] x_mms_message_size = new byte[] {(byte)0x8E, (byte)0x03, (byte)0x01, (byte)0x6e, (byte)0xad};
        byte[] x_mms_expiry = new byte[] {(byte)0x88, (byte)0x05, (byte)0x81, (byte)0x03, (byte)0x00, (byte)0x05, (byte)0x00};
        byte[] x_mms_content_location = new byte[] {(byte)0x83};
        byte[] x_mms_url = CmppUtil.str2Byte(l_strSendMsg, l_strSendMsg.length());
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
