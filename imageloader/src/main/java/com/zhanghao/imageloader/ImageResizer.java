package com.zhanghao.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/**
 * Created by zhanghao on 17-9-20.
 */

public class ImageResizer {
    public Bitmap decodeSampleBitmapFromResource(Resources resources,
                                                 int resId,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources,resId,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources,resId,options);
    }


    public Bitmap decodeSampleBitmapFromFileDescriptor(FileDescriptor descriptor,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(descriptor,null,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(descriptor,null,options);
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqHeight == 0 || reqWidth == 0){
            return 1;
        }
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (width > reqWidth || height>reqHeight){
            int halfWidth = width/2;
            int halfHeight = height/2;
            while ((halfWidth/inSampleSize)>=reqWidth && (halfHeight/inSampleSize)>=reqHeight){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
