//
// Created by mj on 17-8-9.
//

#include "AudioStreamDecoder.h"
#include "../util/logUtil.h"
#include "../util/TimeUtil.h"


static void *audioDecodeThreadFun(void *context) {
    CAudioStreamDecode *thiz = (CAudioStreamDecode *) context;
    thiz->run();
    return NULL;
}

CAudioStreamDecode::CAudioStreamDecode()
    : m_bIsStarted(false),
      m_bWantStop(false)
{
    for(int i = 0; i < VV_MAX_MICCOUNT; ++i){
        m_pAudioFrameDecode[i] = new CAudioFrameDecode();
        m_pSampleRate[i] = 44100;
        m_pChannels[i] = 2;
        m_pWantReset[i] = false;
        m_pDisableAudio[i] = true;
    }

    m_nThreadCount = 1;
}

CAudioStreamDecode::~CAudioStreamDecode()
{
    release();
}

bool CAudioStreamDecode::Init(int micIndex, int liSampleRate, int liChannels)
{
    if(micIndex < VV_MAX_MICCOUNT){
        m_pSampleRate[micIndex] = liSampleRate;
        m_pChannels[micIndex] = liChannels;
        return true;
    }

    return false;
}

void CAudioStreamDecode::Reset(int micIndex, int liSampleRate, int liChannels)
{
    if(micIndex < VV_MAX_MICCOUNT){
        m_pWantReset[micIndex] = (liSampleRate != m_pSampleRate[micIndex]) || (liChannels != m_pChannels[micIndex]);
        m_pSampleRate[micIndex] = liSampleRate;
        m_pChannels[micIndex] = liChannels;
    }
}


void CAudioStreamDecode::start()
{
//    if(m_bIsStarted){
//        return;
//    }
//
//    m_bWantStop = false;
//    int res = pthread_create(&m_ThreadId, NULL, audioDecodeThreadFun, this);
//    if (0 != res) {
//        LOGE("CAudioStreamDecode pthread_create error.");
//    } else {
//        m_bIsStarted = true;
//    }
    run();
}


void CAudioStreamDecode::enableAudio(int micIndex)
{
    m_pDisableAudio[micIndex] = false;
}

void CAudioStreamDecode::disableAudio(int micIndex)
{
    m_pDisableAudio[micIndex] = true;
//    handleDisableAudio();
}


void CAudioStreamDecode::setPath(const char *aacPath)
{
    memset(m_pAACPath, 0, 1024);
    strcpy(m_pAACPath, aacPath);
}


void CAudioStreamDecode::setThreadCount(int threadcount)
{
    m_nThreadCount = threadcount;
}


void CAudioStreamDecode::handleDisableAudio()
{
    bool needStop = true;
    for(int i=0; i < VV_MAX_MICCOUNT; ++i){
        if(!m_pDisableAudio[i]){
            needStop = false;
            break;
        }
    }

    if(needStop){
        stop();
    }
}



bool CAudioStreamDecode::isStarted() {
    return m_bIsStarted;
}


void CAudioStreamDecode::stop()
{
    if(!m_bIsStarted){
        return;
    }

    m_bWantStop = true;
    pthread_join(m_ThreadId, NULL);
    m_bIsStarted = false;
}

void CAudioStreamDecode::run()
{
    m_pFormat = new VVAVFormat();
    m_pFormatCtx = m_pFormat->alloc_foramt_context();

    m_pFormat->open_input_file(&m_pFormatCtx, m_pAACPath);
    AVPacket packet;
    av_init_packet(&packet);

//    FILE* pFILE[VV_MAX_MICCOUNT];
//    char buf[128] = {0};
//    for(int i = 0; i < VV_MAX_MICCOUNT; ++i){
//        m_pAudioFrameDecode[i]->Init(m_pSampleRate[i], m_pChannels[i]);
//        sprintf(buf, "/sdcard/android_test/%d.pcm", i);
//        pFILE[i] = fopen(buf, "w");
//    }

    char pPCMData[VV_PCM_BUFFER_LEN] = {0};
    char pAACData[VV_AAC_BUFFER_LEN] = {0};

    long timeOut = 100;
    int64_t pts;
    int aacLen;
    int pcmLen;
    int ret;
    int count = 0;
    int npakcets = 0;
    uint32_t lastLogTime = TimeUtil::GetTickCount();
    while(!m_bWantStop)
    {
        // first check reset samplerate and channel
        for(int i = 0; i < VV_MAX_MICCOUNT; ++i){
            if(m_pWantReset[i]){
                m_pWantReset[i] = false;
                m_pAudioFrameDecode[i]->Reset(m_pSampleRate[i], m_pChannels[i]);
            }
        }

        // second get the latest aac pkt data
        int index = count % (VV_MAX_MICCOUNT / m_nThreadCount);
        ++count;

        memset(pPCMData, 0, VV_PCM_BUFFER_LEN);
        memset(pAACData, 0, VV_AAC_BUFFER_LEN);

//        aacLen = QueueManager::getInstance()->getAudioDecodeQueue(index)->trypop(pAACData, VV_AAC_BUFFER_LEN, timeOut, pts);
//        if(aacLen <= 0){
//            continue;
//        }

        if(index == 0){
            av_packet_unref(&packet);
            ret = m_pFormat->read_packet(m_pFormatCtx, &packet);
            if(ret < 0){
                break;
            }else{
                ++npakcets;
            }
        }

        // third decode aac data
        pcmLen = VV_PCM_BUFFER_LEN;
        ret = m_pAudioFrameDecode[index]->Decode((char *) packet.data, packet.size, pPCMData, pcmLen);
        uint32_t now = TimeUtil::GetTickCount();
        if(now - lastLogTime > 1000){
            LOGI("CAudioStreamDecode index:%d,ret:%d,aacLen:%d,pcmLen:%d,pts:%lld,packets:%d", index, ret, aacLen, pcmLen, pts, npakcets);
            lastLogTime = now;
        }
        // fourth put pcm data
        if(ret){
//            fwrite(pPCMData, 1, pcmLen, pFILE[index]);
//            putPCMData(index, pPCMData, pcmLen, pts);
        }
    }

    LOGI("CAudioStreamDecode ended, packets:%d", npakcets);
//    for(int i = 0; i < VV_MAX_MICCOUNT; ++i){
//        fclose(pFILE[i]);
//    }

    release();
}

void CAudioStreamDecode::release()
{
    for(int i = 0; i < VV_MAX_MICCOUNT; ++i)
    {
//        QueueManager::getInstance()->getAudioDecodeQueue(i)->clear();

        m_pSampleRate[i] = 0;
        m_pChannels[i] = 0;
        m_pWantReset[i] = false;
        m_pDisableAudio[i] = true;

        if(m_pAudioFrameDecode[i]){
            delete m_pAudioFrameDecode[i];
            m_pAudioFrameDecode[i] = NULL;
        }
    }
}

void CAudioStreamDecode::putPCMData(int micIndex, char *data, int32_t length, int64_t pts)
{
    int32_t left = length;
    char* buffer = data;
    while (left > 0)
    {
        if(left >= VV_ONE_PCM_BUFFER_LEN){
//            QueueManager::getInstance()->getAudioPCMQueue(micIndex)->push(buffer, VV_ONE_PCM_BUFFER_LEN, pts);
            buffer = buffer + VV_ONE_PCM_BUFFER_LEN;
            left = left - VV_ONE_PCM_BUFFER_LEN;
        }else{
//            QueueManager::getInstance()->getAudioPCMQueue(micIndex)->push(buffer, left, pts);
            buffer = buffer + left;
            left -= left;
        }
    }
}


