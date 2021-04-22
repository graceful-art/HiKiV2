package com.hikivision.Communication;

 /* 校验码：CRC16占用两个字节，包含了一个 16 位的二进制值。CRC 值由传输设备计算出来，
  * 然后附加到数据帧上，接收设备在接收数据时重新计算 CRC 值，然后与接收到的 CRC 域中的值
  * 进行比较，如果这两个值不相等，就发生了错误。
  * 生成一个 CRC16 的流程为：
  * (1) 预置一个 16 位寄存器为 0FFFFH（全为1），称之为 CRC 寄存器。
  * (2) 把数据帧中的第一个字节的 8 位与 CRC 寄存器中的低字节进行异或运算，结果存回 CRC 寄存器。
  * (3) 将 CRC 寄存器向右移一位，最高位填以 0，最低位移出并检测。
  * (4) 如果最低位为 0：重复第三步（下一次移位）；如果最低位为 1，将 CRC 寄存器与一个预设的固定值 0A001H 进行异或运算。
  * (5) 重复第三步和第四步直到 8 次移位。这样处理完了一个完整的八位。
  * (6) 重复第 2 步到第 5 步来处理下一个八位，直到所有的字节处理结束。
  * (7) 最终 CRC 寄存器的值就是 CRC16 的值。
  */
public class CrcUtil {
    /**
     * 一个字节包含位的数量 8
     */
    private static final int BITS_OF_BYTE = 8;
    /**
     * 多项式
     */
    private static final int POLYNOMIAL = 0xA001;
    /**
     * 初始值
     */
    private static final int INITIAL_VALUE = 0xFFFF;

    /**
     * CRC16 编码
     *
     * @return 编码结果
     */
    public static int crc16(byte[] data,int length) {
        byte[] buf;
        int crc = 0xFFFF;
        if(length>=0) {
            buf = new byte[length];// 存储需要产生校验码的数据
            for (int i = 0; i < length; i++) {
                buf[i] = data[i];
            }
            int len = buf.length;
            for (int pos = 0; pos < len; pos++) {
                if (buf[pos] < 0) {
                    crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                    // crc
                } else {
                    crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
                }
                for (int i = 8; i != 0; i--) { // Loop over each bit
                    if ((crc & 0x0001) != 0) { // If the LSB is set
                        crc >>= 1; // Shift right and XOR 0xA001
                        crc ^= 0xA001;
                    } else
                        // Else LSB is not set
                        crc >>= 1; // Just shift right
                }
            }
        }
        return  crc;
    }

    public static int Make_CRC(byte[] data) {
        byte[] buf = new byte[data.length];// 存储需要产生校验码的数据
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i];
        }
        int len = buf.length;
        int crc = 0xFFFF;
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        return  crc;
    }
    /**
     * 翻转16位的高八位和低八位字节
     * @param src 翻转数字
     * @return 翻转结果
     */
    private static int revert(int src) {
        int lowByte = (src & 0xFF00) >> 8;
        int highByte = (src & 0x00FF) << 8;
        return lowByte | highByte;
    }

}


