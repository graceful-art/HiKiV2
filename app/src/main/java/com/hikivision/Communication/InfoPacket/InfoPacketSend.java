package com.hikivision.Communication.InfoPacket;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  用于发送的数据类型
 * @modificationHistory
 */

import com.hikivision.Communication.CrcUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * HEAD BYTENUM     INFO
 * 0xA4	0x07	    0xC1	单字节PWM    时间戳(3byte)  CRC(2byte)      0-100%速率左行
 * 	    0x07	    0xC2	单字节PWM	时间戳         CRC             0-100%速率右行
 * 	    0x06	    0xC3				时间戳         CRC             停机后刹车
 * 	    0x06	    0xC4				时间戳         CRC             抱闸
 * 	    0x06	    0xD4				时间戳         CRC             关闭电源
 *  	0x06	    0xE1				时间戳         CRC             每秒查询现场状况（兼作心跳包）
 * 	    0x06	    0xE2				时间戳         CRC             按需查姿态和测距值
 *  	0x06	    0xEB				时间戳         CRC             每5秒查询电源状态
 *  	0x06	    0xB1				时间戳         CRC             声光报警器警告解除
 *  	0x06	    0xB2				时间戳         CRC             电机警告解除
 * 	*/
public class InfoPacketSend {
    public InfoPacketSend(INFO_SEND_KIND kind)
    {
        Calendar calendar = Calendar.getInstance();
        sendpacket=new byte[6+2];
        sendpacket[0]=(byte)0xA4;
        sendpacket[1]=0x06;
        sendpacket[2]=send_data_map.get(kind).byteValue();
        sendpacket[3]=(byte)calendar.getTime().getHours();
        sendpacket[4]=(byte)calendar.getTime().getMinutes();;
        sendpacket[5]=(byte)calendar.getTime().getSeconds();
        int crc = CrcUtil.crc16(sendpacket, 6);
        sendpacket[6] = (byte) (crc >> 0 & 0xFF);
        sendpacket[7] = (byte) (crc >> 8 & 0xFF);
    }
    public InfoPacketSend(INFO_SEND_KIND kind,int pwm)
    {
        if(kind==INFO_SEND_KIND.SET_LEFT_SPEED||kind==INFO_SEND_KIND.SET_RIGHT_SPEED) {
            Calendar calendar = Calendar.getInstance();
            sendpacket = new byte[7 + 2];
            sendpacket[0] = (byte) 0xA4;
            sendpacket[1] = 0x07;
            sendpacket[2] = send_data_map.get(kind).byteValue();
            sendpacket[3] = (byte)pwm;
            sendpacket[4] = (byte) calendar.getTime().getHours();
            sendpacket[5] = (byte) calendar.getTime().getMinutes();
            sendpacket[6] = (byte) calendar.getTime().getSeconds();
            int crc = CrcUtil.crc16(sendpacket, 7);
            sendpacket[7] = (byte) (crc >> 0 & 0xFF);
            sendpacket[8] = (byte) (crc >> 8 & 0xFF);
        }
    }

    public byte[] getSendpacket() {
        return sendpacket;
    }
    private byte[] sendpacket;

    private static Map<INFO_SEND_KIND,Byte> send_data_map=new HashMap<INFO_SEND_KIND,Byte>(){{
        put(INFO_SEND_KIND.SET_LEFT_SPEED,      Byte.valueOf((byte) 0xC1));
        put(INFO_SEND_KIND.SET_RIGHT_SPEED,     Byte.valueOf((byte) 0xC2));
        put(INFO_SEND_KIND.STOP_BRAKE,          Byte.valueOf((byte) 0xC3));
        put(INFO_SEND_KIND.BAND_BRAKE,          Byte.valueOf((byte) 0xC4));
        put(INFO_SEND_KIND.POWEROFF,            Byte.valueOf((byte) 0xD4));
        put(INFO_SEND_KIND.ONE_SECOND_HEART,    Byte.valueOf((byte) 0xE1));
        put(INFO_SEND_KIND.ASK_GESTURE,         Byte.valueOf((byte) 0xE2));
        put(INFO_SEND_KIND.FIVE_SECOND_HEART,   Byte.valueOf((byte) 0xEB));
        put(INFO_SEND_KIND.CANCELL_LIGHT_WARN,  Byte.valueOf((byte) 0xB1));
        put(INFO_SEND_KIND.CANCELL_MOTOR_WARN,  Byte.valueOf((byte) 0xB2));
    }};
    public enum INFO_SEND_KIND{
        SET_LEFT_SPEED,
        SET_RIGHT_SPEED,
        STOP_BRAKE,
        BAND_BRAKE,
        POWEROFF,
        ONE_SECOND_HEART,
        ASK_GESTURE,
        FIVE_SECOND_HEART,
        CANCELL_LIGHT_WARN,
        CANCELL_MOTOR_WARN
    }
}
