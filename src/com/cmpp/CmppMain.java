package com.cmpp;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppMain {
    private GetProperties config = new GetProperties();
    private static int g_iSeq = 1000;
    private static Map<Integer,Object> g_mapSeq = new ConcurrentHashMap<>();
    private static Map<Object,Object> g_mapMsgId = new ConcurrentHashMap<>();

    private static Logger logger = Logger.getLogger(CmppMain.class);

    private synchronized static int getSeq() {
        g_iSeq = g_iSeq + 1;
        return g_iSeq;
    }

    //对外提供发送短信接口，发送状态异步返回，通过dealDeliver方法中添加代码处理状态回执
    public int submitSms(String p_strDoneCode, String p_strTel, String p_strMsg, int p_iType) throws Exception {
        CmppPackData cmppPackData = new CmppPackData();
        int l_iSeq = getSeq();
        CmppMain.g_mapSeq.put(l_iSeq, p_strDoneCode);

        if (CmppMain.g_mapSeq.size() >= (config.winSize * config.maxSocket)) {
            logger.info("等待窗口小于限制数值");
            Thread.sleep(100);
        }

        return CmppSocketClient.sendAndRetSocket(cmppPackData.makeCmppSubmitReq(l_iSeq, p_strTel, p_iType, p_strMsg));
    }

    //获取短信提交状态反馈接口，数据异步返回，通过dealSmsReport方法中添加代码处理回馈
    public int querySmsReport() throws Exception {
        CmppPackData cmppPackData = new CmppPackData();
        return CmppSocketClient.sendAndRetSocket(cmppPackData.makeCmppQueryReq());
    }

    //处理网关发来的消息
    void deliverSms(byte[] p_readHead, int commandId, int seq, byte[] p_readBody) throws IOException{
        CmppPackData cmppPackData = new CmppPackData();
        if (CmppPackData.CMPP_SUBMIT == (commandId & 0x000000ff)) { //仅需要命令的最后一字节,处理短信提交返回报文
            Map<String,Object> l_mapResp = cmppPackData.readCmppSubmitResp(seq, p_readBody);
            dealSubmitResp(l_mapResp);
        } else if (CmppPackData.CMPP_ACTIVE_TEST == commandId) { //对心跳送达做回应
            CmppSocketClient.sendAndRetSocket(cmppPackData.makeCmppActiveTestResp(seq));
        } else if (CmppPackData.CMPP_ACTIVE_TEST == (commandId & 0x000000ff)) { //收到心跳回应无处理
            Map<String,Object> l_mapResp = cmppPackData.readCmppActiveTestResp(seq, p_readBody);
        } else if (CmppPackData.CMPP_QUERY == (commandId & 0x000000ff)) { //收到短信状态查询反馈，这个是总体汇总反馈可以用来做数据报表
            Map<String,Object> l_mapResp = cmppPackData.readCmppQueryResp(seq, p_readBody);
            dealSmsReport(l_mapResp);
        } else if (CmppPackData.CMPP_DELIVER == commandId) {
            Map<String,Object> l_mapResp = cmppPackData.readCmppDeliverResp(seq, p_readBody);
            CmppSocketClient.sendAndRetSocket(cmppPackData.makeCmppDeliverReq(seq, (byte[]) l_mapResp.get("msgId"), (byte) 0));
            if (1 == (int) l_mapResp.get("registeredDelivery")) {
                dealDeliver(l_mapResp);
            } else {
                dealSmsRecv(l_mapResp);
            }
        } else {
            logger.error("发现非正常返回命令编号，请关注！！！");
            System.out.print("接收非正常报文数据：");
            CmppUtil.printHexStringForByte(p_readHead);
            CmppUtil.printHexStringForByte(p_readBody);
        }
    }

    //短信提交反馈处理，这个是中间结果，最终结果参考短信状态反馈
    private void dealSubmitResp(Map<String,Object> p_mapResp) {
        //根据seq设置msgId,后面dealDeliver根据msgId获取doneCode
        int l_iSeq = (int) p_mapResp.get("seq");
        if (!CmppMain.g_mapSeq.containsKey(l_iSeq)) {
            logger.error("未发现对应短信提交seq，请关注！！！");
            return;
        }
        List<Byte> msgId = CmppUtil.bytes2List((byte[])p_mapResp.get("msgId"));
        CmppMain.g_mapMsgId.put(msgId, CmppMain.g_mapSeq.get(l_iSeq).toString());
        CmppMain.g_mapSeq.remove(l_iSeq);

    }

    //实现单条短信状态反馈，如果是超级短信，短信收到并不一定展示需要以服务起访问为准，但是在网关已经计费了，钱还需要收
    private void dealDeliver(Map<String,Object> p_mapResp) {
        List<Byte> msgId = CmppUtil.bytes2List((byte[])p_mapResp.get("msgId"));
        if (!CmppMain.g_mapMsgId.containsKey(msgId)) {
            logger.error("未发现对应短信提交msgId，请关注！！！");
            return;
        }
        String l_DoneCode = CmppMain.g_mapMsgId.get(msgId).toString(); //获取提交短信时的业务流水号
        CmppMain.g_mapMsgId.remove(msgId);
        String stat = new String((byte[]) p_mapResp.get("stat"));

        if (stat.equals("DELIVRD")) {
            System.out.println("短信发送成功");
        } else if (stat.equals("EXPIRED")) {
            System.out.println("短信超过有效期，未送达");
        } else if (stat.equals("DELETED")) {
            System.out.println("短信被删除");
        } else if (stat.equals("UNDELIV")) {
            System.out.println("短信无法投递");
        } else if (stat.equals("ACCEPTD")) {
            System.out.println("中间状态，无需处理");
        } else if (stat.equals("UNKNOWN")) {
            System.out.println("无效短信请求");
        } else if (stat.equals("REJECTD")) {
            System.out.println("短信被拒绝");
        } else {
            logger.error("发现非正常短信提交状态，请关注！！！");
        }
    }

    //实现客户短信上发
    private void dealSmsRecv(Map<String,Object> p_mapResp) {
        System.out.println(p_mapResp.get("msgContent").toString()); //打印客户上发内容
    }

    //实现短信状态汇总报告反馈
    private void dealSmsReport(Map<String,Object> p_mapResp) {
        //查阅cmpp2.0文档
    }
}
