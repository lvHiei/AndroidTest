#include <jni.h>
#include <string>

#include "audioTest/CAACHe2Lc.h"
#include "audioTest/AudioStreamDecoder.h"
#include "audioTest/AacReader.h"

extern "C"
jstring
Java_com_lvhiei_androidtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}



extern "C"
void
Java_com_lvhiei_androidtest_JniTools_nativeAacHE2LC(
        JNIEnv *env,
        jclass clazz,
        jstring jhePath,
        jstring jlcPath) {

    const char* hePath = env->GetStringUTFChars(jhePath, NULL);
    const char* lcPath = env->GetStringUTFChars(jlcPath, NULL);

    CAACHe2Lc* pAAC = new CAACHe2Lc();

    pAAC->doConvert(hePath, lcPath);

    delete pAAC;

    env->ReleaseStringUTFChars(jhePath, hePath);
    env->ReleaseStringUTFChars(jlcPath, lcPath);
}


extern "C"
void
Java_com_lvhiei_androidtest_JniTools_nativeAudioSoftDecoder(
        JNIEnv *env,
        jclass clazz,
        jstring jaacPath,
        jint threadCount) {

    const char* aacPath = env->GetStringUTFChars(jaacPath, NULL);

    CAudioStreamDecode* pAudioDeocder = new CAudioStreamDecode();
    pAudioDeocder->setPath(aacPath);
    pAudioDeocder->setThreadCount(threadCount);
    pAudioDeocder->start();
    delete pAudioDeocder;

    env->ReleaseStringUTFChars(jaacPath, aacPath);
}



extern "C"
jlong
Java_com_lvhiei_androidtest_JniTools_nativeOpenAudioFile(
        JNIEnv *env,
        jclass clazz,
        jstring jaacPath) {

    jlong jresult = 0 ;

    const char* aacPath = env->GetStringUTFChars(jaacPath, NULL);
    AacReader* pReader = new AacReader();
    pReader->open_file(aacPath);

    env->ReleaseStringUTFChars(jaacPath, aacPath);
    *(AacReader **)&jresult = pReader;
    return jresult;

}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeReadAAudioPacket(
        JNIEnv *env,
        jclass clazz,
        jlong thzz,
        jobject jbuffer) {

    uint8_t* data = (uint8_t *) env->GetDirectBufferAddress(jbuffer);
    int length = env->GetDirectBufferCapacity(jbuffer);
    AacReader *pReader = (AacReader *) 0 ;
    pReader = *(AacReader **)&thzz;

    if(!pReader){
        return 0;
    }
    return pReader->read_pacekt(data, length);
}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeCloseAudioFile(
        JNIEnv *env,
        jclass clazz,
        jlong thzz) {

    AacReader *pReader = (AacReader *) 0 ;
    pReader = *(AacReader **)&thzz;

    if(!pReader){
        return 0;
    }

    pReader->close_file();
    delete pReader;
}

extern "C"
void
Java_com_lvhiei_androidtest_Tools_MemUtil_nativeMemCopy(
        JNIEnv *env,
        jclass clazz,
        jobject jdstbuffer,
        jint dstOffset,
        jobject jsrcbuffer,
        jint srcOffset,
        jint length
) {
    unsigned char *dstBuffer = (unsigned char *) env->GetDirectBufferAddress(jdstbuffer);
    if (NULL == dstBuffer) {
        return;
    }

    unsigned char *srcBuffer = (unsigned char *) env->GetDirectBufferAddress(jsrcbuffer);
    if (NULL == srcBuffer) {
        return;
    }

    memcpy(dstBuffer + dstOffset, srcBuffer + srcOffset, length);
}