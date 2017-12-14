#include <jni.h>
#include <string>

#include "audioTest/CAACHe2Lc.h"
#include "audioTest/AudioStreamDecoder.h"
#include "audioTest/AacReader.h"
#include "util/logUtil.h"
#include "util/TimeUtil.h"
#include "MediaTest/MediaTest.h"
#include "jniObject/JniHelper.h"

JavaVM* sp_jvm = NULL;

static pthread_key_t g_thread_key;
static pthread_once_t g_key_once = PTHREAD_ONCE_INIT;

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    sp_jvm = vm;
    JniHelper::ms_pJVM = vm;
    JniHelper::getInstance();
    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_4);

    return JNI_VERSION_1_4;
}

extern "C"
jstring
Java_com_lvhiei_androidtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

static void make_thread_key()
{
    pthread_key_create(&g_thread_key, NULL);
}

static void thread_test(){
    JNIEnv* env;
    if (sp_jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("Attach thread, getenv is NULL");
    }else{
        LOGE("JNIEnvTest thread_test: env:%p", env);
    }
}


jint SDL_JNI_SetupThreadEnv(JNIEnv **p_env)
{
    JavaVM *jvm = sp_jvm;
    if (!jvm) {
        LOGE("SDL_JNI_GetJvm: AttachCurrentThread: NULL jvm");
        return -1;
    }

    pthread_once(&g_key_once, make_thread_key);

    JNIEnv *env = (JNIEnv*) pthread_getspecific(g_thread_key);
    if (env) {
        *p_env = env;
        LOGI("env pthread_getspecific");
        return 0;
    }

    if (jvm->AttachCurrentThread(&env, NULL) == JNI_OK) {
        pthread_setspecific(g_thread_key, env);
        *p_env = env;
        return 0;
    }

    return -1;
}


static void* thread1(void *data){

    pthread_once(&g_key_once, make_thread_key);
    JNIEnv *env;
//    bool isAttachedENV = false;
//    if (sp_jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
//        if (sp_jvm->AttachCurrentThread(&env, NULL) < 0) {
//            LOGE("Attach current thread to JVM error!");
//        }
//        isAttachedENV = true;
//    }

    SDL_JNI_SetupThreadEnv(&env);

    LOGI("thread1 env is %p,key:%d", env, g_thread_key);

    TimeUtil::sleep(20);

    SDL_JNI_SetupThreadEnv(&env);

    LOGI("thread1 env is %p,key:%d", env, g_thread_key);
//    if (isAttachedENV) {
        sp_jvm->DetachCurrentThread();
//    }

    return NULL;
}

static void* thread2(void *data){
    pthread_once(&g_key_once, make_thread_key);
    JNIEnv *env;
//    bool isAttachedENV = false;
//    TimeUtil::sleep(10);
//    if (sp_jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
//        if (sp_jvm->AttachCurrentThread(&env, NULL) < 0) {
//            LOGE("Attach current thread to JVM error!");
//        }
//        isAttachedENV = true;
//    }

    TimeUtil::sleep(10);

    SDL_JNI_SetupThreadEnv(&env);

    LOGI("thread2 env is %p,key:%d", env, g_thread_key);

    TimeUtil::sleep(20);

    SDL_JNI_SetupThreadEnv(&env);

    LOGI("thread2 env is %p,key:%d", env, g_thread_key);

//    if (isAttachedENV) {
        sp_jvm->DetachCurrentThread();
//    }

    return NULL;
}

static void* thread3(void *data){
    pthread_once(&g_key_once, make_thread_key);
    JNIEnv *env;
    bool isAttachedENV = false;

    if (sp_jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        if (sp_jvm->AttachCurrentThread(&env, NULL) < 0) {
            LOGE("Attach current thread to JVM error!");
        }
        isAttachedENV = true;
    }

    LOGE("JNIEnvTest thread3: env:%p", env);

    thread_test();

    sp_jvm->DetachCurrentThread();

    return NULL;
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


extern "C"
jlong
Java_com_lvhiei_androidtest_JniTools_nativeOpenMediaFile(
        JNIEnv *env,
        jclass clazz,
        jstring jmediaPath) {

    jlong jresult = 0 ;

    const char* mediaPath = env->GetStringUTFChars(jmediaPath, NULL);
    MediaTest* pReader = new MediaTest();
    pReader->open_file(mediaPath);

    env->ReleaseStringUTFChars(jmediaPath, mediaPath);
    *(MediaTest **)&jresult = pReader;
    return jresult;

}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeReadMediaPacket(
        JNIEnv *env,
        jclass clazz,
        jlong thzz,
        jobject jbuffer) {

    uint8_t* data = (uint8_t *) env->GetDirectBufferAddress(jbuffer);
    int length = env->GetDirectBufferCapacity(jbuffer);
    MediaTest *pReader = (MediaTest *) 0 ;
    pReader = *(MediaTest **)&thzz;

    if(!pReader){
        return 0;
    }
    return pReader->read_pacekt(data, length);
}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeCloseMediaFile(
        JNIEnv *env,
        jclass clazz,
        jlong thzz) {

    MediaTest *pReader = (MediaTest *) 0 ;
    pReader = *(MediaTest **)&thzz;

    if(!pReader){
        return 0;
    }

    int ret = pReader->close_file();
    delete pReader;
    return ret;
}

extern "C"
jlong
Java_com_lvhiei_androidtest_JniTools_nativeGetMediaTimestamp(
        JNIEnv *env,
        jclass clazz,
        jlong thzz) {

    MediaTest *pReader = (MediaTest *) 0 ;
    pReader = *(MediaTest **)&thzz;

    if(!pReader){
        return 0;
    }

    return pReader->getTimestamp();
}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeGetMediaType(
        JNIEnv *env,
        jclass clazz,
        jlong thzz) {

    MediaTest *pReader = (MediaTest *) 0 ;
    pReader = *(MediaTest **)&thzz;

    if(!pReader){
        return 0;
    }

    return pReader->getType();
}

extern "C"
jint
Java_com_lvhiei_androidtest_JniTools_nativeJniEnvTest(
        JNIEnv *env,
        jclass clazz) {

//    pthread_t t1;
//    pthread_t t2;
//
//    pthread_create(&t1, NULL, thread1, NULL);
//    pthread_create(&t2, NULL, thread2, NULL);
//
//    pthread_join(t1, NULL);
//    pthread_join(t2, NULL);

    pthread_t t3;
    pthread_create(&t3, NULL, thread3, NULL);
    pthread_join(t3, NULL);
    return 0;
}
