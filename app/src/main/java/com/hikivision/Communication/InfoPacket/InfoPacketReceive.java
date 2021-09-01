package com.hikivision.Communication.InfoPacket;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  用于接收数据的结构类型
 * @modificationHistory
 */

import android.util.Log;

import com.hikivision.Communication.CrcUtil;
import com.hikivision.UI.RobotwarnTextview;

import java.util.HashMap;
import java.util.Map;

/**接收的数据情况
 * 0x4A	0x07	0xC1	单字节PWM	                3字节TIME 2字节CRC      0-100%速率左行
 * 	    0x07	0xC2	单字节PWM	                3字节TIME 2字节CRC      0-100%速率右行
 * 	    0x06	0xC3				                3字节TIME 2字节CRC      停机后刹车
 * 	    0x06	0xC4				                3字节TIME 2字节CRC      抱闸
 * 	    0x06	0xD4				                3字节TIME 2字节CRC      确认关闭了电源
 * 	    0x16	0xE1    16个字节的现场状况数据		3字节TIME 2字节CRC 	    回复瓦斯浓度、温度、湿度…详见附表1
 *  	0x19	0xE2	19字节姿态和测距值		    3字节TIME 2字节CRC      转发F1采集的姿态和测距值，详见附表2
 * 	    0x38	0xEB	50字节电池电量、电压、温度等	3字节TIME 2字节CRC 	    回复电源状态，详见附表3
 * 	    0x06	0xB1				                3字节TIME 2字节CRC      声光报警器警告解除
 * 	    0x06	0xB2				                3字节TIME 2字节CRC      电机警告解除
 *  	0x04	0x82				                          2字节CRC D2 B7     电源告警（电压、电流异常、温度异常）
 * 	    0x04	0x83				                          2字节CRC 13 77     姿态告警
 * 	    0x04	0x84				                          2字节CRC 52 B5     距离告警
 *  	0x04	0x85				                          2字节CRC 93 75     F4转发工控机告警信息
 * 	    0x04	0x86				                          2字节CRC D3 74     F4-PC TCP失联
 * 	    0x04	0x88				                          2字节CRC 52 B0     电机告警
 * 	    0x04	0x89				                          2字节CRC 93 70     F4 发烟雾告警
 * 	    0x04	0x8A				                          2字节CRC D3 71    F4 发瓦斯告警
 * 	    0x04	0x95				                          2字节CRC 92 B9     编码器数据误差较大
 * */
/**
 * 测试用例（向左调速）4a 07 c1 08 00 00 00 ce c2 0d 0a
 *        （向右调速）4a 07 c2 08 00 00 00 8a c2 0d 0a
 *         (停止)    4a 06 c3 00 00 00 ba 35 0d 0a
 *        （急停）   4a 06 c4 00 00 00 bb 41 0d 0a
 * */

/**
 * 测试用例（心跳包）：4a 16 e1 00 01 00 01 00 00 02 02 02 02 02 02 02 00 00 00 F6 B0 0d 0a
 * */
/**
 *  ANSWER_ONE_SECOND一秒额心跳包
 * 数组下标(字节)	含义	                计算方法
 * 0	            温湿度传感器湿度值	    [0]
 * 1～2	            温湿度传感器温度值	    [1] *256 +[2]
 * 3～4	            瓦斯气体浓度	            [3] *256 +[4]
 * 5～6	            当前位置	                [5] *256 +[6]
 * 7	            机器人速率值	            [7]
 * 8	            驱动器控制位实际状态	    [8]
 * 9	            驱动器故障信息	        [9]
 * 10	            BMS电路板温度        	[10]
 * 11	            12V电源模块温度          [11]
 * 12	            24V电源模块温度       	[12]
 * 13	            电机温度                	[13]
 * 14	            电机驱动器温度	        [14]
 * 15	            腔内温度                	[15]
 * */
