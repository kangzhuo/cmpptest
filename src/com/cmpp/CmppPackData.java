package com.cmpp;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppPackData {
    GetProperties config = new GetProperties();
    private static final int CMPP_CONNECT = 1;
    private static final int CMPP_SUBMIT = 4;
    private static final int CMPP_ACTIVE_TEST = 8;

    private byte[] makeHead(int p_iType, int p_iBodyLength, byte[] p_strDoneCode) {
        try {
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new byte[] {};
        }
    }

    public byte[] makeCmppConnectReq() throws Exception {

        Date l_nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
        String l_strTime = df.format(l_nowDate);

        byte[] l_bytes = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

        byte[] sourceAddr, authenticatorSource, version, timestamp, head;
        sourceAddr = config.spId.getBytes();
        authenticatorSource = CmppUtil.getMD5(config.spId + new String(l_bytes) + config.pwd + l_strTime).getBytes();
        version = new byte[]{config.version};
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

    public byte[] makeCmppSubmitReq(String p_strDoneCode, String p_strTel, int p_iType) {
        byte[] msgId, pkTotal, pkNumber, registeredDelivery, msgLevel, serviceId, feeUserType,
                feeTerminalId, tpId, tpUdhi, msgFmt, msgSrc, feeType, feeCode, validTime, atTime, srcId,
                destUsrTl, destTerminalId, msgLength, msgContent, reserve;
        msgId = new byte[8];                        //信息标识，由SP侧短信网关本身产生，本处填空。
        pkTotal = new byte[] {0x01};                //相同Msg_Id的信息总条数，从1开始
        pkNumber = new byte[] {0x01};               //相同Msg_Id的信息序号，从1开始
        registeredDelivery = new byte[] {config.registeredDelivery};    //是否要求返回状态确认报告：0：不需要 1：需要 2：产生SMC话
        msgLevel = new byte[] {config.msgLevel};    //信息级别
        serviceId = CmppUtil.stringToByte(p_strDoneCode, 10);
        feeUserType = new byte[] {config.feeUserType};
        feeTerminalId = CmppUtil.stringToByte(p_strTel, 21);
        tpId = new byte[] {0x00};
        tpUdhi = p_iType == 1 ? new byte[] {0x00} : new byte[] {0x01};
        msgFmt = p_iType == 1 ? new byte[] {0x0f} : (p_iType == 2 ? new byte[] {0x04} : new byte[] {0x18});
        msgSrc = config.spId.getBytes();
        feeType = config.feeType.getBytes();
        feeCode = config.feeCode.getBytes();
        validTime = config.validTime;
        atTime = config.atTime;
        srcId =

    }

    public byte[] makeCmppActiveTest() {

    }

    public Map<String,String> readCmppConnectResp() {

    }

    public Map<String,String> readCmppSubmitResp() {

    }
}
