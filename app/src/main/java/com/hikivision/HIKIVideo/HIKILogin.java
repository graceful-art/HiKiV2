package com.hikivision.HIKIVideo;
import android.util.Log;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 * 海康摄像头登录方法
 * @modificationHistory
 */
public class HIKILogin {
    private final String TAG = "HIKILOGIN";
    /**
     * MESSAGE OF IP CAMERA
     */
    private int startChannel = 0; // start channel no
    private int channelNumber = 0; // channel number
    private Videoinfo videoinfo;
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private int loginID = -1; // return by NET_DVR_Login_v30
    public boolean loginStatus=false;
    public int  getStartChannel()
    {
        return startChannel;
    }
    public int  getLoginID()
    {
        return  loginID;
    }
    public void setVideoinfo(Videoinfo vid) {
        videoinfo=vid;
    }
    /**
     * DEVICE TO LOGIN
     * onCreate->CameraLogin->loginDevice->loginNormalDevice->NET_DVR_Login_V30
     * */
    public boolean CameraLogin(Videoinfo vid) {
        try {
            if (loginID < 0) {
                setVideoinfo( vid);
                loginID = loginDevice();
                if (loginID < 0) {
                    Log.e(TAG, "This device logins failed!");
                    return false;
                } else {
                    System.out.println("loginID=" + loginID);
                }
                // get instance of exception callback and set
                ExceptionCallBack oexceptionCbf = MethodUtils.getInstance().getExceptiongCbf();
                if (oexceptionCbf == null) {
                    Log.e(TAG, "ExceptionCallBack object is failed!");
                    return false;
                }

                if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                        oexceptionCbf)) {
                    Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                    return false;
                }
                Log.i(TAG, "Login sucess");
                loginStatus=true;//登录成功
            } else {
                // whether we have logout
                if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(loginID)) {
                    Log.e(TAG, " NET_DVR_Logout is failed!");
                    return false;
                }
                loginID = -1;
            }
        } catch (
                Exception err) {
            Log.e(TAG, "error: " + err.toString());
        }
        return  true;
    }
    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = videoinfo.getIp();
        int nPort = videoinfo.getPort();
        String strUser = videoinfo.getUserName();
        String strPsd = videoinfo.getPassword();
        Log.d(TAG,strIP);
        Log.d(TAG,Integer.toString(nPort));
        Log.d(TAG,strUser);
        Log.d(TAG,strPsd);
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }

        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            startChannel = m_oNetDvrDeviceInfoV30.byStartChan;
            channelNumber = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            startChannel = m_oNetDvrDeviceInfoV30.byStartDChan;
            channelNumber = m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }
        Log.i(TAG, "NET_DVR_Login is Successful!");
        return iLogID;
    }

    private int loginDevice() {
        int iLogID = -1;
        iLogID = loginNormalDevice();
        return iLogID;
    }
}