/**
 * ANSWER_GESTURE
 * 数组下标	        含义	      计算方法（以x轴为例）
 * 0～1          	X加速度        ax=((AxH<<8)|AxL)/32768*16g(g为重力加速度，可取 9.8m/s2)
 * 2～3	            y加速度
 * 4～5	            z加速度
 * 6～7	            X角速度	      wx=((wxH<<8)|wxL)/32768*2000°/s
 * 8～9	            y角速度
 * 10～11	        z角速度
 * 12～13	        X角度	      Roll=((RollH<<8)|RollL)/32768*180°
 * 14～15	        y角度
 * 16～17	        z角度
 * 18	            超声波测距值	  范围：0～255cm
 * */
/**
 *数组下标(字节)	        含义	                    计算方法	            单位+倍率
 * 0	                BMS总电量	                [0]	                1%
 * 1～2	                BMS总电压	                [1] *256 +[2]	    1mV
 * 3～4	                BMS放电总电流	            [3] *256 +[4]	    1mA
 * 5	                1#电芯温度 (unsigned char)	-50+[5]     	    1℃
    ```
 * 19	                15#电芯温度(unsigned char)	-50+[19]	        1℃
 * 20～21	            1#电芯电压	                [20] *256 +[21]	    1mV
 * ……
 * 48～49	            15#电芯电压	                [48] *256 +[49]	    1mV
 * */
 class SpotSituationInfo{ //16字节现场数据 必须一秒更新一次
    public byte[] spotPacket;
    //在UI中需要更新使用到的一些变量
    public int speed;
    public int gas_concentration;
    public int location;


    //现场状态数据包解码函数
    private void decode()
    {
        if(spotPacket!=null)
        {
            speed=(int)(spotPacket[7]<0 ? spotPacket[7]+256 : spotPacket[7]);
            location=(short)((spotPacket[5]&0xFF)<<8 | (spotPacket[6]&0xFF));
            gas_concentration=(short)((spotPacket[3]&0xFF)<<8 | (spotPacket[4]&0xFF));
        }
    }
    //通过接受的数据包更新现场状态信息
    public void reFresh(byte[] spotPacket)
    {
        this.spotPacket=spotPacket;
        decode();
    }
    public SpotSituationInfo(byte[] spotPacket)
    {
        this.spotPacket=spotPacket;
        decode();
    }
    public SpotSituationInfo(){}
}
 class GestureInfo{//19字节现场数据
     public byte[] gesturePacket;
    public byte[] getGesturePacket() {
        return gesturePacket;
    }
    // TODO: 2021/4/20
    // 好像这个不需要解码
    public GestureInfo(byte[] gesturePacket)
    {
        this.gesturePacket=gesturePacket;
    }
    public GestureInfo(){};
    public void reFresh(byte[]gesturePacket) { this.gesturePacket=gesturePacket;}
}
 class BatteryInfo{//50字节电池数据
     public byte[] BatteryPacket;
    public byte[] getBatteryPacket() {
        return BatteryPacket;
    }
    // TODO: 2021/4/20
    // 好像这个不需要解码
    public BatteryInfo(byte[] BatteyPacket){
        this.BatteryPacket=BatteyPacket;
    }
    public BatteryInfo(){};
    public void reFresh(byte[] BatteyPacket){
        this.BatteryPacket=BatteyPacket;
    }
}

public class InfoPacketReceive {
    final String TAG="PACKET_RECEIVE";
    public byte[] getReceivepacket(){return receivepacket;}
    private byte[] receivepacket;                   //收到的数据包
    public  SpotSituationInfo spotsituation=new SpotSituationInfo();    //现场状况信息
    public  GestureInfo       gestureInfo  =new GestureInfo();          //机器人物理状态信息
    public  BatteryInfo       batteryInfo  =new BatteryInfo();          //机器人电池状况
    public static ROBOT_STATUS      robot_status=ROBOT_STATUS.POWEROFF;         //机器人状态信息
    public int                robot_pwm_speed;      //当前PWM速度
    private INFO_RECEIVE_KIND info_receive_kind;    //收到的命令类型
    private int FlowResetTime=10;   // 定义一个定时器 200ms就减一
                                    // 如果更新这个数据结构，重新变成10
                                    // 如果减到了0，就需要重新连接了
    public void FlowTimeReset(){FlowResetTime=10;}//每当接受到ANSWER_ONE_SECOND_HEART调用一次
    public void FlowTimeMinus(){FlowResetTime--;} //每次定时器时间发生一次
    public int getFlowResetTime(){return FlowResetTime;}
    public final boolean FlowLock=true;//false状态不进行重连
                                        //true考虑定时重连
    public enum ROBOT_STATUS{
        LEFT_MOVING,
        RIGHT_MOVING,
        STOP_BRAKE,
        BAND_BRAKE,
        POWEROFF,
    }

