package com.hikivision.DaHuaVideo;

import android.content.Context;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Surface;

import com.company.NetSDK.CB_pfAudioDataCallBack;
import com.company.NetSDK.EM_USEDEV_MODE;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_SPEAK_PARAM;
import com.company.NetSDK.NET_TALK_TRANSFER_PARAM;
import com.company.NetSDK.SDKDEV_TALKFORMAT_LIST;
import com.company.NetSDK.SDK_TALK_CODING_TYPE;
import com.company.PlaySDK.IPlaySDK;
import com.company.PlaySDK.IPlaySDKCallBack;


/**
 * Created by 29779 on 2017/4/8.
 */
public class TalkModule {
    private final String TAG = "TalkModule";
    SDKDEV_TALKFORMAT_LIST mTalkFormatList = new SDKDEV_TALKFORMAT_LIST();
    public long mTalkHandle = 0;
    boolean mOpenAudioRecord = false;
    boolean bTransfer = false;
    CB_pfAudioDataCallBack cbAudioDataCallBack = null;
    IPlaySDKCallBack.pCallFunction cbAudioRecord = null;
    IPLoginModule mLoginModule;
    long mLoginHandle;
    int nPort = 99;

    public TalkModule(IPLoginModule loginModule) {
        this.mLoginModule = loginModule;
        this.mLoginHandle = loginModule.getLoginHandle();
        cbAudioDataCallBack = new AudioDataCallBack();
        cbAudioRecord = new AudioRecordCallBack();
    }


    public boolean startTalk() {
        ///Set talk encode type
        ///设置对讲编码类型
        mTalkFormatList.type[0].encodeType = SDK_TALK_CODING_TYPE.SDK_TALK_PCM;
        mTalkFormatList.type[0].dwSampleRate = 8000;
        mTalkFormatList.type[0].nAudioBit = 16;
        mTalkFormatList.type[0].nPacketPeriod = 25;
        if (!INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_ENCODE_TYPE, mTalkFormatList.type[0])) {
            Log.e(TAG,"Set Talk Encode Mode Failed!");
            return false;
        }

