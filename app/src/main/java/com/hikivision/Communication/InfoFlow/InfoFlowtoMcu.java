package com.hikivision.Communication.InfoFlow;


import android.util.Log;

import com.hikivision.Communication.InfoPacket.InfoMcuReceive;
import com.hikivision.Hardware.NetCol.NetCol;
import com.hikivision.Hardware.Serial.SerialCol;

import java.util.HashMap;
import java.util.Map;

public class InfoFlowtoMcu implements Runnable {
    private String TAG="InfoFlowtoMcu";
    private NetCol netCol;
    private SerialCol serialCol;
    public InfoMcuReceive infomcureceive;

    public InfoFlowtoMcu(SerialCol serialCol,NetCol netCol)
    {
        this.serialCol = serialCol;
        this.netCol = netCol;
        this.infomcureceive = new InfoMcuReceive(netCol);
    }
    public void run()
    {
        while(true)
        {
            byte[] data = serialCol.read();
            if(data!=null) {
                infomcureceive.reFresh(data);
                Log.d(TAG,"MCU RECEIVE DATA");
            }
        }
    }
}
