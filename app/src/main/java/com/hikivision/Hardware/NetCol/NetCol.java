/**
 TCP  TO  COLTROL   F4
 */
package com.hikivision.Hardware.NetCol;
import android.util.Log;
import java.net.*;
import java.io.*;


public class NetCol implements Runnable{

    final String   TAG="NET_COL";
    public String  severip;
    public int     port;
    public boolean connectstatus=false;
    public Socket  clintsocket;
    public OutputStream   cio;
    public InputStream    cin;

    public NetCol(String ip,int port)
    {
        this.severip=ip;
        this.port=port;
    }

    public void send(byte[] writebuf)
    {
        if(connectstatus==true&&writebuf!=null)
        {
            try {
                cio.write(writebuf);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void read(byte[] recebufs)
    {
        try {
            if(connectstatus==true){
                    cin.read(recebufs);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run()
    {
        try {
            if(clintsocket!=null)//如果不是第一次链接先断开链接
            {
                clintsocket.close();
                cio.close();
                cin.close();
            }
            clintsocket=new Socket(severip,port);
            clintsocket.setReceiveBufferSize(4096);
            clintsocket.setSoLinger(true, 30);
            clintsocket.setTcpNoDelay(true);
            clintsocket.setKeepAlive(true);
            connectstatus = true;
            Log.d(TAG, "CONNECT " + clintsocket.getRemoteSocketAddress() + " SUCCESS ");
            cio=clintsocket.getOutputStream();
            cin=clintsocket.getInputStream();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
