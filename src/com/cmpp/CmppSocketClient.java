package com.cmpp;

import java.io.BufferedInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

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

    private synchronized static void setUsed(int p_iNum, int p_iValue) {
        g_iUsed[p_iNum] = p_iValue;
    }

    private synchronized static int getSocket() {
        for (int i = 0; i < g_iMaxSocket; i++) {
            if (0 == checkAndsetUsed(i) && 1 == g_iValids[i]) {
                return i;
            } else if (0 == g_iValids[i]) {
                //建立新链接
                g_sockets[i] = createConnect();
                if (g_sockets[i] != null) {
                    g_iValids[i] = 1;
                    //启动守护线程并保存
                    MyRespThread myRespThread = new MyRespThread(g_sockets[i]);
                    myRespThread.start();
                    g_myThread[i] = myRespThread;
                    //更新为使用状态返回
                    setUsed(i, 1);
                    return i;
                }
            }
        }

        return -1;
    }

    private synchronized static Socket createConnect() {
        try {
            //建立socket
            Socket l_socket = new Socket(config.host, Integer.parseInt(config.port));
            //写入登陆信息
            OutputStream l_out = l_socket.getOutputStream();
            CmppPackData cmppPackData = new CmppPackData();
            l_out.write(cmppPackData.makeCmppConnectReq());
            return l_socket;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private synchronized static void retSocket(int p_iUsed) {
        setUsed(p_iUsed, 0);
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

        try {
            OutputStream l_out = g_sockets[l_iUsed].getOutputStream();
            l_out.write(p_data);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //更改状态
            retSocket(l_iUsed);
            //注销守护线程
            g_myThread[l_iUsed].g_isInterrupted = true;
            g_myThread[l_iUsed].interrupt();
            //重置此链接
            g_iValids[l_iUsed] = 0;
            //销毁socket
            try {
                g_sockets[l_iUsed].close();
            } catch (IOException e1) {
                System.out.println(e.getMessage());
            }

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
                    printHexString(l_rets.toArray());
                    //////////////////////////////////////////////

                    sleep(100);
                } catch (InterruptedException e) {
                    g_isInterrupted = true;
                } catch (SocketTimeoutException e) {
                    //到达重试次数后返回报错
                    if (config.recvRetry == g_iRetry++) {
                        checkAndsetUsed(g_iNum);
                        g_iValids[g_iNum] = 0;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static void printHexString(Object[] p_bytes)
    {
        for (Object l_byte : p_bytes) {
            String hex = Integer.toHexString((byte)l_byte & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
    }
}
