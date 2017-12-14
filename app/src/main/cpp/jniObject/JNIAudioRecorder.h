//
// Created by mj on 17-12-14.
//

#ifndef ANDROIDTEST_JNIAUDIORECORDER_H
#define ANDROIDTEST_JNIAUDIORECORDER_H

#include <jni.h>


class JniHelper;

/**
 * AudioRecorder的Jni层接口
 */
class JNIAudioRecorder {
public:
    JNIAudioRecorder(JavaVM* jvm);
    JNIAudioRecorder(JavaVM* jvm, int samplerate, int channel);
    ~JNIAudioRecorder();

public:
    /**
     * 因为在不同的线程不能公用env, 所以提供两个接口 如果需要在不同线程调用这些接口
     * 要使用下面带env的接口
     */
    bool prepareRecording();
    bool startRecording();
    bool stopRecording();
    bool pauseRecording(bool pause);
    bool releaseRecording();

    bool prepareRecording(JNIEnv* env);
    bool startRecording(JNIEnv* env);
    bool stopRecording(JNIEnv* env);
    bool pauseRecording(JNIEnv* env, bool pause);
    bool releaseRecording(JNIEnv* env);

private:
    bool init(int samplerate, int channel);
    bool initJni(int jsamplerate, int jchannel);

private:
    JavaVM* m_pJVM;
    jclass m_pClass;
    jobject m_joInstance;
    JNIEnv* m_pEnv;

    bool m_bIsAttachEnv;
    jmethodID m_jmConstructor;
    jmethodID m_jmPrepareRecording;
    jmethodID m_jmStartRecording;
    jmethodID m_jmStopRecording;
    jmethodID m_jmPauseRecording;
    jmethodID m_jmReleaseRecording;

    JniHelper* pJniHelper;
};


#endif //ANDROIDTEST_JNIAUDIORECORDER_H
