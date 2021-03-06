package com.lvhiei.androidtest;

import java.nio.ByteBuffer;

/**
 * Created by mj on 17-9-12.
 */


public class JniTools {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ijkffmpeg");
        System.loadLibrary("native-lib");
    }

    // aac he to lc
    public static native void nativeAacHE2LC(String heAacPath, String lcAacpath);

    // audio soft decoder
    public static native void nativeAudioSoftDecoder(String aacPath, int threadcount);

    // audio hard decoder
    public static native long nativeOpenAudioFile(String path);
    public static native int nativeReadAAudioPacket(long thzz, ByteBuffer buffer);
    public static native void nativeCloseAudioFile(long thzz);

    public static native long nativeOpenMediaFile(String filename);
    public static native int nativeReadMediaPacket(long thzz, ByteBuffer buffer);
    public static native void nativeCloseMediaFile(long thzz);
    public static native long nativeGetMediaTimestamp(long thzz);
    public static native int nativeGetMediaType(long thzz);


    public static native int nativeJniEnvTest();
    public static native int nativeJniRecorderTest();
}
