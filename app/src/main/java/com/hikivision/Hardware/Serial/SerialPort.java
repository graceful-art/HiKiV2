package com.hikivision.Hardware.Serial;

import android.util.Log;

import com.hikivision.Hardware.HardCol;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by zera on 17-9-13.
 */

public class SerialPort {
    private static final String TAG = "SerialControal";
    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;


    public SerialPort(File device, int baudrate, int flags)  {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = serialOpen(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    protected InputStream getInputStream() {
        return mFileInputStream;
    }

    protected OutputStream getOutputStream() {
        return mFileOutputStream;
    }


    protected FileDescriptor serialOpen(String path, int baudrate,int flags){
        return HardCol.SerialOpen(path,baudrate, flags);
    }

    public void serialClose(){
        HardCol.SerialClose();
    }

}
