/**
 TCP  TO  COLTROL   F4
 */
package com.hikivision.Hardware.NetCol;
import android.util.Log;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class NetCol implements Runnable{

    final String   TAG="NET_COL";
    public String  severip;
    public int     port;
    public boolean connectstatus=false;
    public Socket  clintsocket;
    public SocketAddress socketAddress;
    private OutputStream        cio;
    private InputStream         cin;
    private DataInputStream    bin;
    private List<Byte>   output=new ArrayList<Byte>();
    public NetCol(String ip,int port)
    {
        this.severip=ip;
        this.port=port;
        socketAddress = new InetSocketAddress(ip,port);
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

    public byte[] read() throws EOFException {
        try {
            output.clear();
            if(connectstatus==true&&clintsocket!=null&&bin!=null){
            Byte b;
            while (true){
                b=bin.readByte();
                if(b==0x0d)break;
                else output.add(b);
            }
            if(bin.readByte()!=0x0a){
                Log.d(TAG,"error");
                return null;
            }
            Iterator<Byte> iterator = output.iterator();
            int i=0;
            byte[] bytes=new byte[output.size()];
            while (iterator.hasNext()) {
                bytes[i] = iterator.next();
                i++;
            }
            return bytes;
        }}
        catch (EOFException e)
        {
            disconnect();
            e.printStackTrace();
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void disconnect()
    {
        if(connectstatus) {
            try {
                connectstatus=false;
                bin.close();
                cin.close();
                cio.close();
                clintsocket.close();
                Log.d(TAG, "DISCONNECT");
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void run()
    {
        try {
//            disconnect();
//            clintsocket=new Socket(severip,port);
            clintsocket = new Socket();
            clintsocket.connect(socketAddress, 1500);
            clintsocket.setReceiveBufferSize(4096);
            clintsocket.setSoLinger(true, 30);
            clintsocket.setTcpNoDelay(true);
            clintsocket.setKeepAlive(true);
            cio=clintsocket.getOutputStream();
            cin=clintsocket.getInputStream();
            bin=new DataInputStream(cin);
            connectstatus = true;
            Log.d(TAG, "CONNECT " + clintsocket.getRemoteSocketAddress() + " SUCCESS ");
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
