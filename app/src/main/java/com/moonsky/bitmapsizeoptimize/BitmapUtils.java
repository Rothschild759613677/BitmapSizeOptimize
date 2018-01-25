package com.moonsky.bitmapsizeoptimize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * 图片工具类
 *
 * @author nick
 * @date 18-1-24
 */

public class BitmapUtils {

    /**
     * 旋转图片
     *
     * @param bitmap 数据源
     * @param degree 旋转的角度
     * @return Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {

        if (bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }


    /**
     * 获取图片的角度
     *
     * @param path 图片的全路径
     * @return int
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int attributeInt = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (attributeInt) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return degree;
    }

    /**
     * 缩放的比例因子
     *
     * @param options   BitmapFactory.Options
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return int
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            //宽度比例值
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 获取最小存的Bitmap
     *
     * @param path   Bitmap全路径
     * @param width  目标宽
     * @param height 目标 高
     * @return Bitmap
     */
    public static Bitmap getMemoryBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, width, height);

        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * 获取压缩图片
     *
     * @param filePath 数据源
     * @param width    目标宽
     * @param height   目标高
     * @param fileName 新的文件名
     * @param quality  压缩质量
     * @return String
     * @throws FileNotFoundException
     */
    public static String compressBitmap(String filePath, int width, int height, String fileName,
                                        Bitmap.CompressFormat format, int quality) throws FileNotFoundException {
        Bitmap memoryBitmap = getMemoryBitmap(filePath, width, height);

        int degree = getBitmapDegree(filePath);
        if (degree != 0) {
            memoryBitmap = rotateBitmap(memoryBitmap, degree);
        }
        File file = new File(Constant.ROOT_DIR, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        memoryBitmap.compress(format, quality, fos);

        return file.getPath();
    }

    static {
        System.loadLibrary("native-jpeg");
    }


    public static native boolean nativeCompressBitmap(Bitmap bitmap, int quality, String filePath);

}
