package com.hikivision.ThermalImaging;

import android.graphics.Bitmap;
import android.util.Log;

import com.company.NetSDK.INetSDK;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.IntBuffer;

public class TICameraModule implements Runnable {
    private DatagramSocket mSocket;
    private DatagramPacket mPacket;
    private byte[] mData = new byte[48*1024];
    private TISurfaceView mSurfaceView;
    private Bitmap mBitmap;
    public boolean connectState = false;

    public void startTICamera(TISurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        initUDP();
        new Thread(this).start();
    }

    private void initUDP() {
        try {
            mSocket = new DatagramSocket(8000);
            mPacket = new DatagramPacket(mData, mData.length);
            connectState = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopTICamera() {
        mSocket.close();
        connectState = false;
    }

    @Override
    public void run() {
        while(true) {
            if(mSurfaceView.bSurfaceCreated) {
                try {
                    mSocket.receive(mPacket);
                    int[] buffer = new int[mPacket.getLength()];
                    for(int i=0;i<mPacket.getLength();++i) {
                        buffer[i] = (0xFF<<24 | (mData[i]&0xFF)<<16 | (mData[i]&0xFF)<<8 | (mData[i]&0xFF));
                    }
                    mBitmap = Bitmap.createBitmap(buffer,256,192, Bitmap.Config.ARGB_8888);
                    mSurfaceView.updateImage(mBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    stopTICamera();
                }
            }
            else {
                try { Thread.sleep(50);
                }catch (InterruptedException e){e.printStackTrace();}
            }
        }
    }
}
