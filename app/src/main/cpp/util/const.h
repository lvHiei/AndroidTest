/*
 * const.h
 *
 *  Created on: 2015年11月12日
 *      Author: mj
 */

#ifndef OFFSCREEN_CONST_H_
#define OFFSCREEN_CONST_H_

#define POP_TIMEOUT					100
#define FLOAT_PRECISION             0.00001

#define TIME_OVER_FLOW              1000000000

#define VV_MAX_MICCOUNT             4
#define VV_PCM_BUFFER_LEN           16384
#define VV_AAC_BUFFER_LEN           1000
#define VV_ONE_PCM_BUFFER_LEN       1024
#define VV_YUV_BUFFER_LEN           (640*480*3)
#define VV_H264_BUFFER_LEN          (640*480*3)

// 滤镜相关

// 错误码
enum JNI_MSG_TYPE{
    // 视频相关
    MSG_CHANGE_BITRATE = 0,

    // 音频相关
    MSG_RECORD_INIT_ERROR = 100,

    // 音频播放相关
    AUDIO_PLAYER_ONERROR = 200,
    AUDIO_PLAYER_ONSTART = 201,
    AUDIO_PLAYER_ONPASUE = 202,
    AUDIO_PLAYER_ONSTOP = 203,
    AUDIO_PLAYER_ONREFRESH = 204,
    AUDIO_PLAYER_ONPREPARE = 205,

    //音效相关
    AUDIO_EFFECT_PROCESS_SLOW = 400,
};



enum {
    LVL_VERBOSE,
    LVL_DEBUG,
    LVL_INFO,
    LVL_WARN,
    LVL_ERROR,
    LVL_FATAL
};

enum {
    YUV_FMT_YUV420P,
    YUV_FMT_YV12,
    YUV_FMT_NV12,
    YUV_FMT_NV21,
};

#endif /* OFFSCREEN_CONST_H_ */