        ///Set talk transfer mode
        ///设置对讲转发模式
        bTransfer = true;
        NET_TALK_TRANSFER_PARAM mTalkTransfer = new NET_TALK_TRANSFER_PARAM();
        mTalkTransfer.bTransfer = bTransfer;
        if (!INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_TRANSFER_MODE, mTalkTransfer)) {
            Log.e(TAG,"Set Transfer Mode Failed!");
            return false;
        }

        if (bTransfer) {
            int chn = 0;
            if (!INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_TALK_CHANNEL, chn)) {
                Log.e(TAG,"Set Transfer Channel Failed!");
                return false;
            }
        }

        nPort = 99;
        return talk();
    }

    public boolean startClientTalk() {
        ///Set talk mode
        ///设置对讲模式
        mTalkFormatList.type[0].encodeType = SDK_TALK_CODING_TYPE.SDK_TALK_PCM;
        mTalkFormatList.type[0].dwSampleRate = 8000;
        mTalkFormatList.type[0].nAudioBit = 16;
        mTalkFormatList.type[0].nPacketPeriod = 25;
        if( ! INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_ENCODE_TYPE, mTalkFormatList.type[0]) ) {
            Log.e(TAG,"Set Talk Encode Mode Failed!");
            return false;
        }

        if( ! INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_CLIENT_MODE, null)) {
            Log.e(TAG,"Set Talk Client Mode Failed!");
            return false;
        }

        NET_SPEAK_PARAM stParam = new NET_SPEAK_PARAM();
        stParam.nMode = 0;
        stParam.nEnableWait = 0;
        if( ! INetSDK.SetDeviceMode(mLoginHandle, EM_USEDEV_MODE.SDK_TALK_SPEAK_PARAM, stParam)) {
            Log.e(TAG,"Set Talk Speak Param Failed!");
            return false;
        }

        nPort = 0;
        return talk();
    }

    public boolean isTalking() {
        return mTalkHandle != 0;
    }

    private boolean talk() {
        ///Start talk
        ///开始对讲
        mTalkHandle = INetSDK.StartTalkEx(mLoginHandle, cbAudioDataCallBack);
        if (0 != mTalkHandle) {
            ///Start audio record
            ///开始音频录音
            boolean bSuccess = startAudioRecord();
            if (!bSuccess) {
                Log.e(TAG,"Start Audio Record Failed!");
                INetSDK.StopTalkEx(mTalkHandle);
                return false;
            } else {
                mOpenAudioRecord = true;
            }
        } else {
            Log.e(TAG,"Start Talk Failed!");
            return false;
        }
        return true;
    }

    public boolean stopTalk() {
        if(mOpenAudioRecord) {
            stopAudioRecord();
        }

        if(0 != mTalkHandle) {
            ///Stop audio talk to the device
            ///停止设备的音频对讲
            if(INetSDK.StopTalkEx(mTalkHandle)) {
                mTalkHandle = 0;
            } else {
                return false;
            }
        }
        return true;
    }

    ///Talk callback
    ///对讲回调函数
    public class AudioDataCallBack implements CB_pfAudioDataCallBack
    {
        public void invoke(long lTalkHandle, byte pDataBuf[], byte byAudioFlag)
        {
//            ToolKits.writeLog("AudioDataCallBack received " + byAudioFlag);
            if(mTalkHandle == lTalkHandle)
            {
                ///byAudioFlag Audio data home sign, 0:means audio data collected by local audio recording list; 1:means received audio data sent by devie
                ///byAudioFlag 音频数据归属标志, 0:表示是本地录音库采集的音频数据; 1:表示收到的设备发过来的音频数据
                if(1 == byAudioFlag)
                {

                    ///You can use PLAY SDK to decode to get PCM and then encode to other formats if you get a uniform formats.
                    ///通过PLAY SDK解码获取PCM，并且如果你获取统一格式，请对其他格式进行编码
                    IPlaySDK.PLAYInputData(nPort, pDataBuf, pDataBuf.length);
                }
            }
        }
    }

    private boolean startAudioRecord()	{

        boolean bRet = false;

        ///Then specify frame length
        ///指定的帧长度
        int nFrameLength = 320;

        ///Then call PLAYSDK library to begin recording audio
        ///调用PLAYSDK库，来开始录制音频
        boolean bOpenRet = IPlaySDK.PLAYOpenStream(nPort,null,0,1024*1024) == 0? false : true;
        if(bOpenRet) {
            boolean bPlayRet = IPlaySDK.PLAYPlay(nPort, (Surface)null) == 0? false : true;
            if(bPlayRet) {
                IPlaySDK.PLAYPlaySoundShare(nPort);
                boolean bSuccess = IPlaySDK.PLAYOpenAudioRecord(cbAudioRecord,mTalkFormatList.type[0].nAudioBit,
                        mTalkFormatList.type[0].dwSampleRate, nFrameLength, 0) == 0? false : true;
                if(bSuccess) {
                    bRet = true;
                    Log.e(TAG,"nAudioBit = " + mTalkFormatList.type[0].nAudioBit + "\n" + "dwSampleRate = "
                                      + mTalkFormatList.type[0].dwSampleRate + "\n" + "nFrameLength = " + nFrameLength + "\n");
                } else {
                    IPlaySDK.PLAYStopSoundShare(nPort);
                    IPlaySDK.PLAYStop(nPort);
                    IPlaySDK.PLAYCloseStream(nPort);
                }
            } else {
                IPlaySDK.PLAYCloseStream(nPort);
            }
        }

        return bRet;
    }

    private void stopAudioRecord()	{
        mOpenAudioRecord = false;
        IPlaySDK.PLAYCloseAudioRecord();
        IPlaySDK.PLAYStop(nPort);
        IPlaySDK.PLAYStopSoundShare(nPort);
        IPlaySDK.PLAYCloseStream(nPort);
    }

    public class AudioRecordCallBack implements IPlaySDKCallBack.pCallFunction {
        public void invoke(byte[] pDataBuffer,int nBufferLen, long pUserData) {
            try
            {
                ///encode
                ///编码
//                ToolKits.writeLog("AudioRecord send " + nBufferLen);
                byte encode[] = AudioRecord(pDataBuffer);

                ///send user's audio data to device.
                ///发送语音数据到设备.
                long lSendLen = INetSDK.TalkSendData(mTalkHandle, encode);
                if(lSendLen != (long)encode.length) {
                    ///Error occurred when sending the user audio data to the device.
                    ///发送音频数据给设备失败.
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    byte[] AudioRecord(byte[] pDataBuffer) {
        int DataLength = pDataBuffer.length;
        byte pCbData[] = null;
        pCbData = new byte[8+DataLength];

        pCbData[0] = (byte) 0x00;
        pCbData[1] = (byte) 0x00;
        pCbData[2] = (byte) 0x01;
        pCbData[3] = (byte) 0xF0;
        pCbData[4] = (byte) 0x0C;

        pCbData[5] = 0x02; // 8k
        pCbData[6]=(byte)(DataLength & 0x00FF);
        pCbData[7]=(byte)(DataLength >> 8);
        System.arraycopy(pDataBuffer, 0, pCbData, 8, DataLength);
        return pCbData;
    }

    ///Get talk format list，this demo only use PCM.
    ///获取语音对讲格式列表, 本demo只用到了PCM.
    public void getCodeType() {
        if(!INetSDK.QueryDevState(mLoginHandle, FinalVar.SDK_DEVSTATE_TALK_ECTYPE, mTalkFormatList, 4000)) {
            Log.e(TAG,"QueryDevState TalkList Failed!");
            return;
        }
    }

    ///Is Transfer Mode
    ///是否转发模式
    public boolean isTransfer(){
        return bTransfer;
    }

}
