package com.lvhiei.androidtest.log;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;


public class FileLog {
    private static final int LOG_COUNT = 100;

    private String m_strFileName = "";
    private Queue<String> m_quMsgs = new LinkedList<String>();

    // 因为日志只在一个进程中，所以只用一个锁就可以
    private Object m_obLock = new Object();

    private boolean m_bInited = false;

    private String m_strFilePath = Environment.getExternalStorageDirectory() + "/vvlive/log/";

    public void init(String path) {
        m_strFilePath = path;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(new Date());
        this.m_strFileName = m_strFilePath + "/vvav-java-" + timeStamp + ".vvlog";

        File dir = new File(m_strFilePath);

//        deleteDir(dir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        m_bInited = true;
    }

    public boolean isInited(){
        return m_bInited;
    }

    public void write(int level, String tag, String msg) {
        SimpleDateFormat timeFmt = new SimpleDateFormat("MM-dd hh:mm:ss.SSS");
        String formatMsg = String.format("%s %s %s %s\r\n", this.formatLevel(level), timeFmt.format(new Date()).toString(), this.formatTag(tag), msg);

        synchronized (this.m_quMsgs) {
            this.m_quMsgs.add(formatMsg);
        }

        if (this.m_quMsgs.size() >= LOG_COUNT) {
            this.flush();
        }
    }

    public String getFileName() {
        return m_strFileName;
    }

    private String formatLevel(int level) {
        switch (level) {
            case ATLog.LogLevel.LEVEL_VERBOSE:
                return "V";
            case ATLog.LogLevel.LEVEL_DEBUG:
                return "D";
            case ATLog.LogLevel.LEVEL_INFO:
                return "I";
            case ATLog.LogLevel.LEVEL_WARN:
                return "W";
            case ATLog.LogLevel.LEVEL_ERROR:
                return "E";
            default:
                return "U";
        }
    }

    public void flush() {
        Queue<String> queue = new LinkedList<String>();

        synchronized (this.m_quMsgs) {
            queue.addAll(this.m_quMsgs);
            this.m_quMsgs.clear();
        }
        if (queue.isEmpty()) {
            return;
        }

        synchronized (m_obLock) {
            FileLock lock = null;
            FileWriter writer = null;
            try {
                writer = new FileWriter(this.m_strFileName, true);
                for (String s : queue) {
                    writer.write(s);
                }
                writer.flush();
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                return;
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (lock != null) {
                        lock.release();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    private String formatTag(String tag) {
        String result = tag;
//		int end = tag.length();
//		if (end > 50) {
//			result = tag.substring(end - 50, end);
//		} else {
//			end = 50 - end;
//			StringBuilder stringBuilder = new StringBuilder(50);
//			for (int i = 0; i < end; ++i) {
//				stringBuilder.append(' ');
//			}
//			stringBuilder.append(result);
//			result = stringBuilder.toString();
//		}
        return result;
    }
}
