package com.hikivision.UI;
import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  带有警告的源代码
 * @modificationHistory
 */
public class RobotwarnTextview extends AppCompatTextView implements Runnable{
        private String[] warnText = {
                "LORA失联",
                "WIFI失联",
                "UDP失联",
                "TCP失联",
                "烟雾告警",
                "瓦斯告警",
                "姿态告警",
                "距离告警",
                "电机告警",
                "里程机告警",
                "工控机告警"
        };

        public enum Warn{
                /**
                 * 机器人与遥控器通信状态
                 * */
                LORAWarn,//0x87
                WIFIWarn,//0x8C
                /**
                 * 机器人与上位机通性状态
                 * */
                UDPWarn,//0x82
                TCPWarn,//0x86
                /**
                 * 烟雾瓦斯告警
                 * */
                SMOKEWarn,//0x89
                GASWarn,  //0x8A
                /**
                 * 物理状态告警
                 * */
                POSTUREWarn,//姿态告警0x83
                DISTANCEWarn,//距离告警0x84

                MOTORWarn,//0x88
                DATAWarn,//0x95
                MCUWarn//0x85
        }
        public static boolean WarnFlag[]=new boolean[11];

        public String[] showText=new String[11];//需要显示的字符串
        public int showlen=0;
        public RobotwarnTextview(Context context) {
                super(context);
        }
        public RobotwarnTextview(Context context,AttributeSet attrs) {
                super(context);
        }
        public void run()
        {
                while (true){
                        showlen=0;
                        for (int i = 0; i < WarnFlag.length; i++) {
                                if (WarnFlag[i] == true)//存在相应警告
                                        {
                                                showText[showlen] = warnText[i];
                                                showlen++;
                                        }
                                }
                        try {
                                Thread.sleep(1000);
                        }catch (InterruptedException e)
                        {
                                e.printStackTrace();
                        }
                }
        }

}
