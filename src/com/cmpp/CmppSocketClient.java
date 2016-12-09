package com.cmpp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by kangbo on 2016/11/29.
 */
public class CmppSocketClient {
    private static GetProperties config = new GetProperties();

    private static int g_iMaxSocket = config.maxSocket;//最大链接数
    private static MyRespThread[] g_myThread = new MyRespThread[g_iMaxSocket];//线程
    private static Socket[] g_sockets = new Socket[g_iMaxSocket];//socket
    private static int[] g_iValids = new int[g_iMaxSocket];//可用
    private static int[] g_iUsed = new int[g_iMaxSocket];//链接是否可用

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
        for (int i = 0; i < g_iMaxSocket; i++) {
            if (0 == checkAndsetUsed(i)) {
                if (1 == g_iValids[i]) {
                    return i;
                } else if (0 == g_iValids[i]) {
                    //建立新链接
                    g_sockets[i] = createConnect();
                    if (g_sockets[i] != null) {
                        g_iValids[i] = 1;
                        //启动守护线程并保存
                        MyRespThread myRespThread = new MyRespThread(i);
                        myRespThread.start();
                        g_myThread[i] = myRespThread;
                        //更新为使用状态返回
                        //setUsed(i, 1); 已经占用了
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    private static void retSocket(int p_iUsed) {
        unSetUsed(p_iUsed);
    }

    private synchronized static Socket createConnect() {
        try {
            //建立socket
            Socket l_socket = new Socket(config.host, Integer.parseInt(config.port));
            //写入登陆信息
            OutputStream l_out = l_socket.getOutputStream();
            CmppPackData cmppPackData = new CmppPackData();
            l_out.write(cmppPackData.makeCmppConnectReq());

            l_socket.setSoTimeout(config.timeOut*1000);
            BufferedInputStream l_in = new BufferedInputStream(l_socket.getInputStream());
            int l_iRet;
            List<Byte> l_rets = new LinkedList<>();
            while ((l_iRet = l_in.read()) != -1) {
                l_rets.add((byte) l_iRet);
            }
            Map<String,String> l_mapResp = cmppPackData.readCmppConnectResp(l_rets);
            if (l_mapResp == null || !l_mapResp.containsKey("status")) {
                System.out.println("建立链接失败，返回数据异常");
                return null;
            } else if (!l_mapResp.get("status").equals("0")) {
                System.out.println("建立链接失败【" + l_mapResp.get("status") + "】，最高支持版本【" + l_mapResp.get("version") + "】");
                return null;
            }

            return l_socket;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static int destroyConnect(int p_iNum) {
        //销毁守护线程
        g_myThread[p_iNum].g_isInterrupted = true;
        g_myThread[p_iNum].interrupt();
        //socket失效
        g_iValids[p_iNum] = 0;
        //socket销毁
        try {
            g_sockets[p_iNum].close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //socket置为未占用
        retSocket(p_iNum);

        return 0;
    }

    public static int sendAndRetSocket(byte[] p_data) {
        int l_iRetry = 0;
        int l_iUsed = getSocket();
        //没有可用链接则等待
        while (l_iUsed < 0 || l_iUsed > g_iMaxSocket) {
            try {
                Thread.sleep((long) (10 + Math.random() * (90)));
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            l_iUsed = getSocket();
            //到达重试次数后返回报错
            if (config.sendRetry == l_iRetry++) {
                return -1;
            }
        }
        //写数据
        try {
            OutputStream l_out = g_sockets[l_iUsed].getOutputStream();
            l_out.write(p_data);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            destroyConnect(l_iUsed);
            return -1;
        }

        retSocket(l_iUsed);
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
                    g_sockets[g_iNum].setSoTimeout(config.timeOut);
                    BufferedInputStream l_in = new BufferedInputStream(g_sockets[g_iNum].getInputStream());
                    int l_iRet;
                    List<Byte> l_rets = new LinkedList<>();
                    while ((l_iRet = l_in.read()) != -1) {
                        g_iRetry = 0;
                        l_rets.add((byte) l_iRet);
                    }
                    //////////////////////////////////////////////
                    //处理待补充
                    CmppUtil.printHexString(l_rets.toArray());
                    CmppPackData cmppPackData = new CmppPackData();
                    cmppPackData.readCmppSubmitResp(l_rets);
                    //////////////////////////////////////////////

                    sleep(1000);
                } catch (InterruptedException e) {
                    g_isInterrupted = true;
                } catch (SocketTimeoutException e) {
                    if (0 == checkAndsetUsed(g_iNum)) { //先占用
                        if (config.recvRetry >= g_iRetry++) { //到达重试次数后返回报错
                            destroyConnect(g_iNum);
                            break;
                        } else {
                            //超过1秒没有信息则发送心跳
                            try {
                                OutputStream l_out = g_sockets[g_iNum].getOutputStream();
                                CmppPackData cmppPackData = new CmppPackData();
                                l_out.write(cmppPackData.makeCmppActiveTest());
                            } catch (IOException e1) {
                                System.out.println(e1.getMessage());
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
