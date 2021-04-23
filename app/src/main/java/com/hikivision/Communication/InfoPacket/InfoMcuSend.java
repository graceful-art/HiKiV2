package com.hikivision.Communication.InfoPacket;

import com.hikivision.Communication.InfoFlow.InfoFlowtoMcu;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  用于发送向MCU的数据类型
 * @modificationHistory
 */
public class InfoMcuSend {
    public byte[] getSendpacket() {
        return sendpacket;
    }
    private byte[] sendpacket;
    private static Map<INFO_MCU_SEND_KIND,Byte> send_data_map=new HashMap<INFO_MCU_SEND_KIND,Byte>(){{
        put(INFO_MCU_SEND_KIND.QUERY_KEY,         Byte.valueOf((byte) 0x0B));
        put(INFO_MCU_SEND_KIND.CONCTRL_KEY,       Byte.valueOf((byte) 0x0C));
        put(INFO_MCU_SEND_KIND.READ_ENV_KEY,      Byte.valueOf((byte) 0xE4));
        put(INFO_MCU_SEND_KIND.READ_BATTERY_KEY,  Byte.valueOf((byte) 0xEB));
    }};
    public enum INFO_MCU_SEND_KIND{
        QUERY_KEY,
        CONCTRL_KEY,
        READ_ENV_KEY,//出现了WIFI连接异常
        READ_BATTERY_KEY
    }

    private byte getCheck(byte[] data,int n)
    {
        int check=0;
        for (int i=0;i<n;i++){check+=(int)data[i];}
        return  (byte)((check) >> 0 & 0xFF);
    }
    //构造0B E4 EB三种方式
    public InfoMcuSend(INFO_MCU_SEND_KIND kind)
    {
        if(kind==INFO_MCU_SEND_KIND.CONCTRL_KEY)return;//不支持这种键值
        sendpacket=new byte[4+2];
        sendpacket[0]=0x64;
        sendpacket[1]=0x03;
        sendpacket[2]=send_data_map.get(kind);
        sendpacket[3]=getCheck(sendpacket,3);
        sendpacket[4]='\r';
        sendpacket[5]='\n';
    }

    public InfoMcuSend(INFO_MCU_SEND_KIND kind, Map<LED_KIND, LED_STATUS> conPan)
    {
        if(kind!=INFO_MCU_SEND_KIND.CONCTRL_KEY)return;
        sendpacket=new byte[6+2];
        sendpacket[0]=0x64;
        sendpacket[1]=0x04;
        sendpacket[2]=send_data_map.get(kind);
        sendpacket[3]=0;
        byte reds   =(byte)conPan.get(LED_KIND.RED_LED).ordinal();
        byte greens =(byte)conPan.get(LED_KIND.GREEN_LED).ordinal();
        byte yellows=(byte)conPan.get(LED_KIND.YELLO_LED).ordinal();
        byte buzz   =(byte)conPan.get(LED_KIND.BUZZ).ordinal();
        sendpacket[4]= (byte) ((byte) (reds<<6)|(greens<<4)|(yellows<<2)|(buzz));
        sendpacket[5]=getCheck(sendpacket,5);
        sendpacket[6]='\r';
        sendpacket[7]='\n';
    }

    //控制面板
    private Map<LED_KIND, LED_STATUS> ConPan=new HashMap<LED_KIND, LED_STATUS>(){{
        put(LED_KIND.RED_LED,   LED_STATUS.OFF);
        put(LED_KIND.GREEN_LED, LED_STATUS.OFF);
        put(LED_KIND.YELLO_LED, LED_STATUS.OFF);
        put(LED_KIND.BUZZ,      LED_STATUS.OFF);
    }};
    //考虑LED与蜂鸣器的控制，当改变发生的时候进行一次控制
    public enum LED_KIND{RED_LED,GREEN_LED,YELLO_LED,BUZZ};
    public enum LED_STATUS{OFF,SLOW_ON,QUICK_ON,ALL_ON};
}
