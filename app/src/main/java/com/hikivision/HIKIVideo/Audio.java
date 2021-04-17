package com.hikivision.HIKIVideo;
import android.util.Log;
import com.hikvision.audio.AudioCodecParam;
import com.hikvision.audio.AudioEngine;
import com.hikvision.audio.AudioEngineCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_COMPRESSION_AUDIO;
import com.hikvision.netsdk.VoiceDataCallBack;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  海康摄像头话筒
 * @modificationHistory
 */
public class Audio{
    final   String TAG="Audio";
    public   HIKILogin login;
    private  AudioEngine audio = null;
    private  VoiceDataCallBack TalkCbf = null;
    private  int m_iVoiceTalkID = -1;
    private  int iRet = -1;
    private  AudioEngineCallBack.RecordDataCallBack AudioCbf = null;
    private  boolean isTalk = false;
    public   int key=0;//默认关闭

    public Audio(HIKILogin login)
    {
        this.login=login;
    }

    public int startVoiceTalk()//only support G711A/U and G722, but G722 Non publication function
    {
        //get the device current valid audio type
        NET_DVR_COMPRESSION_AUDIO compressAud = new NET_DVR_COMPRESSION_AUDIO();
        if(!HCNetSDK.getInstance().NET_DVR_GetCurrentAudioCompress(login.getLoginID(), compressAud))
        {
            System.out.println("NET_DVR_GetCurrentAudioCompress failed, error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }

        AudioCodecParam AudioParam = new AudioCodecParam();
        AudioParam.nVolume = 100; //the volume is between 0~100
        AudioParam.nChannel = AudioCodecParam.AudioChannel.AUDIO_CHANNEL_MONO;
        AudioParam.nBitWidth = AudioCodecParam.AudioBitWidth.AUDIO_WIDTH_16BIT;
        if(compressAud.byAudioEncType == 1)//G711_U
        {
            AudioParam.nCodecType = AudioCodecParam.AudioEncodeType.AUDIO_TYPE_G711U;
            AudioParam.nSampleRate = AudioCodecParam.AudioSampleRate.AUDIO_SAMPLERATE_8K;
            AudioParam.nBitRate = AudioCodecParam.AudioBitRate.AUDIO_BITRATE_16K;
        }
        else if(compressAud.byAudioEncType == 2)//G711_A
        {
            AudioParam.nCodecType = AudioCodecParam.AudioEncodeType.AUDIO_TYPE_G711A;
            AudioParam.nSampleRate = AudioCodecParam.AudioSampleRate.AUDIO_SAMPLERATE_8K;
            AudioParam.nBitRate = AudioCodecParam.AudioBitRate.AUDIO_BITRATE_16K;
        }
        else if(compressAud.byAudioEncType == 0) //G722
        {
            AudioParam.nCodecType = AudioCodecParam.AudioEncodeType.AUDIO_TYPE_G722;
            AudioParam.nSampleRate = AudioCodecParam.AudioSampleRate.AUDIO_SAMPLERATE_16K;
            AudioParam.nBitRate = AudioCodecParam.AudioBitRate.AUDIO_BITRATE_16K;
        }
        else
        {
            return -1;
        }
        //start AudioEngine
        if(!startAudioEngine(AudioParam))
        {
            return -1;
        }
        //start HCNetSDK
        if (TalkCbf==null)
        {
            TalkCbf = new VoiceDataCallBack()
            {
                public void fVoiceDataCallBack(int lVoiceComHandle, byte[] pDataBuffer, int iDataSize, int iAudioFlag)
                {
                    processDeviceVoiceData(lVoiceComHandle, pDataBuffer, iDataSize, iAudioFlag);
                }
            };
        }
        m_iVoiceTalkID = HCNetSDK.getInstance().NET_DVR_StartVoiceCom_MR_V30(login.getLoginID(), 1, TalkCbf);
        if (-1 == m_iVoiceTalkID)
        {
            stopVoiceTalk();
        }
        return m_iVoiceTalkID;
    }

    private  void processDeviceVoiceData(int lVoiceComHandle, byte[] pDataBuffer, int iDataSize, int iAudioFlag)
    {
        audio.inputData(pDataBuffer, iDataSize)	;
    }

    public boolean stopVoiceTalk()
    {
        if(audio!=null){
        HCNetSDK.getInstance().NET_DVR_StopVoiceCom(m_iVoiceTalkID);
        audio.stopRecord();
        audio.stopPlay();
        audio.close();
        return true;
        }else return false;
    }

    private boolean startAudioEngine(AudioCodecParam AudioParam)
    {
        if(audio == null)
        {
            audio = new AudioEngine(AudioEngine.CAE_INTERCOM);
        }
        //open audio engine
        iRet =audio.open();
        if(iRet != 0)
        {
            return false;
        }
        //set parameter
        iRet = audio.setAudioParam(AudioParam, AudioEngine.PARAM_MODE_PLAY);
        if(iRet != 0)
        {
            System.out.println("audio.setAudioParam PARAM_MODE_PLAY failed, error:" + iRet);
            audio.close();
            return false;
        }
        iRet = audio.setAudioParam(AudioParam, AudioEngine.PARAM_MODE_RECORDE);
        if(iRet != 0)
        {
            System.out.println("audio.setAudioParam PARAM_MODE_RECORDE failed, error:" + iRet);
            audio.close();
            return false;
        }
        //set callback
        if(AudioCbf == null)
        {
            AudioCbf = new AudioEngineCallBack.RecordDataCallBack()
            {
                public void onRecordDataCallBack(byte[] buf, int size)
                {
                    processLocalVoiceData(buf, size);
                }
            };
        }
        iRet = audio.setAudioCallBack(AudioCbf, AudioEngine.RECORDE_DATA_CALLBACK);
        if(iRet != 0)
        {
            System.out.println("audio.setAudioCallBack RECORDE_DATA_CALLBACK failed, error:" + iRet);
            audio.close();
            return false;
        }
        iRet = audio.startPlay();
        if(iRet != 0)
        {
            System.out.println("audio.startPlay failed, error:" + iRet);
            audio.close();
            return false;
        }
        iRet = audio.startRecord();
        if(iRet != 0)
        {
            System.out.println("audio.startRecord failed, error:" + iRet);
            audio.stopPlay();
            audio.close();
            return false;
        }
        return true;
    }

    private  void processLocalVoiceData(byte[] buf, int size)
    {
        HCNetSDK.getInstance().NET_DVR_VoiceComSendData(m_iVoiceTalkID, buf, size);
    }
}

