package com.hikivision.UI;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  带有警告的源代码
 * @modificationHistory
 */
public class RobotwarnTextview extends AppCompatTextView{

        private static final String TAG="RobotTextView";
        //实现一个可以不需要外部控制，实现自我更新的TEXT控件
        //向外提供refresh函数，使用警告缩索引作为输入值
        class RobotWarn{
                RobotWarn(String name,int times){this.warnname=name;this.times=times;}
                public String warnname;
                public int   times;      //当前警告可以保持的节拍，如果当前节拍为0，就不进行显示
        }
        final static String[] Warnname={"电源告警","姿态告警","距离告警","工控机告警","TCP失联","电机告警","烟雾告警","瓦斯告警","误差告警"};
        private static RobotWarn robotwarn[]=new RobotWarn[Warnname.length];
        //初始化块
        {
                for(int i=0;i<robotwarn.length;i++) robotwarn[i]= new RobotWarn(Warnname[i],0);
                RobotWarnMap.put((byte)0x82,robotwarn[0]);
                RobotWarnMap.put((byte)0x83,robotwarn[1]);
                RobotWarnMap.put((byte)0x84,robotwarn[2]);
                RobotWarnMap.put((byte)0x85,robotwarn[3]);
                RobotWarnMap.put((byte)0x86,robotwarn[4]);
                RobotWarnMap.put((byte)0x88,robotwarn[5]);
                RobotWarnMap.put((byte)0x89,robotwarn[6]);
                RobotWarnMap.put((byte)0x8A,robotwarn[7]);
                RobotWarnMap.put((byte)0x95,robotwarn[8]);
                TextChangeTimerStart();
        }
        public static boolean haveWarn(){
                for(int i=0;i<robotwarn.length;i++){
                        if(robotwarn[i].times>0)return true;
                }
                return false;
        }
        public static boolean haveSmokeWarn() {
                if(robotwarn[6].times>0)return true;
                else return  false;
        }
        public static boolean haveGasWarn() {
                if(robotwarn[7].times>0)return true;
                else return  false;
        }
        public static boolean haveTcpWarn() {
                if(robotwarn[4].times>0)return true;
                else return  false;
        }
        public static Map<Byte,RobotWarn> RobotWarnMap=new HashMap<Byte,RobotWarn>();
        public static void reFresh(byte info)
        {
            RobotWarnMap.get(Byte.valueOf(info)).times=5;
        }
        private void TextChangeTimerStart()
        {
                new Timer().schedule(new TimerTask() {
                        private int currentIndex=0;//每过一秒尝试将其向后移动，如果读到times=0直接下一个
                        @Override
                        public void run() {
                                for(int i=0;i<robotwarn.length;i++)robotwarn[i].times--;//每过一秒全部减一
                                int times;//扫描次数，最多扫描8个如果一个都没有扫描到就显示运行正常
                                for(times=1;times<10;times++){//注意这个1和10改成其他的值一定会出问题
                                        int temp=(currentIndex+times)%9;
                                        if(robotwarn[temp].times>0){
                                                currentIndex=(currentIndex+times)%9;//更新显示序列
                                                break;
                                        }
                                }
                                if(times==10){
                                        setText("运行正常");
                                        currentIndex=0;
                                }
                                else {
                                        String currectString=robotwarn[currentIndex].warnname;
                                        setText(currectString);
                                }
                                try {
                                }catch (Exception e) { e.printStackTrace(); }
                        }
                }, 1000, 1000);//1秒以后启动，每1s发生一次
        }

        public RobotwarnTextview(Context context) { super(context); }
        public RobotwarnTextview(Context context,AttributeSet attrs) { super(context); }


}
