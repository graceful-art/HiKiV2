package com.hikivision.Hardware;
import java.io.FileDescriptor;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  基于6818的硬件控制器
 * @modificationHistory
 */
public class HardCol {
    public native static FileDescriptor SerialOpen(String path,int baudrate,int flags);
    public native static void SerialClose();
    static {
        System.loadLibrary("serial-lib");
    }
}
