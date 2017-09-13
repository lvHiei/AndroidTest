package com.lvhiei.androidtest;

/**
 * Created by mj on 17-9-12.
 */


public class JniTools {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("native-lib");
    }


    public static native void nativeAacHE2LC(String heAacPath, String lcAacpath);


    public static native void nativeAudioSoftDecoder(String aacPath);

}
