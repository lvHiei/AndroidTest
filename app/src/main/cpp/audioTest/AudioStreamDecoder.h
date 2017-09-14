//
// Created by mj on 17-8-9.
//

#ifndef VVMUSIC_ANDROID_APP_AUDIOSTREAMDECODER_H
#define VVMUSIC_ANDROID_APP_AUDIOSTREAMDECODER_H

#include <pthread.h>
#include "AudioFrameDecoder.h"
#include "../util/const.h"
#include "../format/VVAVFormat.h"

static void * audioDecodeThreadFun(void *context);

//音频解码器，输入一个avpacket包，解码出包含的PCM数据
class CAudioStreamDecode
{
    friend void * audioDecodeThreadFun(void *context);
public:
    CAudioStreamDecode();
    ~CAudioStreamDecode();
public:
    bool Init(int micIndex, int liSampleRate, int liChannels);
    void Reset(int micIndex, int liSampleRate, int liChannels);
    void enableAudio(int micIndex);
    void disableAudio(int micIndex);

    void setPath(const char* aacPath);
    void setThreadCount(int threadcount);
public:
    void start();
    void stop();
    bool isStarted();

private:
    void run();
    void release();
    void putPCMData(int micIndex, char* data, int32_t length, int64_t pts);
    void handleDisableAudio();

private:
    //解压一帧的解码器
    CAudioFrameDecode* m_pAudioFrameDecode[VV_MAX_MICCOUNT];
    int m_pSampleRate[VV_MAX_MICCOUNT];
    int m_pChannels[VV_MAX_MICCOUNT];
    bool m_pWantReset[VV_MAX_MICCOUNT];
    bool m_pDisableAudio[VV_MAX_MICCOUNT];

private:
    pthread_t m_ThreadId;
    bool m_bWantStop;
    bool m_bIsStarted;

private:
    VVAVFormat* m_pFormat;
    AVFormatContext* m_pFormatCtx;
    char m_pAACPath[1024];

private:
    int m_nThreadCount;
};


#endif //VVMUSIC_ANDROID_APP_AUDIOSTREAMDECODER_H
