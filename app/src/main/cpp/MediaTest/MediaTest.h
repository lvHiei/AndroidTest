//
// Created by mj on 17-10-31.
//

#ifndef ANDROIDTEST_MEDIATEST_H
#define ANDROIDTEST_MEDIATEST_H


#include <stdint.h>
#include "../format/VVAVFormat.h"

class MediaTest {
public:
    MediaTest();
    ~MediaTest();

public:
    int open_file(const char* file);
    int read_pacekt(uint8_t* data, int length);
    int close_file();
    int64_t getTimestamp();
    int getType();


private:
    VVAVFormat* m_pFormat;
    AVFormatContext* m_pFormatContext;

    int64_t m_iTimestamp;
    int m_iVStream;
    int m_iAStream;
    int m_iType;
};


#endif //ANDROIDTEST_MEDIATEST_H
