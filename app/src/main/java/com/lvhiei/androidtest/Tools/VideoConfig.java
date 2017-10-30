package com.lvhiei.androidtest.Tools;

/**
 * Created by mj on 17-8-8.
 */


public class VideoConfig {
    private int micIndex;
    private int width;
    private int height;
    private int gop;
    private int bitrate;
    private int framerate;
    private boolean isVideoSpeak;
    private byte[] bitmapBRGA;


    private int length;
    private long timestamp;


    public VideoConfig()
    {

    }

    public VideoConfig(int micIndex, int width, int height, int gop, int bitrate, int framerate, boolean isVideoSpeak)
    {
        this.micIndex = micIndex;
        this.width = width;
        this.height = height;
        this.gop = gop;
        this.bitrate = bitrate;
        this.framerate = framerate;
        this.isVideoSpeak = isVideoSpeak;
    }

    public int getMicIndex() {
        return micIndex;
    }

    public void setMicIndex(int micIndex) {
        this.micIndex = micIndex;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getGop() {
        return gop;
    }

    public void setGop(int gop) {
        this.gop = gop;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public boolean isVideoSpeak() {
        return isVideoSpeak;
    }

    public void setVideoSpeak(boolean videoSpeak) {
        isVideoSpeak = videoSpeak;
    }

    public byte[] getBitmapBRGA() {
        return bitmapBRGA;
    }

    public void setBitmapBRGA(byte[] bitmapBRGA) {
        this.bitmapBRGA = bitmapBRGA;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
