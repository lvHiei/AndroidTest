//
// Created by mj on 17-12-14.
//

#include <stdint.h>
#include <jni.h>
#include "JniHelper.h"
#include "../util/logUtil.h"

JavaVM* JniHelper::ms_pJVM = NULL;
JniHelper* JniHelper::ms_instance = NULL;


JniHelper *JniHelper::getInstance()
{
    if(!ms_instance){
        ms_instance = new JniHelper();
    }

    return ms_instance;
}

jclass JniHelper::FindClass(JNIEnv *env, const char *classname)
{
    return env->FindClass(classname);
}

jmethodID
JniHelper::GetMethondID(JNIEnv *env, jclass clz, const char *method, const char *signature)
{
    return env->GetMethodID(clz, method, signature);
}

jobject JniHelper::NewObject(JNIEnv *env, jclass clz, jmethodID methodID, ...)
{
    va_list args;
    va_start(args, methodID);
    jobject obj = env->NewObjectV(clz, methodID, args);
    va_end(args);

    CHECK_JNI_EXCEPTION(env, __func__);

    return obj;
}

jobject JniHelper::NewGlobalRef(JNIEnv *env, jobject obj) {
    return env->NewGlobalRef(obj);
}

void JniHelper::DeleteGlobalRef(JNIEnv *env, jobject obj) {
    env->DeleteLocalRef(obj);
}

JniHelper::JniHelper()
{

}

JniHelper::~JniHelper()
{

}
