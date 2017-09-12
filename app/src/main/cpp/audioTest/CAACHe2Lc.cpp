//
// Created by mj on 17-9-12.
//

#include "CAACHe2Lc.h"


CAACHe2Lc::CAACHe2Lc()
{

    m_pFormat = new VVAVFormat();
    m_pDecoder = new VVAVDecoder();
    m_pEncoder = new VVAVEncoder();

    m_pIFCtx = NULL;
    m_pOFCtx = NULL;
    m_pDCtx = NULL;
    m_pECtx = NULL;

}


CAACHe2Lc::~CAACHe2Lc()
{
    if(m_pFormat){
        delete m_pFormat;
        m_pFormat = NULL;
    }

    if(m_pDecoder){
        delete m_pDecoder;
        m_pDecoder = NULL;
    }

    if(m_pEncoder){
        delete m_pEncoder;
        m_pEncoder = NULL;
    }
}


int CAACHe2Lc::doConvert(const char *inputfilename, const char *outputfilename)
{

    m_pIFCtx = m_pFormat->alloc_foramt_context();
    m_pOFCtx = m_pFormat->alloc_foramt_context();

    int iAudioIndex;
    int oAudioIndex;


    AVStream* pVideoStream = NULL;
    AVStream* pAudioStream = NULL;
    int ret = 0;
    int framerate;
    int bitrate;

    AVFrame* audioFrame = av_frame_alloc();
    AVFrame* videoFrame = av_frame_alloc();

    int got_picture;

    ret = m_pFormat->open_input_file(&m_pIFCtx, inputfilename);

    if(ret < 0){
        goto codecFail;
    }

    iAudioIndex = m_pFormat->find_audio_stream(m_pIFCtx);

    m_pDCtx = m_pIFCtx->streams[iAudioIndex]->codec;

    ret = m_pDecoder->open_decoder(m_pDCtx);
    if(ret < 0){
    	goto codecFail;
    }

    ret = m_pFormat->alloc_output_context(&m_pOFCtx, outputfilename);

    if(ret < 0){
        goto codecFail;
    }


    pAudioStream = m_pFormat->add_stream(m_pOFCtx, (AVStream*)NULL, false);
    pAudioStream->time_base = m_pIFCtx->streams[iAudioIndex]->time_base;


    oAudioIndex = pAudioStream->index;
    m_pECtx = pAudioStream->codec;

    ret = m_pEncoder->init_and_open_audio_encoder(m_pECtx, m_pDCtx->codec_id,
    		m_pDCtx->sample_rate, m_pDCtx->channels, m_pDCtx->bit_rate, 0);

	if(ret < 0){
		goto codecFail;
	}

    ret = m_pFormat->open_output_file(m_pOFCtx);
    if(ret < 0){
        goto codecFail;
    }

    ret = m_pFormat->write_hearder(m_pOFCtx);
    if(ret < 0){
        goto codecFail;
    }

    AVPacket rpacket;
    AVPacket epacket;

    av_init_packet(&epacket);

    // 这里封装也是有问题的 应该按照时间戳大小封装
    while(true){
        if(m_pFormat->read_packet(m_pIFCtx, &rpacket) < 0){
            break;
        }

        if(rpacket.stream_index == iAudioIndex){
			ret = m_pDecoder->decode_audio(m_pDCtx, audioFrame, &got_picture, &rpacket);
			if(ret >= 0 && got_picture){
                for(int i = 0; i < 2; i++){
//                    audioFrame->nb_samples = 1024;
//                    audioFrame->data[0] = audioFrame->data[0] + i * 4096;
//                    audioFrame->linesize[0] = 4096;
                    ret = m_pEncoder->encode_audio_frame(m_pECtx, &epacket, audioFrame, &got_picture);
                    if(ret >= 0 && got_picture){
                        epacket.stream_index = oAudioIndex;
                        m_pFormat->write_packet(m_pOFCtx, epacket);
                        av_packet_unref(&epacket);
                    }
                }

			}

            rpacket.stream_index = oAudioIndex;
        }
        av_packet_unref(&rpacket);
    }


	while(true){
		ret = m_pDecoder->flush_audio_decoder(m_pDCtx, audioFrame, &got_picture, &rpacket);
		if(ret < 0 || !got_picture){
			break;
		}

        for(int i = 0; i < 2; i++){
            audioFrame->data[0] = audioFrame->data[0] + i * 1024;
            ret = m_pEncoder->encode_audio_frame(m_pECtx, &epacket, audioFrame, &got_picture);
            if(ret >= 0 && got_picture){
                epacket.stream_index = oAudioIndex;
                m_pFormat->write_packet(m_pOFCtx, epacket);
                av_packet_unref(&epacket);
            }
        }
	}


	while(true){
		ret = m_pEncoder->encode_audio_frame(m_pECtx, &epacket, NULL, &got_picture);
		if(ret < 0 || !got_picture){
			break;
		}

		epacket.stream_index = oAudioIndex;
		m_pFormat->write_packet(m_pOFCtx, epacket);
		av_packet_unref(&epacket);
	}


    m_pFormat->write_tailer(m_pOFCtx);

codecFail:

	m_pEncoder->close_encoder(m_pECtx);
	m_pDecoder->close_decoder(m_pDCtx);
    m_pFormat->close_in_out_file(&m_pIFCtx, &m_pOFCtx);
    m_pFormat->free_format_context(m_pIFCtx);
    m_pFormat->free_format_context(m_pOFCtx);


    return 0;
}

