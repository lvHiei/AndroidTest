//
// Created by mj on 17-12-14.
//

#include <stdint.h>
#include "JNIAudioRecorder.h"
#include "../util/logUtil.h"
#include "JniHelper.h"


#define JAUDIO_RECODER_CLASS_PATH       "com/lvhiei/androidtest/Tools/AudioRecorder"

// copy from AudioFormat
#define CHANNEL_IN_MONO                 0x10
#define CHANNEL_IN_LEFT                 0x04
#define CHANNEL_IN_RIGHT                0x08
#define CHANNEL_IN_STEREO               (CHANNEL_IN_LEFT | CHANNEL_IN_RIGHT)

// default samplerate and channel
#define DEFAULT_SAMPLE_RATE             44100
#define DEFAULT_CHANNLE                 1


JNIAudioRecorder::JNIAudioRecorder(JavaVM *jvm):
    m_pJVM(jvm),
    m_pEnv(NULL),
    m_bIsAttachEnv(false)
{
    init(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNLE);
}

JNIAudioRecorder::JNIAudioRecorder(JavaVM *jvm, int samplerate, int channel):
    m_pJVM(jvm),
    m_pEnv(NULL),
    m_bIsAttachEnv(false)
{
    init(samplerate, channel);
}

JNIAudioRecorder::~JNIAudioRecorder()
{
    if(m_joInstance){
        pJniHelper->DeleteGlobalRef(m_pEnv, m_joInstance);
    }

    if(m_bIsAttachEnv){
        m_pJVM->DetachCurrentThread();
    }
}

bool JNIAudioRecorder::init(int samplerate, int channel)
{
    pJniHelper = JniHelper::getInstance();

    if(m_pJVM->GetEnv((void **) &m_pEnv, JNI_VERSION_1_4) != JNI_OK){
        if (m_pJVM->AttachCurrentThread(&m_pEnv, NULL) < 0) {
            LOGE("Attach current thread to JVM error!");
        }
        m_bIsAttachEnv = true;
    }

    jint jsamplerate = samplerate;
    jint jchannel = channel == 2 ? CHANNEL_IN_STEREO : CHANNEL_IN_MONO; // default mono


    return initJni(jsamplerate, jchannel);
}

bool JNIAudioRecorder::initJni(int jsamplerate, int jchannel)
{
    m_pClass = pJniHelper->FindClass(m_pEnv, JAUDIO_RECODER_CLASS_PATH);
    if(!m_pClass){
        LOGE("JNIAudioRecorder::initJni findclass failed, class:%s", JAUDIO_RECODER_CLASS_PATH);
        return false;
    }


    m_jmConstructor = pJniHelper->GetMethondID(m_pEnv, m_pClass, "<init>", "(II)V");
    if(!m_jmConstructor){
        LOGE("JNIAudioRecorder::initJni get constructor failed, class:%s", JAUDIO_RECODER_CLASS_PATH);
        return false;
    }

    jobject obj = pJniHelper->NewObject(m_pEnv, m_pClass, m_jmConstructor, jsamplerate, jchannel);
    if(!obj){
        LOGE("JNIAudioRecorder::initJni new instance failed, class:%s", JAUDIO_RECODER_CLASS_PATH);
        return false;
    }

    m_joInstance = pJniHelper->NewGlobalRef(m_pEnv, obj);

    m_jmPrepareRecording = pJniHelper->GetMethondID(m_pEnv, m_pClass, "prepare", "()Z");
    m_jmStartRecording = pJniHelper->GetMethondID(m_pEnv, m_pClass, "start", "()Z");
    m_jmStopRecording = pJniHelper->GetMethondID(m_pEnv, m_pClass, "stop", "()Z");
    m_jmPauseRecording = pJniHelper->GetMethondID(m_pEnv, m_pClass, "pause", "(Z)Z");
    m_jmReleaseRecording = pJniHelper->GetMethondID(m_pEnv, m_pClass, "release", "()Z");

    LOGI("JNIAudioRecorder::initJni methodId: constructor:%p,prepare:%p,start:%p,stop:%p,pause:%p,release:%p",
        m_jmConstructor, m_jmPrepareRecording, m_jmStartRecording, m_jmStopRecording, m_jmPauseRecording, m_jmReleaseRecording);

    CHECK_JNI_EXCEPTION(m_pEnv, __func__);

    return true;
}

bool JNIAudioRecorder::prepareRecording()
{
    return prepareRecording(m_pEnv);
}

bool JNIAudioRecorder::startRecording()
{
    return startRecording(m_pEnv);
}

bool JNIAudioRecorder::stopRecording()
{
    return stopRecording(m_pEnv);
}

bool JNIAudioRecorder::pauseRecording(bool pause)
{
    return pauseRecording(m_pEnv, pause);
}

bool JNIAudioRecorder::releaseRecording()
{
    return releaseRecording(m_pEnv);
}


bool JNIAudioRecorder::prepareRecording(JNIEnv *env)
{
    return env->CallBooleanMethod(m_joInstance, m_jmPrepareRecording);
}

bool JNIAudioRecorder::startRecording(JNIEnv *env) {
    return env->CallBooleanMethod(m_joInstance, m_jmStartRecording);
}

bool JNIAudioRecorder::stopRecording(JNIEnv *env) {
    return env->CallBooleanMethod(m_joInstance, m_jmStopRecording);
}

bool JNIAudioRecorder::pauseRecording(JNIEnv *env, bool pause) {
    return env->CallBooleanMethod(m_joInstance, m_jmPauseRecording, pause);
}

bool JNIAudioRecorder::releaseRecording(JNIEnv *env) {
    return env->CallBooleanMethod(m_joInstance, m_jmReleaseRecording);
}

