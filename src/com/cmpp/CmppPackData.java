package com.cmpp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppPackData {
    private static GetProperties config = new GetProperties();
    public static final int CMPP_CONNECT = 1;
    public static final int CMPP_SUBMIT = 4;
    public static final int CMPP_QUERY = 6;
    public static final int CMPP_ACTIVE_TEST = 8;

    private byte[] makeHead(int p_iType, int p_iBodyLength, byte[] p_strDoneCode) throws IOException {
        byte[] totalLength, commandId, seqId;

        if (CmppPackData.CMPP_CONNECT == p_iType) {
            totalLength = CmppUtil.int2byte(12 + 27);
            commandId = CmppUtil.int2byte(CmppPackData.CMPP_CONNECT);
            seqId = p_strDoneCode;
        } else if (CmppPackData.CMPP_SUBMIT == p_iType) {
            totalLength = CmppUtil.int2byte(12 + p_iBodyLength);
            commandId = CmppUtil.int2byte(CmppPackData.CMPP_SUBMIT);
            seqId = p_strDoneCode;
        } else if (CmppPackData.CMPP_ACTIVE_TEST == p_iType){
            totalLength = CmppUtil.int2byte(12);
            commandId = CmppUtil.int2byte(CmppPackData.CMPP_SUBMIT);
            seqId = p_strDoneCode;
        } else {
            return new byte[] {};
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(totalLength);
        bos.write(commandId);
        bos.write(seqId);
        bos.flush();
        return bos.toByteArray();
    }

    public byte[] makeCmppConnectReq() throws Exception {

        Date l_nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
        String l_strTime = df.format(l_nowDate);

        byte[] l_bytes = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        byte[] sourceAddr, authenticatorSource, version, timestamp, head;
        sourceAddr = CmppPackData.config.spId.getBytes();
        authenticatorSource = CmppUtil.getMD5(CmppPackData.config.spId + new String(l_bytes) + CmppPackData.config.pwd + l_strTime).getBytes();
        version = new byte[]{CmppPackData.config.version};
        timestamp = l_strTime.getBytes();

        head = makeHead(CmppPackData.CMPP_CONNECT, 27, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01});

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
        serviceId = CmppUtil.str2Byte("xsms", 10);
        feeUserType = new byte[] {CmppPackData.config.feeUserType};
        feeTerminalId = CmppUtil.str2Byte(p_strTel, 21);
        tpId = new byte[] {(byte)0x00};
        tpUdhi = 1 == p_iMsgType ? new byte[] {(byte)0x00} : new byte[] {(byte)0x01};
        msgFmt = 1 == p_iMsgType ? new byte[] {(byte)0x0f} : (p_iMsgType == 2 ? new byte[] {(byte)0x04} : new byte[] {(byte)0x18});
        msgSrc = CmppPackData.config.spId.getBytes();
        feeType = CmppPackData.config.feeType.getBytes();
        feeCode = CmppPackData.config.feeCode.getBytes();
        validTime = CmppPackData.config.validTime;
        atTime = CmppPackData.config.atTime;
        srcId = CmppUtil.str2Byte(CmppPackData.config.srcId, 21);
        destUsrTl = new byte[] {(byte)0x01};
        destTerminalId = CmppUtil.str2Byte(p_strTel, 21);
        reserve = CmppPackData.config.reserve;

        head = makeHead(CmppPackData.CMPP_CONNECT, 147 + l_iMsgLength, CmppUtil.int2byte(p_iSeq));

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

    public byte[] makeCmppQueryReq() throws IOException {

        Date l_nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("YYYYMMDD");
        String l_strTime = df.format(l_nowDate);

        byte[] time, queryType, queryCode, reserve, head;
        time = CmppUtil.str2Byte(l_strTime, 8);
        queryType = new byte[] {(byte)0x00};
        queryCode = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        reserve = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        head = makeHead(CmppPackData.CMPP_QUERY, 27, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(head);
        bos.write(time);
        bos.write(queryType);
        bos.write(queryCode);
        bos.write(reserve);
        bos.flush();
        return bos.toByteArray();
    }

    public byte[] makeCmppActiveTest() throws IOException {
        return makeHead(CmppPackData.CMPP_ACTIVE_TEST, 0, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03});
    }

    public Map<String,String> readCmppConnectResp(List<Byte> p_respDatas) {
        Map<String,String> l_mapRet = new HashMap<>();

        if (p_respDatas.size() < 30)
            return null;

        int l_iCmppType = CmppUtil.byte2int(p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7));
        if (CmppPackData.CMPP_CONNECT != l_iCmppType) {
            return null;
        }

        byte[] totalLength = {p_respDatas.get(0), p_respDatas.get(1), p_respDatas.get(2), p_respDatas.get(3)};
        byte[] commandId = {p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7)};
        byte[] sequence = {p_respDatas.get(8), p_respDatas.get(9), p_respDatas.get(10), p_respDatas.get(11)};
        byte[] status = {p_respDatas.get(12)};
        byte[] authenticatorISMG = {p_respDatas.get(13), p_respDatas.get(14), p_respDatas.get(15), p_respDatas.get(16),
                p_respDatas.get(17), p_respDatas.get(18), p_respDatas.get(19), p_respDatas.get(20),
                p_respDatas.get(21), p_respDatas.get(22), p_respDatas.get(23), p_respDatas.get(24),
                p_respDatas.get(25), p_respDatas.get(26), p_respDatas.get(27), p_respDatas.get(28)};
        byte[] version = {p_respDatas.get(29)};

        l_mapRet.put("totalLength", new String(totalLength));
        l_mapRet.put("commandId", new String(commandId));
        l_mapRet.put("sequence", new String(sequence));
        l_mapRet.put("status", new String(status));
        l_mapRet.put("authenticatorISMG", new String(authenticatorISMG));
        l_mapRet.put("version", new String(version));
        return l_mapRet;
    }

    public Map<String,String> readCmppSubmitResp(List<Byte> p_respDatas) {
        Map<String,String> l_mapRet = new HashMap<>();

        if (p_respDatas.size() < 21)
            return null;

        int l_iCmppType = CmppUtil.byte2int(p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7));
        if (CmppPackData.CMPP_QUERY == l_iCmppType) {
            readCmppQueryResp(p_respDatas);
        } else if (CmppPackData.CMPP_ACTIVE_TEST == l_iCmppType) {
            readCmppActiveTestResp(p_respDatas);
        } else if (CmppPackData.CMPP_SUBMIT != l_iCmppType) {
            return null;
        }

        byte[] totalLength = {p_respDatas.get(0), p_respDatas.get(1), p_respDatas.get(2), p_respDatas.get(3)};
        byte[] commandId = {p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7)};
        byte[] sequence = {p_respDatas.get(8), p_respDatas.get(9), p_respDatas.get(10), p_respDatas.get(11)};
        byte[] msgId = {p_respDatas.get(12), p_respDatas.get(13), p_respDatas.get(14), p_respDatas.get(15),
                p_respDatas.get(16), p_respDatas.get(17), p_respDatas.get(18), p_respDatas.get(19)};
        byte[] result = {p_respDatas.get(20)};

        l_mapRet.put("totalLength", new String(totalLength));
        l_mapRet.put("commandId", new String(commandId));
        l_mapRet.put("sequence", new String(sequence));
        l_mapRet.put("msgId", new String(msgId));
        l_mapRet.put("result", new String(result));

        return l_mapRet;
    }

    public Map<String,String> readCmppQueryResp(List<Byte> p_respDatas) {
        Map<String,String> l_mapRet = new HashMap<>();

        if (p_respDatas.size() < 59)
            return null;

        int l_iCmppType = CmppUtil.byte2int(p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7));
        if (CmppPackData.CMPP_QUERY != l_iCmppType) {
            return null;
        }

        byte[] totalLength = {p_respDatas.get(0), p_respDatas.get(1), p_respDatas.get(2), p_respDatas.get(3)};
        byte[] commandId = {p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7)};
        byte[] sequence = {p_respDatas.get(8), p_respDatas.get(9), p_respDatas.get(10), p_respDatas.get(11)};
        byte[] time = {p_respDatas.get(12), p_respDatas.get(13), p_respDatas.get(14), p_respDatas.get(15),
                p_respDatas.get(16), p_respDatas.get(17), p_respDatas.get(18), p_respDatas.get(19)};
        byte[] queryTime = {p_respDatas.get(20)};
        byte[] queryCode = {p_respDatas.get(21), p_respDatas.get(22), p_respDatas.get(23), p_respDatas.get(24),
                p_respDatas.get(25), p_respDatas.get(26), p_respDatas.get(27), p_respDatas.get(28),
                p_respDatas.get(29), p_respDatas.get(30)};
        byte[] mtTlMsg = {p_respDatas.get(31), p_respDatas.get(32), p_respDatas.get(33), p_respDatas.get(34)};
        byte[] mtTlUsr = {p_respDatas.get(35), p_respDatas.get(36), p_respDatas.get(37), p_respDatas.get(38)};
        byte[] mtScs = {p_respDatas.get(39), p_respDatas.get(40), p_respDatas.get(41), p_respDatas.get(42)};
        byte[] mtFl = {p_respDatas.get(43), p_respDatas.get(44), p_respDatas.get(45), p_respDatas.get(46)};
        byte[] moScs = {p_respDatas.get(47), p_respDatas.get(48), p_respDatas.get(49), p_respDatas.get(50)};
        byte[] moWt = {p_respDatas.get(51), p_respDatas.get(52), p_respDatas.get(53), p_respDatas.get(54)};
        byte[] moFl = {p_respDatas.get(55), p_respDatas.get(56), p_respDatas.get(57), p_respDatas.get(58)};

        l_mapRet.put("totalLength", new String(totalLength));
        l_mapRet.put("commandId", new String(commandId));
        l_mapRet.put("sequence", new String(sequence));
        l_mapRet.put("time", new String(time));
        l_mapRet.put("queryTime", new String(queryTime));
        l_mapRet.put("queryCode", new String(queryCode));
        l_mapRet.put("mtTlMsg", new String(mtTlMsg));
        l_mapRet.put("mtTlUsr", new String(mtTlUsr));
        l_mapRet.put("mtScs", new String(mtScs));
        l_mapRet.put("mtFl", new String(mtFl));
        l_mapRet.put("moScs", new String(moScs));
        l_mapRet.put("moWt", new String(moWt));
        l_mapRet.put("moFl", new String(moFl));

        return l_mapRet;
    }

    public Map<String,String> readCmppActiveTestResp(List<Byte> p_respDatas) {
        Map<String,String> l_mapRet = new HashMap<>();

        if (p_respDatas.size() < 13)
            return null;

        int l_iCmppType = CmppUtil.byte2int(p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7));
        if (CmppPackData.CMPP_ACTIVE_TEST != l_iCmppType) {
            return null;
        }

        byte[] totalLength = {p_respDatas.get(0), p_respDatas.get(1), p_respDatas.get(2), p_respDatas.get(3)};
        byte[] commandId = {p_respDatas.get(4), p_respDatas.get(5), p_respDatas.get(6), p_respDatas.get(7)};
        byte[] sequence = {p_respDatas.get(8), p_respDatas.get(9), p_respDatas.get(10), p_respDatas.get(11)};
        byte[] reserved = {p_respDatas.get(12)};

        l_mapRet.put("totalLength", new String(totalLength));
        l_mapRet.put("commandId", new String(commandId));
        l_mapRet.put("sequence", new String(sequence));
        l_mapRet.put("reserved", new String(reserved));

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
