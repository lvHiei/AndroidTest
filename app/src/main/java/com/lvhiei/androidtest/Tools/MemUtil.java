package com.lvhiei.androidtest.Tools;

import java.nio.ByteBuffer;

/**
 * Created by mj on 17-8-17.
 */


public class MemUtil {
    public static native void nativeMemCopy(ByteBuffer dst, int dstOffset, ByteBuffer src, int srcOffset, int length);
}
