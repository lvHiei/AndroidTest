//
// Created by mj on 17-10-31.
//

#include "MediaTest.h"
#include "../util/logUtil.h"


MediaTest::MediaTest()
{
    m_pFormat = new VVAVFormat();
    m_pFormatContext = m_pFormat->alloc_foramt_context();
}

MediaTest::~MediaTest()
{
    m_pFormat->free_format_context(m_pFormatContext);
    delete m_pFormat;
}

int MediaTest::open_file(const char *file)
{
    m_iTimestamp = -1;
    int ret = m_pFormat->open_input_file(&m_pFormatContext, file);

    m_iAStream = m_pFormat->find_audio_stream(m_pFormatContext);
    m_iVStream = m_pFormat->find_video_stream(m_pFormatContext);

    return ret;
}

int MediaTest::read_pacekt(uint8_t *data, int length)
{
    AVPacket packet;
    av_init_packet(&packet);

    int ret = m_pFormat->read_packet(m_pFormatContext, &packet);
    if(ret >= 0){
        if(length < packet.size){
            LOGE("AacReader::read_pacekt length not enough");
            return 0;
        }

        m_iTimestamp = packet.pts;
        m_iType = packet.stream_index == m_iVStream;

        memcpy(data, packet.data, packet.size);
        return packet.size;
    }

    return ret;
}

int MediaTest::close_file()
{
    m_iTimestamp = -1;
    return m_pFormat->close_input_file(&m_pFormatContext);
}

int64_t MediaTest::getTimestamp() {
    return m_iTimestamp;
}

int MediaTest::getType() {
    return m_iType;
}
