package com.zhanghao.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhanghao on 17-9-22.
 */

public class ImageRequest {
    private static final String TAG = "ImageRequest";
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private ImageCache mImageCache = null;
    private boolean mIsDiskLruCacheCreated = false;
    private OkHttpClient mHttpClient;
    public ImageRequest(Context context) {
        mImageCache  = new ImageCache(context);
        mIsDiskLruCacheCreated = mImageCache.isDiskLruCacheCreated();
        initOkHttpClient();
    }

    private void initOkHttpClient(){
        mHttpClient =  new OkHttpClient();
    }

    public Bitmap request(String url){
        return request(url,0,0);
    }

    public Bitmap request(String url,int reqWidth,int reqHeight){
        Bitmap bitmap = requestFromMemoryCache(url);
        if (bitmap!=null){
            Log.d(TAG, "request: from lruCache");
            return bitmap;
        }
        bitmap = requestFromDiskCache(url,reqWidth,reqHeight);
        if (bitmap!=null){
            Log.d(TAG, "request: from diskLruCache");
            return bitmap;
        }
        bitmap  = requestFromHttp(url,reqWidth,reqHeight);
        if (bitmap != null){
            Log.d(TAG, "request: from http");
            return bitmap;
        }
        return null;
    }


    public Bitmap requestFromMemoryCache(String key){
        return mImageCache.getBitmapFromMemoryCache(key);
    }

    public Bitmap requestFromDiskCache(String url,int reqWidth,int reqHeight){
        return mImageCache.getBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    public Bitmap requestFromHttp(String url,int reqWidth,int reqHeight){
        return requestFromURLConnection(url,reqWidth,reqHeight);
    }

    /**
     * 使用url请求图片，并且加入DiskLruCache
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return bitmap
     */
    private Bitmap requestFromURLConnection(String url, int reqWidth, int reqHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()){
            throw new IllegalStateException("You must call this method on the main thread");
        }
        if (mIsDiskLruCacheCreated){
            //diskLruCache创建成功了
            Log.d(TAG, "requestFromURLConnection: diskLruCache is created");
            try {
                String key = mImageCache.hashKeyFromUrl(url);
                DiskLruCache.Editor editor = mImageCache.getDiskLruCache().edit(key);
                if (editor!=null){
                    OutputStream outputStream = editor.newOutputStream(mImageCache.getDiskCacheIndex());
                    if (saveBitmapIntoDiskLruCache(url,outputStream)){
                        editor.commit();
                    }else{
                        editor.abort();
                    }
                    mImageCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return requestFromDiskCache(url,reqWidth,reqHeight);
        }else{
          //diskLruCache创建失败了
            Log.d(TAG, "requestFromURLConnection: diskLruCache is not created");
            return requestFromURLConnection(url);
        }

    }


    /**
     * 使用url请求图片，适用于diskLruCache创建失败的情况
     * @param url
     * @return
     */
    private Bitmap requestFromURLConnection(String url){
        Request request =
                new Request.Builder().url(url).build();
        InputStream inputStream;
        try {
            ResponseBody response = mHttpClient.newCall(request).execute().body();
            if (response!=null){
                inputStream = response.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mImageCache.addBitmapIntoMemoryCache(url, bitmap);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;

    }

    private boolean saveBitmapIntoDiskLruCache(String url,OutputStream outputStream){
        Request request = new Request.Builder()
                .url(url).build();
        try {
            ResponseBody response = mHttpClient.newCall(request).execute().body();
            if (response != null) {
                InputStream inputStream = response.byteStream();
                BufferedOutputStream out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
                int b;
                while ((b = inputStream.read()) != -1) {
                    out.write(b);
                }
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
