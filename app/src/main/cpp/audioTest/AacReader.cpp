//
// Created by mj on 17-9-15.
//

#include "AacReader.h"
#include "../util/logUtil.h"


AacReader::AacReader()
{
    m_pFormat = new VVAVFormat();
    m_pFormatContext = m_pFormat->alloc_foramt_context();
}

AacReader::~AacReader()
{
    m_pFormat->free_format_context(m_pFormatContext);
    delete m_pFormat;
}

int AacReader::open_file(const char *aacfile)
{
    m_pFormat->open_input_file(&m_pFormatContext, aacfile);

    return 0;
}

int AacReader::read_pacekt(uint8_t *data, int length)
{
    AVPacket packet;
    av_init_packet(&packet);

    int ret = m_pFormat->read_packet(m_pFormatContext, &packet);
    if(ret >= 0){
        if(length < packet.size){
            LOGE("AacReader::read_pacekt length not enough");
            return 0;
        }

        memcpy(data, packet.data, packet.size);
        return packet.size;
    }

    return ret;
}

int AacReader::close_file()
{
    m_pFormat->close_input_file(&m_pFormatContext);
    return 0;
}


