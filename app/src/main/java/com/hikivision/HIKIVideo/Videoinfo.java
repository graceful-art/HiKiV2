package com.hikivision.HIKIVideo;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 * 海康摄像头基础信息
 * @modificationHistory
 */
public class Videoinfo{

    public Videoinfo(String ip ,int port , String userName ,String password)
    {
        this.ip=ip;
        this.password=password;
        this.port=port;
        this.userName=userName;
    }

    public Videoinfo()
    {
    }

    private String ip = "192.168.1.10";

    private int port=8081;

    private String userName = "admin";

    private String password = "iris2020";

    private String channel = "";

    private String cameraName = "";

    private String desc = "";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", channel='" + channel + '\'' +
                ", cameraName='" + cameraName + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

}
