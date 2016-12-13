package com.cmpp;

import org.apache.log4j.Logger;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/29.
 */
class CmppSocketClient {
    private static GetProperties config = new GetProperties();

    private static int g_iMaxSocket = config.maxSocket;//最大链接数
    private static MyRespThread[] g_myThread = new MyRespThread[g_iMaxSocket];//线程
    private static Socket[] g_sockets = new Socket[g_iMaxSocket];//socket
    private static int[] g_iValids = new int[g_iMaxSocket];//可用
    private static int[] g_iUsed = new int[g_iMaxSocket];//链接是否可用

    private static Logger logger = Logger.getLogger(CmppSocketClient.class);

    private synchronized static int checkAndsetUsed(int p_iNum) {
        if (0 == g_iUsed[p_iNum]) {
            g_iUsed[p_iNum] = 1;
            return 0;
        } else {
            return 1;
        }
    }

    private static void unSetUsed(int p_iNum) {
        g_iUsed[p_iNum] = 0;
    }

    private synchronized static int getSocket() {
        logger.debug("收到分配链接请求，共" + g_iMaxSocket + "链接");
        String l_strValid = "", l_strUnUsed = "";
        for (int i = 0; i < g_iMaxSocket; i++) {
            if (1 == g_iValids[i]) {
                l_strValid = l_strValid + i + ",";
            }
            if (0 == g_iUsed[i]) {
                l_strUnUsed = l_strUnUsed + i + ",";
            }
        }
        logger.debug("其中有效链接编号：" + l_strValid);
        logger.debug("其中未用链接编号：" + l_strUnUsed);

        for (int i = 0; i < g_iMaxSocket; i++) {
            if (0 == checkAndsetUsed(i)) {
                if (1 == g_iValids[i]) {
                    logger.info("分配链接编号【" + i + "】。");
                    return i;
                } else if (0 == g_iValids[i]) {
                    //建立新链接
                    logger.debug("发现失效链接，重新开始建立链接，编号【" + i + "】");
                    g_sockets[i] = createConnect();
                    if (g_sockets[i] != null) {
                        g_iValids[i] = 1;
                        //启动守护线程并保存
                        MyRespThread myRespThread = new MyRespThread(i);
                        myRespThread.start();
                        logger.debug("守护线程【" + i + "】启动");
                        g_myThread[i] = myRespThread;
                        //更新为使用状态返回
                        //setUsed(i, 1); 已经占用了
                        logger.info("分配链接编号【" + i + "】。");
                        return i;
                    } else {
                        logger.error("建立链接失败，释放占用！！！");
                        unSetUsed(i);
                    }
                }
            }
        }
        logger.error("分配链接失败！！！");

        return -1;
    }

    private static void retSocket(int p_iUsed) {
        unSetUsed(p_iUsed);
    }

    private synchronized static Socket createConnect() {
        try {
            //建立socket
            Socket l_socket = new Socket(config.host, Integer.parseInt(config.port));
            logger.debug("初始化socket");
            //写入登陆信息
            OutputStream l_out = l_socket.getOutputStream();
            CmppPackData cmppPackData = new CmppPackData();
            logger.debug("传输登陆数据");
            System.out.print("发送登陆报文：");
            CmppUtil.printHexStringForByte(cmppPackData.makeCmppConnectReq());
            l_out.write(cmppPackData.makeCmppConnectReq());

            l_socket.setSoTimeout(config.timeOut * 1000); //设置超时时间
            BufferedInputStream l_in = new BufferedInputStream(l_socket.getInputStream());
            logger.debug("接收登陆结果");
            byte[] l_read = new byte[30];
            int l_iGetLen = 0, l_iReadLen;
            while (l_iGetLen < 30) {
                l_iReadLen = l_in.read(l_read, l_iGetLen, 30 - l_iGetLen);
                if (-1 == l_iReadLen) {
                    logger.error("读取链接建立返回数据失败，未读取到完整数据！！！");
                    return null;
                }
                l_iGetLen = l_iGetLen + l_iReadLen;
            }
            System.out.print("接收登陆报文数据：");
            CmppUtil.printHexStringForByte(l_read);

            Map<String,Object> l_mapResp = cmppPackData.readCmppConnectResp(l_read);
            if (l_mapResp == null || !l_mapResp.containsKey("status")) {
                logger.error("读取链接建立返回数据失败，解析返回数据异常！！！");
                return null;
            } else if (0 != (int) l_mapResp.get("status")) {
                logger.error("读取链接建立返回数据失败【" + l_mapResp.get("status") + "】，最高支持版本【" + l_mapResp.get("version") + "】！！！");
                return null;
            }
            logger.info("建立链接成功。");

            return l_socket;
        } catch (Exception e) {
            logger.error("链接建立失败，出现异常！！！");
            logger.error(e.getMessage());
            return null;
        }
    }

    private static int destroyConnect(int p_iNum) {
        //销毁守护线程
        g_myThread[p_iNum].g_isInterrupted = true;
        g_myThread[p_iNum].interrupt();
        logger.debug("守护线程【" + p_iNum + "】销毁");
        //socket失效
        g_iValids[p_iNum] = 0;
        //socket销毁
        try {
            g_sockets[p_iNum].close();
        } catch (Exception e) {
            logger.error("链接销毁失败，出现异常！！！");
            logger.error(e.getMessage());
        }
        //socket置为未占用
        retSocket(p_iNum);
        logger.info("链接销毁成功。");

        return 0;
    }

