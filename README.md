# BitmapSizeOptimize

一、Android中使用的图片压缩库 
Android和IOS 中图片处理使用了一个叫做skia的开源图形处理引擎。他位于android源码的/external/skia 目录。我们平时在java层使用一个图片处理的函数实际上底层就是调用了这个开源引擎中的相关的函数，只不过在android7.0以前把哈夫曼算法关闭了。
二、Android 中常用的压缩方式 
Android中常用压缩方法分为2种：一种是降采样率压缩，另外一种是质量压缩。 

1.降采样率压缩的一般写法：
通过BitmapFactory压缩后，加载Bitmap到内存

2.质量压缩的一般写法：
bitmap.compress(Bitmap.CompressFormat.JPEG, 20, new FileOutputStream("sdcard/result.jpg"));


三、libjpeg 
我们使用质量压缩的话它的底层就是用skia引擎进行处理，加入我们调用bitmap.compress(Bitmap.CompressFormat.JPEG,…..) 他实际会 使用一个libjpeg.so 的动态库进行编码压缩。 
android在进行jpeg压缩编码的时候，考虑到了效率问题使用了定长编码方式进行编码（因为当时的手机性能都比较低），而IOS使用了变长编码的算法——哈夫曼算法。而且IOS对skia引擎也做了优化。所有我们看到同样的图片在ios上压缩会好一点。

四、优化思路 
1、下载开源的libjpeg，进行移植、编译得到libjpeg.so 
2、使用jni编写一个函数用来图片压缩 
3、在函数中添加一个开关选项，可以让我们选择是否使用哈夫曼算法。 
4、打包，搞成sdk供我们以后使用。

五、实现 
1、下载libjpeg
https://sourceforge.net/projects/libjpeg-turbo/files/

2、编译 
查看帮助文档	
https://github.com/libjpeg-turbo/libjpeg-turbo/blob/master/BUILDING.md

编译libjpeg使用到的工具
	1、sudo apt install autoconf
	2、sudo apt install libtool
	3、sudo apt install libsysfs-dev

a、生成配置文件
autoconf -fiv

b、编写脚本，编译为静态库

3、使用
提供对外访问的接口，编写JNI调用libjpeg,加入头文件和静态库
