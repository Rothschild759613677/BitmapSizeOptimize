#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#import <malloc.h>
#include "turbojpeg/include/jpeglib.h"

#define LOG_TAG "Nick"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C"
int write_JPEG_file(uint8_t *data, uint32_t w, uint32_t h, jint q, const char *path) {

    //3.1、创建jpeg压缩对象
    jpeg_compress_struct jcs;

    //错误回调
    jpeg_error_mgr error;
    jcs.err = jpeg_std_error(&error);

    //创建压缩对象
    jpeg_create_compress(&jcs);

    //3.2、指定存储文件    // w = 写,b = 二进制
    FILE *f = fopen(path, "wb");
    if (f == NULL) {
        return 0;
    }
    jpeg_stdio_dest(&jcs, f);

    //3.3、设置压缩参数
    jcs.image_width = w;
    jcs.image_height = h;
    //bgr
    jcs.input_components = 3;
    jcs.in_color_space = JCS_RGB;
    jpeg_set_defaults(&jcs);

    //开启哈夫曼 1=true 0=false
    jcs.optimize_coding = 1;
    jpeg_set_quality(&jcs, q, 1);

    //3.4、开始压缩
    jpeg_start_compress(&jcs, 1);

    //3.5、循环写入每一行数据
    int row_stride = w * 3;

    //next_scan_line 一行数据开头的位置
    JSAMPROW row[1];
    while (jcs.next_scanline < jcs.image_height) {
        //拿一行数据
        uint8_t *pixels = data + jcs.next_scanline * row_stride;
        row[0] = pixels;
        jpeg_write_scanlines(&jcs, row, 1);
    }

    //3.6、压缩完成
    jpeg_finish_compress(&jcs);

    //3.7、释放jpeg对象
    jpeg_destroy_compress(&jcs);

    fclose(f);
    LOG_D("Bitmap 压缩成功");
    return 1;
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_moonsky_bitmapsizeoptimize_BitmapUtils_nativeCompressBitmap(JNIEnv *env, jclass type, jobject bitmap, jint quality, jstring filePath_) {
    const char *filePath = env->GetStringUTFChars(filePath_, 0);

    //从bitmap 获得 argb数据
    AndroidBitmapInfo info;

    //获得bitmap的信息 比如 宽、高
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOG_D("获取Bitmap信息失败");
        return 0;
    }

    //c/c++ char = byte
    uint8_t *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels) < 0) {
        LOG_D("加载图片失败");
        return 0;
    }

    //jpeg alpha=透明?    去掉 透明度
    uint32_t w = info.width;
    uint32_t h = info.height;

    int color;

    //rgb
    uint8_t *data = (uint8_t *) malloc(w * h * 3);
    uint8_t *temp = data;
    uint8_t r, g, b;
    for (int i = 0; i < h; ++i) {
        for (int j = 0; j < w; ++j) {
            //argb = 4字节  int
            // * =>解引用=》解释引用
            color = *(int *) pixels; //0-3字节
            //argb
            r = (uint8_t) ((color >> 16) & 0xFF);
            g = (uint8_t) ((color >> 8) & 0xFF);
            b = (uint8_t) (color & 0xFF);

            //以前主流bgr      libjpeg  bgr
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data += 3;
            //指针 跳过4个字节 //4-7字节
            pixels += 4;
        }
    }
    int resultCode = write_JPEG_file(temp, w, h, quality, filePath);
    AndroidBitmap_unlockPixels(env, bitmap);
    if (*data != '\0' && *data != NULL) {
        free(data);
    }
    env->ReleaseStringUTFChars(filePath_, filePath);

    return (jboolean) (resultCode == 1);
}