    static int sendAndRetSocket(byte[] p_data) {
        int l_iRetry = 0;
        int l_iUsed = getSocket();
        //没有可用链接则等待
        while (l_iUsed < 0 || l_iUsed > g_iMaxSocket) {
            logger.info("没有获取到链接，重试第" + l_iRetry + "次。");
            try {
                Thread.sleep((long) (10 + Math.random() * (90)));
            } catch (InterruptedException e) {
                logger.error("sleep异常！！！");
                logger.error(e.getMessage());
            }
            l_iUsed = getSocket();
            //到达重试次数后返回报错
            if (config.sendRetry == l_iRetry++) {
                return -1;
            }
        }

        logger.info("成功获取到链接，编号【" + l_iUsed + "】开始写入数据。");
        //写数据
        try {
            OutputStream l_out = g_sockets[l_iUsed].getOutputStream();
            System.out.print("发送报文数据：");
            CmppUtil.printHexStringForByte(p_data);
            l_out.write(p_data);
        } catch (IOException e) {
            logger.error("发送数据异常！！！");
            logger.error(e.getMessage());
            destroyConnect(l_iUsed);
            return -1;
        }

        retSocket(l_iUsed);
        logger.info("成功写入数据，编号【" + l_iUsed + "】释放占用。");
        return 0;
    }

    private static class MyRespThread extends Thread {
        private int g_iNum;
        private int g_iRetry = 0;
        volatile boolean g_isInterrupted = false;

        MyRespThread(int p_iNum) {
            g_iNum = p_iNum;
        }

        public void run() {
            while (!g_isInterrupted) {
                try {
                    //g_sockets[g_iNum].setSoTimeout(config.timeOut * 1000); 超时时间建立socket时已经设置
                    BufferedInputStream l_in = new BufferedInputStream(g_sockets[g_iNum].getInputStream());
                    logger.debug("链接中剩余未读取字节数：" + l_in.available());
                    //取12字节头
                    byte[] l_read = new byte[12];
                    int l_iGetLen = 0, l_iReadLen;
                    while (l_iGetLen < 12) {
                        l_iReadLen = l_in.read(l_read, l_iGetLen, 12 - l_iGetLen);
                        if (-1 == l_iReadLen) {
                            logger.error("未读取到完整数据，重新获取！！！");
                            System.out.print("接收不完整报文头数据：");
                            CmppUtil.printHexStringForByte(l_read);
                            continue;
                        }
                        g_iRetry = 0; //初始化心跳链接计数器
                        l_iGetLen = l_iGetLen + l_iReadLen;
                    }
                    System.out.print("接收报文头数据：");
                    CmppUtil.printHexStringForByte(l_read);
                    //根据长度取后续报文
                    int totalLength = CmppUtil.byte2int(l_read[0], l_read[1], l_read[2], l_read[3]) - 12;
                    int commandId = CmppUtil.byte2int(l_read[4], l_read[5], l_read[6], l_read[7]);
                    int seq = CmppUtil.byte2int(l_read[8], l_read[9], l_read[10], l_read[11]);
                    byte[] l_readBody = new byte[totalLength];
                    l_iGetLen = 0;
                    while (l_iGetLen < totalLength && totalLength > 0) {
                        l_iReadLen = l_in.read(l_readBody, l_iGetLen, totalLength - l_iGetLen);
                        if (-1 == l_iReadLen) {
                            logger.error("未读取到完整数据，重新获取！！！");
                            System.out.print("接收不完整报文体数据：");
                            CmppUtil.printHexStringForByte(l_readBody);
                            continue;
                        }
                        l_iGetLen = l_iGetLen + l_iReadLen;
                    }
                    System.out.print("接收报文体数据：");
                    CmppUtil.printHexStringForByte(l_readBody);
                    CmppMain cmppMain = new CmppMain();
                    ////////////////////////////////////////////////////////////////////////////////
                    cmppMain.deliverSms(l_read, commandId, seq, l_readBody);
                    ////////////////////////////////////////////////////////////////////////////////
                    sleep(100);
                } catch (InterruptedException e) {
                    logger.info("守护线程【" + g_iNum + "】终止。");
                    g_isInterrupted = true;
                } catch (SocketTimeoutException e) {
                    //logger.info("守护线程【" + g_iNum + "】超时读取，做第" + g_iRetry + "次超时处理。"); //测试时服务端发送心跳就断开链接，发送心跳回执不断链接，这里不做重试判断当超时就发心跳
                    logger.info("守护线程【" + g_iNum + "】超时读取，发送心跳");
                    if (0 == checkAndsetUsed(g_iNum)) { //先占用
                        //if (config.recvRetry <= g_iRetry++) { //到达重试次数后返回报错
                        //    logger.info("守护线程【" + g_iNum + "】到达重试上限，开始销毁链接。");
                        //    destroyConnect(g_iNum);
                        //    break;
                        //} else {
                            //超过1秒没有信息则发送心跳
                            try {
                                OutputStream l_out = g_sockets[g_iNum].getOutputStream();
                                CmppPackData cmppPackData = new CmppPackData();
                        //        logger.info("守护线程【" + g_iNum + "】发送心跳。");
                                System.out.print("发送心跳报文：");
                                CmppUtil.printHexStringForByte(cmppPackData.makeCmppActiveTestResp(3));
                                l_out.write(cmppPackData.makeCmppActiveTestResp(3));
                            } catch (IOException e1) {
                                logger.error("发送心跳包异常！！！");
                                logger.error(e1.getMessage());
                                destroyConnect(g_iNum);
                                break;
                            }
                        //}
                        unSetUsed(g_iNum);
                    }
                } catch (Exception e) {
                    logger.error("出现异常！！！");
                    logger.error(e.getMessage());
                    destroyConnect(g_iNum);
                }
            }
        }
    }
}
