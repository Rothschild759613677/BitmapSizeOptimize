
cmake_minimum_required(VERSION 3.4.1)

add_library(
             native-jpeg
             SHARED
             src/main/cpp/native-jpeg.cpp )

add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/cpp/turbojpeg/lib/libturbojpeg.a)
#引入头文件
include_directories(src/main/cpp/turbojpeg/include)

find_library(
              log-lib
              log )

target_link_libraries(
                       native-jpeg
                       libjpeg
                       jnigraphics
                       ${log-lib} )
