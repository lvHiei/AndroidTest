//
// Created by mj on 17-8-9.
//

#include "AudioFrameDecoder.h"

#include "../util/logUtil.h"
#include "put_bits.h"


CAudioFrameDecode::CAudioFrameDecode()
{
    m_codec_id = AV_CODEC_ID_AAC;
    m_pCodecCtx = NULL;
    m_pFrame = NULL;
    m_swr_ctx = NULL;
    m_outbuf = NULL;
    m_outbuflen = 0;
}
CAudioFrameDecode::~CAudioFrameDecode()
{
    if(m_pFrame){
        av_frame_free(&m_pFrame);
    }
    avcodec_close(m_pCodecCtx);
    if(m_pCodecCtx){
        av_free(m_pCodecCtx);
    }
    if(m_swr_ctx)
    {
        swr_free(&m_swr_ctx);
    }
    if(m_outbuf)
    {
        free(m_outbuf);
    }
}


int CAudioFrameDecode::TransferSampleRate(int samplerate)
{
    int defSampleRate = 6; //default 24000hz
    switch(samplerate)
    {
        case 96000:
            defSampleRate = 0;
            break;
        case 88200:
            defSampleRate = 1;
            break;
        case 64000:
            defSampleRate = 2;
            break;
        case 48000:
            defSampleRate = 3;
            break;
        case 44100:
            defSampleRate = 4;
            break;
        case 32000:
            defSampleRate = 5;
            break;
        case 24000:
            defSampleRate = 6;
            break;
        case 22050:
            defSampleRate = 7;
            break;
        case 16000:
            defSampleRate = 8;
            break;
        case 12000:
            defSampleRate = 9;
            break;
        case 11025:
            defSampleRate = 10;
            break;
        case 8000:
            defSampleRate = 11;
            break;
        case 7350:
            defSampleRate = 12;
            break;
        default:
            defSampleRate = 6;
            break;
    }
    return defSampleRate;
}
bool CAudioFrameDecode::Init(int liSampleRate, int liChannels)
{
    avcodec_register_all();
    m_pCodec = avcodec_find_decoder(m_codec_id);
    if (!m_pCodec) {
        LOGW("CAudioFrameDecode::Init Fail, Codec not found");
        return false;
    }
    m_pCodecCtx = avcodec_alloc_context3(m_pCodec);
    if (!m_pCodecCtx){
        LOGW("CAudioFrameDecode::Init Fail, Could not allocate video codec context");
        return false;
    }
    if(avcodec_open2(m_pCodecCtx, m_pCodec, NULL) < 0) {
        LOGW("CAudioFrameDecode::Init Fail, Could not open codec");
        return false;
    }
    m_pFrame = av_frame_alloc();
    if(!m_pFrame){
        LOGW("CAudioFrameDecode::Init Fail, Malloc Frame Fail");
        return false;
    }
    m_swr_ctx = swr_alloc();
    if(!m_swr_ctx)
    {
        LOGW("CAudioFrameDecode::Init Fail, Could not alloc SwrContext");
        return false;
    }
    /* set options */
    av_opt_set_int(m_swr_ctx, "in_channel_layout",    av_get_default_channel_layout(liChannels), 0);
    av_opt_set_int(m_swr_ctx, "in_sample_rate",       liSampleRate, 0);
    av_opt_set_sample_fmt(m_swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_FLTP, 0);
    av_opt_set_int(m_swr_ctx, "out_channel_layout",    av_get_default_channel_layout(liChannels), 0);
    av_opt_set_int(m_swr_ctx, "out_sample_rate",       liSampleRate, 0);
    av_opt_set_sample_fmt(m_swr_ctx, "out_sample_fmt", AV_SAMPLE_FMT_S16, 0);// AV_SAMPLE_FMT_S16

    if(swr_init(m_swr_ctx) < 0){
        LOGW("Failed to initialize the resampling context");
        if(m_swr_ctx){
            swr_free(&m_swr_ctx);
            m_swr_ctx = NULL;
        }
        return false;
    }
    //
    av_init_packet(&m_packet);
    m_nSampleRate = TransferSampleRate(liSampleRate/2);
    m_nChannels = liChannels;
    return true;
}
void CAudioFrameDecode::Reset(int liSampleRate, int liChannels)
{
    m_nSampleRate = TransferSampleRate(liSampleRate/2);
    m_nChannels = liChannels;
    if(m_swr_ctx)
    {
        swr_free(&m_swr_ctx);
    }
    m_swr_ctx = swr_alloc();
    if(!m_swr_ctx)
    {
        LOGW("CAudioFrameDecode::Init Fail, Could not alloc SwrContext");
        return;
    }
    /* set options */
    av_opt_set_int(m_swr_ctx, "in_channel_layout",    av_get_default_channel_layout(liChannels), 0);
    av_opt_set_int(m_swr_ctx, "in_sample_rate",       liSampleRate, 0);
    av_opt_set_sample_fmt(m_swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_FLTP, 0);
    av_opt_set_int(m_swr_ctx, "out_channel_layout",    av_get_default_channel_layout(liChannels), 0);
    av_opt_set_int(m_swr_ctx, "out_sample_rate",       liSampleRate, 0);
    av_opt_set_sample_fmt(m_swr_ctx, "out_sample_fmt", AV_SAMPLE_FMT_S16, 0);// AV_SAMPLE_FMT_S16
    if(swr_init(m_swr_ctx) < 0){
        LOGW("Failed to initialize the resampling context");
        if(m_swr_ctx){
            swr_free(&m_swr_ctx);
            m_swr_ctx = NULL;
        }
    }
}
bool CAudioFrameDecode::Decode(char* lpData, int liDataLen, char* lpDecodeData, int &liDecodeLen)
{
    if(!m_pCodecCtx || !m_pFrame)
    {
        return false;
    }
    if(liDataLen > MAX_AUDIO_PACKLEN){
        return false;
    }
//    uint8_t packetBuf[MAX_AUDIO_PACKLEN + ADTS_HEADER_SIZE];
//    int  packetLen = MAX_AUDIO_PACKLEN + ADTS_HEADER_SIZE;
//    //写入ADTS头
//    ff_adts_write_frame_header((uint8_t*)packetBuf, liDataLen, 0, m_nChannels ,0, m_nSampleRate);
//    //将AAC数据拷贝到ADTS头之后
//    memcpy(packetBuf+ADTS_HEADER_SIZE, lpData, liDataLen);
//    packetLen = liDataLen+ADTS_HEADER_SIZE;
//    m_packet.data = packetBuf;
//    m_packet.size = packetLen;

    m_packet.data = (uint8_t *) lpData;
    m_packet.size = liDataLen;
    int len, got_frame;
    //char *outbuf;
    //int  outbuflen = 16384+4096;
    //outbuf = (char*)malloc(outbuflen);
    len = avcodec_decode_audio4(m_pCodecCtx, m_pFrame, &got_frame, &m_packet);
    if(len<0){
        return false;
    }
    if(got_frame > 0)
    {
        //解码成功，转换双声道float型数据位单声道short行数据
        /*
        int in_samples = m_pFrame->nb_samples;
        float* inputChannel0 = (float*)m_pFrame->extended_data[0];
        float* inputChannel1 = (float*)m_pFrame->extended_data[1];
        for (int i=0 ; i<in_samples ; i++) {
            outbuf[i*2] = (int16_t) ((*inputChannel0++) * 32767.0f);
            outbuf[i*2+1] = (int16_t) ((*inputChannel1++) * 32767.0f);
        }
        int dstlen = in_samples*4;
        */
        int data_size = av_samples_get_buffer_size(NULL, m_nChannels,
                                                   m_pFrame->nb_samples,
                                                   AV_SAMPLE_FMT_S16, 1);
        if(m_outbuf == NULL)
        {
            m_outbuflen = data_size;
            m_outbuf = (char*)malloc(m_outbuflen);
        }
        else
        {
            if(m_outbuflen < data_size)
            {
                m_outbuflen = data_size;
                m_outbuf = (char*)realloc(m_outbuf, m_outbuflen);
            }
        }
        int len = swr_convert(m_swr_ctx,(uint8_t**)&m_outbuf,m_pFrame->nb_samples,(const uint8_t**)m_pFrame->extended_data, m_pFrame->nb_samples);
        if(len < 0){
            LOGW("error swr_convert");
            return false;
        }
        if(data_size >  0 && data_size <= liDecodeLen)
        {
            liDecodeLen = data_size;
            memcpy(lpDecodeData, (char*)m_outbuf, liDecodeLen);
            return true;
        }
        else
        {
            return false;
        }
    }
    else
    {
        return false;
    }
}
