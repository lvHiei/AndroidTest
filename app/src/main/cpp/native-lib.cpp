#include <jni.h>
#include <string>

#include "audioTest/CAACHe2Lc.h"

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
