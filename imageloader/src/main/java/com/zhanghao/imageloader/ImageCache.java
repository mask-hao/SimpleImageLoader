package com.zhanghao.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zhanghao on 17-9-22.
 */

class ImageCache {
    private static final String TAG = "ImageCache";
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private boolean mIsDiskLruCacheCreated = false;
    private ImageResizer imageResizer;

    ImageCache(Context context) {
        imageResizer = new ImageResizer();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };
        mIsDiskLruCacheCreated = initDiskLruCache(context);
    }


    private boolean initDiskLruCache(Context context){
        File diskCacheDir = getDiskCacheDir(context, "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                        DISK_CACHE_SIZE);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    private  File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private long getUsableSpace(File path) {
        return path.getUsableSpace();
    }

    public void addBitmapIntoMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemoryCache(key)==null){
            mMemoryCache.put(key,bitmap);
        }
    }

    public  Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    public  Bitmap getBitmapFromDiskCache(String url,int reqWidth,int reqHeight){
        if (mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap  =  null;
        String key = hashKeyFromUrl(url);
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot!=null){
                FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                bitmap = imageResizer.decodeSampleBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
                if (bitmap!=null){
                    addBitmapIntoMemoryCache(url,bitmap);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "getBitmapFromDisCache: "+e.getMessage());
            return null;
        }
        return bitmap;
    }

    public String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] digest) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1){
                builder.append("0");
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    public DiskLruCache getDiskLruCache(){
        return mDiskLruCache!=null?mDiskLruCache:null;
    }

    public int getDiskCacheIndex(){
        return DISK_CACHE_INDEX;
    }

    public void flush(){
        if (mDiskLruCache!=null){
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDiskLruCacheCreated(){
        return mIsDiskLruCacheCreated;
    }

}
