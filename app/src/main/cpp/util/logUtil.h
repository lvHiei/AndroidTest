//
// Created by mj on 16-2-19.
//

#ifndef MVBOX_NEW_LOGUTIL_H
#define MVBOX_NEW_LOGUTIL_H

#include <android/log.h>

namespace vvplayer {

#define TAG "NativeRenderer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)


//#define LOGD(...)
//#define LOGI(...)
//#define LOGW(...)
//#define LOGE(...)
//#define LOGF(...)
}



#endif //MVBOX_NEW_LOGUTIL_H
