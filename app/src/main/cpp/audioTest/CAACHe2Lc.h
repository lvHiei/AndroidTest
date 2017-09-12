//
// Created by mj on 17-9-12.
//

#ifndef ANDROIDTEST_CAACHE2LC_H
#define ANDROIDTEST_CAACHE2LC_H


#include "../format/VVAVFormat.h"
#include "../codec/VVAVEncoder.h"
#include "../codec/VVAVDecoder.h"

class CAACHe2Lc {
public:
    CAACHe2Lc();
    virtual ~CAACHe2Lc();


    int doConvert(const char* inputfilename, const char* outputfilename);

private:
    VVAVFormat* m_pFormat;
    AVFormatContext* m_pIFCtx;
    AVFormatContext* m_pOFCtx;

    VVAVEncoder* m_pEncoder;
    VVAVDecoder* m_pDecoder;

    AVCodecContext* m_pDCtx;
    AVCodecContext* m_pECtx;
};


#endif //ANDROIDTEST_CAACHE2LC_H
