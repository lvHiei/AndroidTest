//
// Created by mj on 17-12-14.
//

#ifndef ANDROIDTEST_JNIHELPER_H
#define ANDROIDTEST_JNIHELPER_H


#include <jni.h>

class JniHelper {
public:
    static JniHelper* getInstance();
    static JavaVM* ms_pJVM;

public:
    jclass FindClass(JNIEnv *env, const char *classname);
    jmethodID GetMethondID(JNIEnv *env, jclass clz, const char *method, const char *signature);
    jobject NewObject(JNIEnv *env, jclass clz, jmethodID methodID, ...);
    jobject NewGlobalRef(JNIEnv *env, jobject obj);
    void DeleteGlobalRef(JNIEnv *env, jobject obj);

private:
    JniHelper();
    ~JniHelper();

private:
    static JniHelper* ms_instance;
};


#endif //ANDROIDTEST_JNIHELPER_H
