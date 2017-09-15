//
// Created by mj on 17-9-15.
//

#ifndef ANDROIDTEST_AACREADER_H
#define ANDROIDTEST_AACREADER_H


#include "../format/VVAVFormat.h"

class AacReader {
public:
    AacReader();
    ~AacReader();

public:
    int open_file(const char* aacfile);
    int read_pacekt(uint8_t* data, int length);
    int close_file();

private:
    VVAVFormat* m_pFormat;
    AVFormatContext* m_pFormatContext;
};


#endif //ANDROIDTEST_AACREADER_H
