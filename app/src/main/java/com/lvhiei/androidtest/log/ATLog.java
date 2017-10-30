package com.lvhiei.androidtest.log;

import android.util.Log;

/**
 * Created by mj on 17-8-28.
 */


public class ATLog {
    private String m_tag = "android_test";

    public static final class LogLevel {
        public static final int LEVEL_VERBOSE = 0;
        public static final int LEVEL_DEBUG = 1;
        public static final int LEVEL_INFO = 2;
        public static final int LEVEL_WARN = 3;
        public static final int LEVEL_ERROR = 4;
        public static final int LEVEL_FATAL = 5;

        public static final int LEVEL_SILNECE = 100;
    }

    private int m_iLogLevel = LogLevel.LEVEL_DEBUG;

    public static boolean ms_bEnableFileLog = false;

    private static FileLog ms_fileLog = null;
    private Object logLock = new Object();

    private static boolean ms_bNativeLogLevelSetted = false;

    public ATLog(String tag) {
        m_tag = tag;
        synchronized (logLock){
            if(null == ms_fileLog){
                ms_fileLog = new FileLog();
            }
            if(!ms_bNativeLogLevelSetted){
                ms_bNativeLogLevelSetted = true;
            }
        }
    }

    public void setEnableFileLog(boolean enableFileLog){
        ms_bEnableFileLog = enableFileLog;
    }

    public void setFileLogPath(String path){
        synchronized (logLock){
            if(!ms_fileLog.isInited()){
                ms_fileLog.init(path);
            }
        }
    }

    public void i(String msg) {
        if (m_iLogLevel <= LogLevel.LEVEL_INFO) {
            Log.i(m_tag, msg);
        }

        if(ms_bEnableFileLog){
            ms_fileLog.write(LogLevel.LEVEL_INFO, m_tag, msg);
        }
    }

    public void d(String msg) {
        if (m_iLogLevel <= LogLevel.LEVEL_DEBUG) {
            Log.d(m_tag, msg);
        }


        if(ms_bEnableFileLog){
            ms_fileLog.write(LogLevel.LEVEL_DEBUG, m_tag, msg);
        }
    }

    public void w(String msg) {
        if (m_iLogLevel <= LogLevel.LEVEL_WARN) {
            Log.w(m_tag, msg);
        }

        if(ms_bEnableFileLog){
            ms_fileLog.write(LogLevel.LEVEL_WARN, m_tag, msg);
        }
    }

    public void e(String msg) {
        if (m_iLogLevel <= LogLevel.LEVEL_ERROR) {
            Log.e(m_tag, msg);
        }

        if(ms_bEnableFileLog){
            ms_fileLog.write(LogLevel.LEVEL_ERROR, m_tag, msg);
        }
    }

    public void debug(String format, Object...args){
        d(String.format(format, args));
    }

    public void info(String format, Object...args){
        i(String.format(format, args));
    }

    public void warn(String format, Object...args){
        w(String.format(format, args));
    }

    public void error(String format, Object...args){
        e(String.format(format, args));
    }

    public void flush(){
        if(ms_bEnableFileLog){
            ms_fileLog.flush();
        }
    }
}
