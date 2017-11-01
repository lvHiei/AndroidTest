//
// Created by mj on 17-10-31.
//

#include "MediaTest.h"
#include "../util/logUtil.h"


/* Parse the SPS/PPS Metadata and convert it to annex b format */
static int convert_sps_pps( const uint8_t *p_buf, size_t i_buf_size,
                            uint8_t *p_out_buf, size_t i_out_buf_size,
                            size_t *p_sps_pps_size, size_t *p_nal_size)
{
    // int i_profile;
    uint32_t i_data_size = i_buf_size, i_nal_size, i_sps_pps_size = 0;
    unsigned int i_loop_end;

    /* */
    if( i_data_size < 7 )
    {
        LOGE( "Input Metadata too small" );
        return -1;
    }

    /* Read infos in first 6 bytes */
    // i_profile    = (p_buf[1] << 16) | (p_buf[2] << 8) | p_buf[3];
    if (p_nal_size)
        *p_nal_size  = (p_buf[4] & 0x03) + 1;
    p_buf       += 5;
    i_data_size -= 5;

    for ( unsigned int j = 0; j < 2; j++ )
    {
        /* First time is SPS, Second is PPS */
        if( i_data_size < 1 )
        {
            LOGE( "PPS too small after processing SPS/PPS %u",
                   i_data_size );
            return -1;
        }
        i_loop_end = p_buf[0] & (j == 0 ? 0x1f : 0xff);
        p_buf++; i_data_size--;

        for ( unsigned int i = 0; i < i_loop_end; i++)
        {
            if( i_data_size < 2 )
            {
                LOGE( "SPS is too small %u", i_data_size );
                return -1;
            }

            i_nal_size = (p_buf[0] << 8) | p_buf[1];
            p_buf += 2;
            i_data_size -= 2;

            if( i_data_size < i_nal_size )
            {
                LOGE( "SPS size does not match NAL specified size %u",
                       i_data_size );
                return -1;
            }
            if( i_sps_pps_size + 4 + i_nal_size > i_out_buf_size )
            {
                LOGE( "Output SPS/PPS buffer too small" );
                return -1;
            }

            p_out_buf[i_sps_pps_size++] = 0;
            p_out_buf[i_sps_pps_size++] = 0;
            p_out_buf[i_sps_pps_size++] = 0;
            p_out_buf[i_sps_pps_size++] = 1;

            memcpy( p_out_buf + i_sps_pps_size, p_buf, i_nal_size );
            i_sps_pps_size += i_nal_size;

            p_buf += i_nal_size;
            i_data_size -= i_nal_size;
        }
    }

    *p_sps_pps_size = i_sps_pps_size;

    return 0;
}

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

        if(m_iType == 1){
            packet.data[0] = 0x0;
            packet.data[1] = 0x0;
            packet.data[2] = 0x0;
            packet.data[3] = 0x1;
            if((packet.data[2] == 0x01 && packet.data[3] == 0x65) || (packet.data[3] == 0x01 && packet.data[4] == 0x65)){
                size_t spsSize = m_pFormatContext->streams[0]->codec->extradata_size + 20;
                uint8_t *spsData = (uint8_t *) malloc(sizeof(uint8_t) * spsSize);
                size_t nalu_size;
                convert_sps_pps(m_pFormatContext->streams[0]->codec->extradata, m_pFormatContext->streams[0]->codec->extradata_size,
                    spsData, spsSize, &spsSize, &nalu_size);

                for(int i = 0; i < spsSize; i+=4) {
                    LOGE("csd-0[%d]: %02x%02x%02x%02x\n", (int)spsSize, (int)spsData[i+0], (int)spsData[i+1], (int)spsData[i+2], (int)spsData[i+3]);
                }

                memcpy(data, spsData, spsSize);
                memcpy(data + spsSize, packet.data, packet.size);

                free(spsData);

                return packet.size + spsSize;
            }
        }

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
