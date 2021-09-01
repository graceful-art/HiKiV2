package com.hikivision.Communication.InfoPacket;

import android.util.Log;

import com.hikivision.Hardware.NetCol.NetCol;
import com.hikivision.UI.RobotwarnTextview;

import java.util.HashMap;
import java.util.Map;

import static com.hikivision.UI.MainActivity.mTalkModule;


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
            speed=(int)(spotPacket[7]<0 ? spotPacket[7]+256 : spotPacket[7]);
            location=(short)((spotPacket[5]&0xFF)<<8 | (spotPacket[6]&0xFF));
            gas_thinkness=(short)((spotPacket[3]&0xFF)<<8 | (spotPacket[4]&0xFF));
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
    final String TAG="MCU_RECEIVE";
    final byte Hight_Speed=0x64;
    final byte Medium_Speed=0x32;
    final byte Low_Speed=0x14;
    final byte[] WarnCommand={(byte)0x83,(byte)0x84,(byte)0x86,(byte)0x89,(byte)0x8A};
    public int batteryvalue;
    public int ControlPanel;
    public boolean haveWarn;
    private byte speed;
    private NetCol netCol;
    private byte[] receivepacket;                   //收到的数据包
    private INFO_MCU_RECEIVE_KIND info_receive_kind;    //收到的命令类型
    public EXSpotWarnInfo exspotwarninfo = new EXSpotWarnInfo();
    public EXBatteryInfo exBatteryinfo = new EXBatteryInfo();

    public InfoMcuReceive(NetCol netCol)
    {
        this.netCol = netCol;
    }
    public byte[] getReceivepacket(){return receivepacket;}

    //TODO:完成接受逻辑
    private byte getCheck(byte[] data,int n)
    {
        int check=0;
        for (int i=0;i<n;i++){check+=(int)data[i];}
        return  (byte)((check) >> 0 & 0xFF);
    }

    public void reFresh(byte[] frommcu)
    {
        receivepacket=frommcu;
        info_receive_kind=null;
        if(frommcu[0]!=0x46){ Log.d(TAG,"MCU Receive ERROR");return; }
        if(getCheck(frommcu,frommcu.length-1)!=frommcu[frommcu.length-1]) {
            Log.d(TAG,"CHECK ERROR");
            return;
        }
        //根据获取的数值，得到当前命令类型
        try { info_receive_kind=receive_data_map.get(Byte.valueOf(frommcu[2])); }
        catch (ArrayIndexOutOfBoundsException e){e.printStackTrace();}
        if(info_receive_kind!=null) {
//            Log.d(TAG, Integer.toHexString((int) frommcu[2]));
            Log.d(TAG, info_receive_kind.name());
            switch (info_receive_kind) {
                case ANSWER_QUERY_KEY:
                    batteryvalue=frommcu[3];
                    if(((frommcu[5]>>2)&0x03)==0x02) speed=Hight_Speed;
                    else if(((frommcu[5]>>2)&0x03)==0x01) speed=Medium_Speed;
                    else if(((frommcu[5]>>2)&0x03)==0x00) speed=Low_Speed;
                    if((frommcu[4]&0x01)!=0)
                    {
                        netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.SET_POWEROFF).getSendpacket());
                    }
                    else
                    {
                        if(RobotwarnTextview.haveDistanceWarn()||RobotwarnTextview.haveAttitudeWarn()||
                                RobotwarnTextview.haveGasWarn()||RobotwarnTextview.haveSmokeWarn()) {
                            haveWarn = true;
                        }
                        if(((frommcu[5]>>6)&0x01)!=0) {
                            netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.SET_BAND_BRAKE).getSendpacket());
                        }
                        else if((frommcu[5]&0x03)==0x01) {
                            if(!haveWarn) netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.SET_LEFT_SPEED,speed).getSendpacket());
                        }
                        else if((frommcu[5]&0x03)==0x02) {
                            if(!haveWarn) netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.SET_RIGHT_SPEED,speed).getSendpacket());
                        }
                        else if((ControlPanel&0x03)!=0&&(frommcu[5]&0x03)==0)
                        {
                            haveWarn = false;
                            netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.SET_STOP_BRAKE).getSendpacket());
                        }
                        if(((ControlPanel>>7)&0x01)==0&&((frommcu[5]>>7)&0x01)!=0)
                        {
                            mTalkModule.startClientTalk();
                        }
                        else if(((ControlPanel>>7)&0x01)!=0&&((frommcu[5]>>7)&0x01)==0)
                        {
                            mTalkModule.stopTalk();
                        }
                        if(((ControlPanel>>5)&0x01)==0&&((frommcu[5]>>5)&0x01)!=0)
                        {
                            try { Thread.sleep(50);
                            }catch (InterruptedException e){e.printStackTrace();}
                            netCol.send(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.CANCELL_LIGHT_WARN).getSendpacket());
                        }
                    }
                    if(frommcu[6]!=0xFF&&netCol.connectstatus==false) {
                        if ((frommcu[6] & 0x01) > 0) InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.STOP_BRAKE;
                        else if ((frommcu[6] & 0x04) > 0) InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.LEFT_MOVING;
                        else InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.RIGHT_MOVING;
                    }
                    for(int i=0;i<5;++i) {
                        if((frommcu[8]&(1<<i))>=1) RobotwarnTextview.reFresh(WarnCommand[i]);
                    }
                    ControlPanel=frommcu[4]<<8|frommcu[5];
                    break;
                case ANSWER_CONCTRL_KEY:
                    Log.d(TAG,"CONCTRL LED");
                    break;
                case ANSWER_READ_BATTERY_KEY:
                    byte[] batterydata=new byte[50];
                    for(int i=0;i<batterydata.length;++i) {
                        batterydata[i]=frommcu[i+3];
                    }
                    exBatteryinfo.reFresh(batterydata);
                    break;
                case ANSWER_READ_ENV_KEY:
                    byte [] spotdata=new byte[16];
                    for (int i=0;i<spotdata.length;i++) {
                        spotdata[i]=frommcu[i+3];
                    }
                    exspotwarninfo.reFresh(spotdata);
//                    if(spotdata[8]!=0xFF) {
//                        if ((spotdata[8] & 0x01) > 0) InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.STOP_BRAKE;
//                        else if ((spotdata[8] & 0x04) > 0) InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.LEFT_MOVING;
//                        else InfoPacketReceive.robot_status = InfoPacketReceive.ROBOT_STATUS.RIGHT_MOVING;
//                    }
                    for(int i=0;i<5;++i) {
                        if((frommcu[19]&(1<<i))>=1) RobotwarnTextview.reFresh(WarnCommand[i]);
                    }
                    break;
            }
        }else {
            return;
        }
    }

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