    public byte[] getSpotPacket() { return spotsituation.spotPacket; }
    public float getSpeed(){return spotsituation.speed==255 ? 0 : (float) (spotsituation.speed*8*314*50/27/10000)/100;}
    public int getLocation(){return spotsituation.location/100;}
    public int getGas_concentration(){return spotsituation.gas_concentration==-1 ? 0 : spotsituation.gas_concentration;}


    public InfoPacketReceive() {}
    public void reFresh(byte[] fromtcp)
    {
        receivepacket=fromtcp;
        info_receive_kind=null;
        if(fromtcp[0]!=0x4a){ Log.d(TAG,"FORM ERROR");return; }
        int crc=CrcUtil.crc16(fromtcp,fromtcp.length-2);
        if(fromtcp[fromtcp.length-2]!=(byte)(crc>>0&0xFF)||fromtcp[fromtcp.length-1]!=(byte)(crc>>8&0xFF))
        {
            Log.d(TAG,"CRC ERROR");
            return;
        }
        //根据获取的数值，得到当前命令类型
        try { info_receive_kind=receive_data_map.get(Byte.valueOf(fromtcp[2])); }
        catch (ArrayIndexOutOfBoundsException e){e.printStackTrace();}

        //找到相应的命令
        if(info_receive_kind!=null){
//            Log.d(TAG,Integer.toHexString((int)fromtcp[2]));
            Log.d(TAG,info_receive_kind.name());
            switch (info_receive_kind){
                case GET_LEFT_SPEED:
//                    robot_status=ROBOT_STATUS.LEFT_MOVING;
                    robot_pwm_speed=(int)fromtcp[3];
                    break;
                case GET_RIGHT_SPEED:
//                    robot_status=ROBOT_STATUS.RIGHT_MOVING;
                    robot_pwm_speed=(int)fromtcp[3];
                    break;
                case GET_STOP_BRAKE:
//                    robot_status=ROBOT_STATUS.STOP_BRAKE;
                    robot_pwm_speed=0;
                    break;
                case GET_BAND_BRAKE:
                    robot_status=ROBOT_STATUS.BAND_BRAKE;
                    robot_pwm_speed=0;
                    break;
                case GET_POWEROFF:
                    robot_status=ROBOT_STATUS.POWEROFF;
                    robot_pwm_speed=0;
                    break;
                case ANSWER_ONE_SECOND_HEART:
                    Log.d(TAG,"ONEHEART");
                    byte [] spotdata=new byte[16];
                    for (int i=0;i<spotdata.length;i++)
                        spotdata[i]=fromtcp[i+3];
                    spotsituation.reFresh(spotdata);
                    if(spotdata[8]!=0xFF) {
                        if ((spotdata[8] & 0x01) > 0) robot_status = ROBOT_STATUS.STOP_BRAKE;
                        else if ((spotdata[8] & 0x04) > 0) robot_status = ROBOT_STATUS.LEFT_MOVING;
                        else robot_status = ROBOT_STATUS.RIGHT_MOVING;
                    }
                    FlowTimeReset();//重置输入流状态
                    break;
                case ANSWER_GESTURE:
                    byte[] gesturedata=new byte[19];
                    for (int i=0;i<gesturedata.length;i++)
                        gesturedata[i]=fromtcp[i+3];
                    gestureInfo.reFresh(gesturedata);
                    break;
                case ANSWER_FIVE_SECOND_HEART:
                    byte[] batterydata=new byte[50];
                    for(int i=0;i<batterydata.length;i++)
                        batterydata[i]=fromtcp[i+3];
                    batteryInfo.reFresh(batterydata);
                    break;
                case CONFIRM_CANCELL_LIGHT_WARN:
                    Log.d(TAG,"CANCELL LIGHT");
                    break;
                case CONFIRM_CANCELL_MOTOR_WARN:
                    Log.d(TAG,"CANCELL MOTOR");
                    break;
                case WARN_POWER:
                case WARN_GESTURE:
                case WARN_DISTANCE:
                case WARN_IPC:
                case WARN_TCP_DISCONNNECT:
                case WARN_MOTOR:
                case WARN_SMOKE:
                case WARN_GAS:
                case WARN_DATA_WRONG:
                    RobotwarnTextview.reFresh(fromtcp[2]);
                    break;
            }
        }else {
            return;
        }
    }
    private static Map<Byte,INFO_RECEIVE_KIND> receive_data_map=new HashMap<Byte, INFO_RECEIVE_KIND>(){{
        put(Byte.valueOf((byte) 0xC1),INFO_RECEIVE_KIND.GET_LEFT_SPEED);
        put(Byte.valueOf((byte) 0xC2),INFO_RECEIVE_KIND.GET_RIGHT_SPEED);
        put(Byte.valueOf((byte) 0xC3),INFO_RECEIVE_KIND.GET_STOP_BRAKE);
        put(Byte.valueOf((byte) 0xC4),INFO_RECEIVE_KIND.GET_BAND_BRAKE);
        put(Byte.valueOf((byte) 0xD4),INFO_RECEIVE_KIND.GET_POWEROFF);
        put(Byte.valueOf((byte) 0xE1),INFO_RECEIVE_KIND.ANSWER_ONE_SECOND_HEART);
        put(Byte.valueOf((byte) 0xE2),INFO_RECEIVE_KIND.ANSWER_GESTURE);
        put(Byte.valueOf((byte) 0xEB),INFO_RECEIVE_KIND.ANSWER_FIVE_SECOND_HEART);
        put(Byte.valueOf((byte) 0xB1),INFO_RECEIVE_KIND.CONFIRM_CANCELL_LIGHT_WARN);
        put(Byte.valueOf((byte) 0xB2),INFO_RECEIVE_KIND.CONFIRM_CANCELL_MOTOR_WARN);
        put(Byte.valueOf((byte) 0x82),INFO_RECEIVE_KIND.WARN_POWER);
        put(Byte.valueOf((byte) 0x83),INFO_RECEIVE_KIND.WARN_GESTURE);
        put(Byte.valueOf((byte) 0x84),INFO_RECEIVE_KIND.WARN_DISTANCE);
        put(Byte.valueOf((byte) 0x85),INFO_RECEIVE_KIND.WARN_IPC);
        put(Byte.valueOf((byte) 0x86),INFO_RECEIVE_KIND.WARN_TCP_DISCONNNECT);
        put(Byte.valueOf((byte) 0x88),INFO_RECEIVE_KIND.WARN_MOTOR);
        put(Byte.valueOf((byte) 0x89),INFO_RECEIVE_KIND.WARN_SMOKE);
        put(Byte.valueOf((byte) 0x8A),INFO_RECEIVE_KIND.WARN_GAS);
        put(Byte.valueOf((byte) 0x95),INFO_RECEIVE_KIND.WARN_DATA_WRONG);
    }};
     enum INFO_RECEIVE_KIND{
        GET_LEFT_SPEED,
        GET_RIGHT_SPEED,
        GET_STOP_BRAKE,
        GET_BAND_BRAKE,
        GET_POWEROFF,
        ANSWER_ONE_SECOND_HEART,
        ANSWER_GESTURE,
        ANSWER_FIVE_SECOND_HEART,
        CONFIRM_CANCELL_LIGHT_WARN,
        CONFIRM_CANCELL_MOTOR_WARN,
        WARN_POWER,
        WARN_GESTURE,
        WARN_DISTANCE,
        WARN_IPC,
        WARN_TCP_DISCONNNECT,
        WARN_MOTOR,
        WARN_SMOKE,
        WARN_GAS,
        WARN_DATA_WRONG
    }
}
