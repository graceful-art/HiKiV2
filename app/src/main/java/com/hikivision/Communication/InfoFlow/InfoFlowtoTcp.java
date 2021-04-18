package com.hikivision.Communication.InfoFlow;
import com.hikivision.Communication.InfoPacket.InfoPacketSend;
import com.hikivision.Hardware.NetCol.NetCol;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  网络通信实现与F429的数据流
 * @modificationHistory
 */
public class InfoFlowtoTcp implements Runnable{
    private NetCol netCol;
    public InfoFlowtoTcp(NetCol netCol)
    {
        this.netCol=netCol;
    }
    public void sendPacket(InfoPacketSend packetSend)
    {
        netCol.send(packetSend.getSendpacket());
    }

    public void run()
    {

    }
}
