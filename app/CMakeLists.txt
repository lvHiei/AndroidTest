# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

#set($CFL)

INCLUDE_DIRECTORIES(./src/main/cpp/ffmpeg)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/audioTest/AudioFrameDecoder.cpp
             src/main/cpp/audioTest/AudioStreamDecoder.cpp
             src/main/cpp/audioTest/CAACHe2Lc.cpp
             src/main/cpp/audioTest/AacReader.cpp
             src/main/cpp/util/TimeUtil.cpp
             src/main/cpp/format/VVAVFormat.cpp
             src/main/cpp/format/Mp4Muxer.cpp
             src/main/cpp/codec/VVAVEncoder.cpp
             src/main/cpp/codec/VVAVDecoder.cpp
             src/main/cpp/native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

#link_directories(${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/)
#SET(CMAKE_LIBRARY_PATH ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/)
#message(STATUS "11111111111  ${CMAKE_LIBRARY_PATH}")

find_library( # Sets the name of the path variable.
            ffmpeg-lib

            # Specifies the name of the NDK library that
            # you want CMake to locate.
            ijkffmpeg
            )

#增加so文件动态共享库，${ANDROID_ABI}表示so文件的ABI类型的路径
add_library(ffmpeg-lib SHARED IMPORTED)
set_target_properties(ffmpeg-lib  PROPERTIES
IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libijkffmpeg.so)

SET(FIND_MSG "ffmpeg-lib is " ${ffmpeg-lib})
Message(WARNING ${FIND_MSG})
#Message(FATAL_ERROR ${ffmpeg-lib})

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       #${ffmpeg-lib}
                       ffmpeg-lib
                       )

#target_include_directories(native-lib PUBLIC ffmpeg)