package com.hikivision.Hardware.Serial;

import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SerialCol implements Runnable{
    final String TAG="SERIAL COL";
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private int baudrate;
    private String SerialPath;
    private DataInputStream bin;
    private List<Byte> output=new ArrayList<Byte>();
    public boolean connectStatus=false;
    public SerialCol(String SericalPath,int baudrate)
    {
        this.baudrate=baudrate;
        this.SerialPath=SericalPath;
    }

    public void run()
    {
        mSerialPort = getSerialPort(SerialPath,baudrate,0);
        if(mSerialPort!=null) {
            connectStatus=true;
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            bin = new DataInputStream(mInputStream);
        }else connectStatus=false;
    }
    @Override
    public void finalize()
    {
        mSerialPort.serialClose();
    }
    private SerialPort getSerialPort(String path, int baudrate, int flag)  {
        try {
            if (mSerialPort == null) {
                /* Open the serial port */
                mSerialPort = new SerialPort(new File(path), baudrate, flag);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }

        return mSerialPort;
    }

    public void send(byte[] data){
        try {
            if (mOutputStream != null) {
                mOutputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] read()
    {
        try {
            output.clear();
            if (mSerialPort != null&&bin!=null) {
                Byte b;
                while (true) {
                    b = bin.readByte();
                    if (b == 0x0d){
                        b = bin.readByte();
                        if (b != 0x0a) {
                            output.add((byte) 0x0d);
                            output.add(b);
                        }
                        else break;
                    }
                    else output.add(b);
                }
                Iterator<Byte> iterator = output.iterator();
                int i = 0;
                byte[] bytes = new byte[output.size()];
                while (iterator.hasNext()) {
                    bytes[i] = iterator.next();
                    i++;
                }
                return bytes;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }


    public String getSerialPath()
    {
        return SerialPath;
    }
    public  int getBaudrate()
    {
        return baudrate;
    }
}
