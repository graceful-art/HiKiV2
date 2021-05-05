package com.hikivision.Communication.InfoFlow;
import android.util.Log;

import com.hikivision.Communication.InfoPacket.InfoPacketReceive;
import com.hikivision.Communication.InfoPacket.InfoPacketSend;
import com.hikivision.Hardware.NetCol.NetCol;

import java.io.EOFException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  网络通信实现与F429的数据流
 * @modificationHistory
 */
public class InfoFlowtoTcp implements Runnable{
    private String TAG="InfoFlowtoTcp";
    private NetCol netCol;
    public InfoPacketReceive infoPacketReceive=new InfoPacketReceive();
    //发生断开以后重新连接一次
    private void ResetFlow()
    {
        Thread netth = new Thread(netCol);//prevent connect failed
        netth.start();
    }
    public InfoFlowtoTcp(final NetCol netCol)
    {
        this.infoPacketReceive=new InfoPacketReceive();
        this.netCol=netCol;

        if(this.infoPacketReceive.FlowLock==true) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        infoPacketReceive.FlowTimeMinus();
                        if (infoPacketReceive.getFlowResetTime() == 0) {
                            if(netCol.connectstatus) netCol.clintsocket.shutdownInput();
                            else {
                                ResetFlow();
                                infoPacketReceive.FlowTimeReset();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000, 200);//1秒以后启动，每200ms发生一次
        }
    }
    public void sendPacket(InfoPacketSend packetSend){
        if(netCol.connectstatus==true)
        netCol.send(packetSend.getSendpacket());
    }

    public void run()
    {
        while (true)
        {
            try {
                byte[] data = netCol.read();
                if (data != null) {
                    infoPacketReceive.reFresh(data);
                    Log.d(TAG, infoPacketReceive.robot_status.name());
                }
            }
            catch (EOFException e)
            {
                ResetFlow();
                infoPacketReceive.FlowTimeReset();
            }
        }
    }
}
