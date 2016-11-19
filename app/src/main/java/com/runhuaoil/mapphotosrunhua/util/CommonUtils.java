package com.runhuaoil.mapphotosrunhua.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CommonUtils {

    public static final String PICTURE_PATH ;
    public static final String THUMB_PATH ;
// static 代码块，它在 CommonUtils 类首次加载时执行一次
    static {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        PICTURE_PATH = sdPath +  "/MapPhotos/Picture/";
        THUMB_PATH = PICTURE_PATH +  ".thumb/";
    }
/**
 * 生成一个以当前时间命名的照片文件名字符串
 */
    public static String getPictureNameByNowTime(){
        String filename =  null;
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss", Locale.CHINA );
        Date now = new Date();
        filename = sdf.format(now) +  ".jpg";
        return filename;
    }
    /**
     * 保存照片文件，并返回最终生成的照片文件完整路径
     */

    public static String savePicture(Context context, Bitmap bitmap,String path){
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        String filename = getPictureNameByNowTime ();
        String completePath = path + filename;
// 调用 Bitmap 的 compress 将图像压缩为〕PCG 格式保存到文件中
        try {
            FileOutputStream fos = new FileOutputStream(completePath);
            bitmap.compress(Bitmap.CompressFormat. JPEG , 100, fos);
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return completePath;
    }
/**
 * 解码照片文件，返回指定尺寸的 Bitmap 对象
 */
    public static Bitmap decodeBitmapFromFile(String absolutePath,int reqWidth, int reqHeight) {
        Bitmap bm =  null;
// 获取指定照片文件的分辨率
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds =  true;
        BitmapFactory. decodeFile (absolutePath, options);
// 计算采样倍率
        options.inSampleSize = calcInSampleSize (options, reqWidth, reqHeight);
// 按照指定倍率对照片进行解码,解码后即得到指定大小的 Bitmap 对象
        options.inJustDecodeBounds =  false;
        bm = BitmapFactory. decodeFile (absolutePath, options);
        return bm;
    }
/**
 * 计算解码尺寸倍率。结果是 1 则为原始图像大小，2 则为原图像的二分之一
 MapPhotos 地图相册项目开发
 62
 */
    public static int calcInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
// 图像原始尺寸
        final float height = options. outHeight;
        final float width = options. outWidth;
        int inSampleSize = 1;
// 根据宽高计算倍率，并四舍五入取整
        if (height > reqHeight || width > reqWidth) {
//将较小的值与期望的宽或高计算，以保证缩放后的图像有正常的宽高比例
            if (width > height) {
                inSampleSize = Math. round (height/reqHeight);
            } else {
//Math.round{)是四舍五入处理
                inSampleSize = Math. round (width/reqWidth);
            }
        }
        return inSampleSize;
    }
/**
 * 将图像文件解码为 128x128 的尺寸的 Bitmap 对象。得到的图像大小
 *不一定正好是 128x128 的尺寸，但宽和高均不超 128
 */
    public static Bitmap getPicture128(String path, String filename) {
        String imageFile = path + filename;
        return decodeBitmapFromFile (imageFile, 128, 128);
    }
    public static Bitmap getPicture128(String absolutePath) {
        return decodeBitmapFromFile (absolutePath, 128, 128);
    }
/**
 * 将图像文件解码为 64x64 的尺寸的 Bitmap 对象。得到的图像大小
 *不一定正好是 64x64 的尺寸，但宽和高均不超 64
 */
    public  static Bitmap getPicture64(String path, String filename) {
        String imageFile = path + filename;
        return decodeBitmapFromFile (imageFile, 64, 64);
    }
    public static Bitmap getPicture64(String absolutePath) {
        return decodeBitmapFromFile (absolutePath, 64, 64);
    }
}
