//
// Created by mj on 17-8-9.
//

#ifndef VVMUSIC_ANDROID_APP_AUDIOFRAMEDECODER_H
#define VVMUSIC_ANDROID_APP_AUDIOFRAMEDECODER_H


extern "C"
{
#include "../ffmpeg/libavcodec/avcodec.h"
#include "../ffmpeg/libavutil/frame.h"
#include "../ffmpeg/libavutil/opt.h"
#include "../ffmpeg/libavutil/avstring.h"
#include "../ffmpeg/libswresample/swresample.h"
}
#include <unistd.h>

#define MAX_AUDIO_PACKLEN  1000         //音频压缩数据最大长度
#define MAX_AUDIO_DRAMELEN 16384        //音频解压数据缓存长度
#define ADTS_HEADER_SIZE   7            //ADTS头长度




//音频解码器，解一帧数据
class CAudioFrameDecode
{
public:
    CAudioFrameDecode();
    ~CAudioFrameDecode();
    //初始化ffmpeg，设置采样率和通道号，以此来封装ADTS头
    bool Init(int liSampleRate, int liChannels);
    //解码函数
    bool Decode(char* lpData, int liDataLen, char* lpDecodeDataLen, int &liDecodeLen);
    //重新设置样率和通道号，以此来封装ADTS头
    void Reset(int liSampleRate, int liChannels);
private:
    //采样率转换为ADTS定义的值
    int       TransferSampleRate(int samplerate);
private:
    AVCodecID        m_codec_id;
    //采样率，要在房间配置信息的基础上除以2，在转换为ADTS定义的值
    int              m_nSampleRate;
    //通道号
    int              m_nChannels;

    AVCodec*         m_pCodec;
    AVCodecContext*  m_pCodecCtx;
    AVFrame*         m_pFrame;
    AVPacket         m_packet;
    //转换AV_SAMPLE_FMT_FLTP to AV_SAMPLE_FMT_S16
    SwrContext*      m_swr_ctx;
    //AV_SAMPLE_FMT_S16格式数据缓存
    char*            m_outbuf;
    int              m_outbuflen;
};


#endif //VVMUSIC_ANDROID_APP_AUDIOFRAMEDECODER_H
