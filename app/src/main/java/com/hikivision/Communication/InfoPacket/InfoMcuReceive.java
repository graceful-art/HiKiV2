package com.hikivision.Communication.InfoPacket;

import java.util.HashMap;
import java.util.Map;

class EXBatteryInfo{//50字节电池数据
    public byte[] BatteryPacket;
    public byte[] getBatteryPacket() {
        return BatteryPacket;
    }
    // TODO: 2021/4/20
    // 好像这个不需要解码
    public EXBatteryInfo(byte[] BatteyPacket){
        this.BatteryPacket=BatteyPacket;
    }
    public EXBatteryInfo(){};
    public void reFresh(byte[] BatteyPacket){
        this.BatteryPacket=BatteyPacket;
    }
}
class EXSpotWarnInfo{ //18字节现场数据 必须一秒更新一次
    public byte[] spotPacket;
    //在UI中需要更新使用到的一些变量
    public int speed;
    public int gas_thinkness;
    public int location;

    //现场状态数据包解码函数
    private void decode()
    {
        if(spotPacket!=null)
        {
            speed=(int)spotPacket[7];
            location=(int)(spotPacket[5])*256+(int)spotPacket[6];
            gas_thinkness=(int)(spotPacket[3])*256+(int)spotPacket[4];
        }
    }
    //通过接受的数据包更新现场状态信息
    public void reFresh(byte[] spotPacket)
    {
        this.spotPacket=spotPacket;
        decode();
    }
    public EXSpotWarnInfo(byte[] spotPacket)
    {
        this.spotPacket=spotPacket;
        decode();
    }
    public EXSpotWarnInfo(){}
}
public class InfoMcuReceive {
    public byte[] getReceivepacket(){return receivepacket;}
    private byte[] receivepacket;                   //收到的数据包
    public InfoMcuReceive(){}

    //TODO:完成接受逻辑

    private static Map<Byte,INFO_MCU_RECEIVE_KIND> receive_data_map=new HashMap<Byte,INFO_MCU_RECEIVE_KIND>(){{
        put(Byte.valueOf((byte) 0x0B),  INFO_MCU_RECEIVE_KIND.ANSWER_QUERY_KEY);
        put(Byte.valueOf((byte) 0x0C),  INFO_MCU_RECEIVE_KIND.ANSWER_CONCTRL_KEY);
        put(Byte.valueOf((byte) 0xE4),  INFO_MCU_RECEIVE_KIND.ANSWER_READ_ENV_KEY);
        put(Byte.valueOf((byte) 0xEB),  INFO_MCU_RECEIVE_KIND.ANSWER_READ_BATTERY_KEY);
    }};
    public enum INFO_MCU_RECEIVE_KIND{
        ANSWER_QUERY_KEY,
        ANSWER_CONCTRL_KEY,
        ANSWER_READ_ENV_KEY,//出现了WIFI连接异常
        ANSWER_READ_BATTERY_KEY
    }
}
