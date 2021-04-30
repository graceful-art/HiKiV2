package com.hikivision.UI;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hikivision.Communication.InfoFlow.InfoFlowtoMcu;
import com.hikivision.Communication.InfoFlow.InfoFlowtoTcp;
import com.hikivision.Communication.InfoPacket.InfoMcuSend;
import com.hikivision.Communication.InfoPacket.InfoPacketReceive;
import com.hikivision.Communication.InfoPacket.InfoPacketSend;
import com.hikivision.HIKIVideo.Audio;
import com.hikivision.HIKIVideo.HIKILogin;
import com.hikivision.HIKIVideo.MethodUtils;
import com.hikivision.HIKIVideo.PlaySurfaceview;
import com.hikivision.HIKIVideo.Videoinfo;
import com.hikivision.Hardware.NetCol.NetCol;
import com.hikivision.Hardware.NetCol.Wifi;
import com.hikivision.R;
import com.hikivision.Hardware.Serial.SerialCol;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.PTZCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  主活动源代码
 * @modificationHistory
 */
public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private Context context;
    /**
     * 硬件控制器
     * */
    private SerialCol serialCol;
    private NetCol    netCol;

    /**
     * 通讯控制
     * */
    private InfoFlowtoTcp infoFlowtoTcp;
    private InfoFlowtoMcu infoFlowtoMcu;
    /**
     * 子线程
     * */
    private Thread    netth;                //网络初始化线程         单次线程
    private Thread    serialth;             //串口初始换线程
    private Thread    initThread;           //其他硬件初始化线程     单次线程
    private Thread    logicth;              //循环线程              Thread_logic
    private Thread    infoFlowTcpth;        //循环TCP通信线程        Thread_flow
    private Thread    infoFlowMcuth;        //串口接收数据线程
    /**
     * 窗口上的元素
     */
    private RobotwarnTextview  robotwarnTextview=null;
    private PlaySurfaceview    surface1   = null;
    private PlaySurfaceview    surface2   = null;
    private TextView           gas        = null;
    private TextView           speed      = null;
    private TextView           status     = null;
    private RobotProgressBar   progressBar= null;
    private ImageView          pcconncet  = null;//链接警告
    private ImageView          dir        = null;//方向警告
    private ImageView          alarm      = null;//其他警告
    private ImageView          Smoke      = null;//烟雾警告
    private Button             leftButton = null;
    private Button             rightButton= null;
    private Button             upButton   = null;
    private Button             downButton = null;
    private BatteryView        batteryView= null;
    private RelativeLayout     ptzvis     = null;
    /**
     * 图片资源
     * */
    Bitmap b_smoke;
    Bitmap b_pcconnect;
    Bitmap b_dir;
    Bitmap b_alarm;
    Bitmap b_stop;
    /**
     * 与触摸屏相关变量
     * */
    private int ptztimeout=3;                   //PTZ消除所需要的时间 5S左右
    private GestureDetector        mDetector1;  //手势检测
    private GestureDetector        mDetector2;
    private void PTZTimerStart()
    {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if(ptztimeout>0) ptztimeout--;
                    else if(ptztimeout==0) {
                        SendMessage(0x02);//隐藏掉
                        ptztimeout--;
                    }
                }catch (Exception e) { e.printStackTrace(); }
            }
        }, 1000, 1000);//1秒以后启动，每1s发生一次
    }
    private void PTZStayShow() {
        ptzvis.setVisibility(View.VISIBLE);
        ptztimeout=5;
    }

    private ColorMatrixColorFilter grayColorFilter;
    /**
     * 决定当前那个窗口
     */
    private boolean isSurface1;
    private boolean isSurface2;
    /**
     * 与摄像头有关的变量
     * */
    private  HIKILogin[] login =new HIKILogin[2];//存储登录信息
    private  Videoinfo[] vid   =new Videoinfo[2];//存储摄像头信息
    public static Audio[]audio =new Audio[2];    //存储话筒信息
    /**
     * 在线程中进行,UI显示
     * */
    public   Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x00://更新顶部UI
                    /**
                     * 更新连接图标
                     * */
                    if(robotwarnTextview.haveTcpWarn()==true)  pcconncet.setColorFilter(grayColorFilter);
                    else pcconncet.clearColorFilter();
                    /**
                     * 更新方向图标
                     * */
                    if(infoFlowtoTcp.infoPacketReceive.robot_status== InfoPacketReceive.ROBOT_STATUS.RIGHT_MOVING){
                        dir.setImageBitmap(b_dir);
                        dir.setRotation(0);
                    }else if(infoFlowtoTcp.infoPacketReceive.robot_status== InfoPacketReceive.ROBOT_STATUS.LEFT_MOVING)
                    {
                        dir.setImageBitmap(b_dir);
                        dir.setRotation(180);
                    }else {
                        dir.setImageBitmap(b_stop);
                        dir.setRotation(0);
                    }
                    /**
                     * 更新瓦斯警告图标
                     * */
                    if(robotwarnTextview.haveGasWarn()==true)alarm.clearColorFilter();
                    else alarm.setColorFilter(grayColorFilter);
                    /**
                     * 更新烟雾图标
                     * */
                    if(robotwarnTextview.haveSmokeWarn()==true) Smoke.clearColorFilter();
                    else Smoke.setColorFilter(grayColorFilter);
                    /**
                     * 更新车速
                     * */
                    String carspeed="车速:"+ infoFlowtoTcp.infoPacketReceive.getSpeed()+"m/s";
                    speed.setText(carspeed);
                    /**
                     * 更新瓦斯浓度
                     * */
                    String gasstr="瓦斯:"+ infoFlowtoTcp.infoPacketReceive.getGas_thinkness();
                    gas.setText(gasstr);
                    //更改警告信息
                    robotwarnTextview.setText(robotwarnTextview.showText);
                    //更新电池电量
                    batteryView.setPower(infoFlowtoMcu.infomcureceive.batteryvalue/2);
                    /**
                     * 更新当前状态
                     * */
                    break;
                case 0x01:
                    /**
                     * 更新机器人的位置
                     * */
                    int location= infoFlowtoTcp.infoPacketReceive.getLocation();
                    if(location==0||location==50)progressBar.setCenterColor(Color.RED);
                    else progressBar.setCenterColor(Color.GREEN);
                    progressBar.setProgress(location);
                    break;
                case 0x02:
                    ptzvis.setVisibility(View.GONE);
                    break;

                case 0x03:
                    break;

                default:
                    break;
            }
        }
    };

    public void SendMessage(final int msg)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {Message message = new Message();
                message.what = msg;
                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * UI逻辑线程
     * */
    class LogicMain extends Thread
    {
        public int Heart=0;
        public void run()
        {
            while (true)
            {
                try { Thread.sleep(100);
                }catch (InterruptedException e){e.printStackTrace();}
                Heart++;//逻辑子线程心跳包
                if(Heart%10==0)
                {//更新顶部UI
                    SendMessage(0x00);
                } else if(Heart%10==1)
                {//更新位置
                    SendMessage(0x01);
                } else if(Heart%10==2)
                {
//                    SendMessage(0x03);
                } else if(Heart%10==3)
                {
                    if(netCol.connectstatus==false)
                        serialCol.send(new InfoMcuSend(InfoMcuSend.INFO_MCU_SEND_KIND.READ_ENV_KEY).getSendpacket());
                    else
                        infoFlowtoTcp.sendPacket(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.ASK_ONE_SECOND_HEART));

                } else if(Heart%10==4)
                {
                    serialCol.send(new InfoMcuSend(InfoMcuSend.INFO_MCU_SEND_KIND.QUERY_KEY).getSendpacket());
//                    serialCol.send(new InfoMcuSend(InfoMcuSend.INFO_MCU_SEND_KIND.CONCTRL_KEY,
//                            new HashMap<InfoMcuSend.LED_KIND, InfoMcuSend.LED_STATUS>(){{
//                                put(InfoMcuSend.LED_KIND.RED_LED, InfoMcuSend.LED_STATUS.SLOW_ON);
//                                put(InfoMcuSend.LED_KIND.GREEN_LED, InfoMcuSend.LED_STATUS.QUICK_ON);
//                                put(InfoMcuSend.LED_KIND.YELLO_LED, InfoMcuSend.LED_STATUS.ALL_ON);
//                                put(InfoMcuSend.LED_KIND.BUZZ, InfoMcuSend.LED_STATUS.OFF);
//                            }}).getSendpacket());
                } else if(Heart%50==5)
                {
                    infoFlowtoTcp.sendPacket(new InfoPacketSend(InfoPacketSend.INFO_SEND_KIND.ASK_FIVE_SECOND_HEART));
                }
//                else if(Heart%100==25)
//                {
//                    if (login[0].loginStatus == false) {
//                        Thread Hiki = new Thread() {
//                            public void run() {
//                                Log.d(TAG,"第一个摄像头初始化");
//                                HiKiInit(0);//摄像头初始化
//                            }
//                        };
//                        Hiki.start();
//                    }
//                    if (login[1].loginStatus == false) {
//                        Thread Hiki = new Thread() {
//                            public void run() {
//                                Log.d(TAG,"第二个摄像头初始化");
//                                HiKiInit(1);//摄像头初始化
//                            }
//                        };
//                        Hiki.start();
//                    }
//                }
            }
        }
    }
    /**
     * 设置APP风格
     * */
    private void setStyle() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    /**
     * 隐藏导航栏
     * */
    private void setGuideBar(boolean visual) {
        if(visual) {
            View decorView = this.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    /**
     * 初始化两个摄像头
     **/
    private void HiKiInit(int index){
        if(index==0) {
            vid[0] = new Videoinfo("192.168.2.64", 8000, "admin", "iris2020");
            login[0] = new HIKILogin();
            login[0].CameraLogin(vid[0]);
            surface1.startPreview(login[0].getLoginID(), login[0].getStartChannel());
            surface1.login = login[0];
            audio[0] = new Audio(login[0]);
        }else {
            vid[1] = new Videoinfo("192.168.2.65", 8000, "admin", "root1234");
            login[1] = new HIKILogin();
            login[1].CameraLogin(vid[1]);
            surface2.startPreview(login[1].getLoginID(), login[1].getStartChannel());
            surface2.login = login[1];
            audio[1] = new Audio(login[1]);
        }
    }
    /**
     * 初始化其他硬件，以及硬件线程
     * */
    private void HardInit() {
        netCol = new NetCol("192.168.2.9", 8081);
        netth = new Thread(netCol);//prevent connect failed
        netth.start();

        serialCol=new SerialCol("/dev/ttySAC2",115200);
        serialth=new Thread(serialCol);
        serialth.start();

        infoFlowtoTcp=new InfoFlowtoTcp(netCol);
        infoFlowTcpth=new Thread(infoFlowtoTcp,"Thread_Tcp");
        infoFlowTcpth.start();

        infoFlowtoMcu=new InfoFlowtoMcu(serialCol,netCol);
        infoFlowMcuth=new Thread(infoFlowtoMcu,"Thread_Mcu");
        infoFlowMcuth.start();
    }
    private void WifiInit(String ssid,String passward)
    {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = Wifi.configWifiInfo(context, ssid, passward, 2);
        int netId = configuration.networkId;
        wifiManager.enableNetwork(netId, true);
    }

    private boolean ButtonTouchUtil(View view, MotionEvent motionEvent,int dir1,int dir2) {
        ptztimeout=5;
        HIKILogin log;
        if(isSurface1==true)log=login[0];
        else log=login[1];
        try {
            if (log.getLoginID() < 0) {
                Log.e(TAG, "please login on a device first");
                return false;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                        log.getLoginID(), log.getStartChannel(), dir1, 0)) {
                }
            }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                        log.getLoginID(), log.getStartChannel(), dir2, 1)) {
                }
            }
            return true;
        }catch (Exception err) {
            Log.e(TAG, "error: " + err.toString());
            return false;
        }
    }
    private void    SurfaceTouchUtil() {
        //SurfaceView1实现双击与滑动效果
        surface1.setLongClickable(true);
        surface1.setOnTouchListener(new View.OnTouchListener() {
            double nLenStart=0,nLenEnd=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                PTZStayShow();
                int pCount = event.getPointerCount();// 触摸设备时手指的数量
                int action = event.getAction();// 获取触屏动作。比如：按下、移动和抬起等手势动作 // 手势按下且屏幕上是两个手指数量时
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN
                        && pCount == 2) { // 获取按下时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取按下时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
                    nLenStart = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
                } else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE
                        && pCount == 2) {
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[0];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                        && pCount == 2) {// 手势抬起且屏幕上是两个手指数量时 // 获取抬起时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[0];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                isSurface1 = true;
                mDetector1.onTouchEvent(event);
                isSurface1 = false;
                return true;
            }
        });
        //SurfaceView2实现双击与滑动效果
        surface2.setLongClickable(true);
        surface2.setOnTouchListener(new View.OnTouchListener() {
            double nLenStart=0,nLenEnd=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                PTZStayShow();
                int pCount = event.getPointerCount();// 触摸设备时手指的数量
                int action = event.getAction();// 获取触屏动作。比如：按下、移动和抬起等手势动作 // 手势按下且屏幕上是两个手指数量时
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN
                        && pCount == 2) { // 获取按下时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取按下时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
                    nLenStart = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
                } else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE
                        && pCount == 2) {
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[1];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                        && pCount == 2) {// 手势抬起且屏幕上是两个手指数量时 // 获取抬起时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[1];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                isSurface2 = true;
                mDetector2.onTouchEvent(event);
                isSurface2 = false;
                return true;
            }
        });
    }
    /**
     * 活动初始化
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setStyle();
        setContentView(R.layout.activity_main);
        MethodUtils.initHCNetSDK();
        mDetector1 = new GestureDetector(this, new HiGestuewDetector());
        mDetector2 = new GestureDetector(this, new HiGestuewDetector());
        surface1         =(PlaySurfaceview)  findViewById(R.id.surface1);
        surface2         =(PlaySurfaceview)  findViewById(R.id.surface2);
        gas              =(TextView)         findViewById(R.id.Gas);
        speed            =(TextView)         findViewById(R.id.speed);
        status           =(TextView)         findViewById(R.id.status);
        robotwarnTextview=(RobotwarnTextview)findViewById(R.id.Alter);
        progressBar      =(RobotProgressBar) findViewById(R.id.progressBar);
        batteryView      =(BatteryView)      findViewById(R.id.power);
        Smoke            =(ImageView)        findViewById(R.id.smoke);
        alarm            =(ImageView)        findViewById(R.id.alarm);
        dir              =(ImageView)        findViewById(R.id.Dir);
        pcconncet        =(ImageView)        findViewById(R.id.PCconnect);
        leftButton       =(Button)           findViewById(R.id.btn_PTZ_left);
        rightButton      =(Button)           findViewById(R.id.btn_PTZ_right);
        upButton         =(Button)           findViewById(R.id.btn_PTZ_up);
        downButton       =(Button)           findViewById(R.id.btn_PTZ_down);
        ptzvis           =(RelativeLayout)   findViewById(R.id.dirlay);
        b_smoke          =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.smoke);
        b_alarm          =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
        b_dir            =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.right);
        b_pcconnect      =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.networking);
        b_stop           =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.stop);
        ptzvis.setVisibility(View.GONE);
        Smoke.setImageBitmap(b_smoke);
        alarm.setImageBitmap(b_alarm);
        dir.setImageBitmap(b_stop);
        pcconncet.setImageBitmap(b_pcconnect);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // 设置饱和度
        grayColorFilter = new ColorMatrixColorFilter(cm);
        Smoke.setColorFilter(grayColorFilter);
        alarm.setColorFilter(grayColorFilter);

        //按键设置检测
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return ButtonTouchUtil(view, motionEvent, PTZCommand.PAN_LEFT, PTZCommand.PAN_RIGHT);
            }
        });
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return ButtonTouchUtil(view, motionEvent, PTZCommand.PAN_RIGHT, PTZCommand.PAN_LEFT);
            }
        });

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return ButtonTouchUtil(view, motionEvent, PTZCommand.TILT_UP, PTZCommand.TILT_DOWN);
            }
        });
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return ButtonTouchUtil(view, motionEvent, PTZCommand.TILT_DOWN, PTZCommand.TILT_UP);
            }
        });
        //显示面板设置手势检测器
        SurfaceTouchUtil();
        PTZTimerStart();
        initThread=new Thread()
        {
            public void run() {
//                HiKiInit(0);//摄像头初始化
//                HiKiInit(1);
                HardInit();
                logicth= new Thread(new LogicMain(),"Thread_logic");
                logicth.start();
            }
        };
        initThread.start();
    }
    //手势处理监听器
    private class HiGestuewDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //移动速度大于1000时判定为滑动
            if (velocityX < -1000 && isSurface1) {
                surface2.setVisibility(View.VISIBLE);
            } else if (velocityX > 1000 && isSurface2) {
                surface1.setVisibility(View.VISIBLE);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //实现surfaceview的单独显示
            if (isSurface1) {
                surface2.setVisibility(View.GONE);
            } else if (isSurface2) {
                surface1.setVisibility(View.GONE);
            }
            return super.onDoubleTap(e);
        }
    }
}
