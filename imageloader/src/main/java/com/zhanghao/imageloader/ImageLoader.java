package com.zhanghao.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhanghao on 17-9-21.
 */

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static volatile ImageLoader mImageLoader = null;
    private ImageRequest mImageRequest = null;
    public static final int MESSAGE_POST_RESULT = 1;
    private final Context mContext;

    public Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_POST_RESULT) {
                RequestResult result = (RequestResult) msg.obj;
                ImageView imageView = result.imageView;
                imageView.setImageDrawable(result.bitmapDrawable);
            }
        }
    };

    public static ImageLoader getInstance(Context context) {
        Context internalContext = context.getApplicationContext();
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(internalContext);
                }
            }
        }
        return mImageLoader;
    }

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger atomicInteger = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "imageLoader: " + atomicInteger.getAndIncrement());
        }
    };

    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), mThreadFactory
            );


    private ImageLoader(Context context) {
        mContext = context;
        mImageRequest = new ImageRequest(context);
    }

    public void into(String url, ImageView imageView) {
        into(url, imageView, 0, 0);
    }

    public void into(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {

        Bitmap bitmap = mImageRequest.requestFromMemoryCache(url);
        if (bitmap!=null){
            Log.d(TAG, "request: from lruCache");
            setBitmap(imageView,bitmap);
            return;
        }
        if (cancelRequestRunnable(url,imageView)){
            RequestRunnable requestRunnable =
                    new RequestRunnable(this,imageView,url,reqWidth,reqHeight);
            RequestDrawable requestDrawable
                    = new RequestDrawable(getContext().getResources(),null,requestRunnable);
            imageView.setImageDrawable(requestDrawable);
            THREAD_POOL_EXECUTOR.execute(requestRunnable);
        }
    }

    private void setBitmap(ImageView imageView, Bitmap bitmap) {
        BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(),bitmap);
        imageView.setImageDrawable(drawable);
    }

    Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = mImageRequest.requestFromDiskCache(url,reqWidth,reqHeight);
        if (bitmap!=null){
            Log.d(TAG, "request: from diskLruCache");
            return bitmap;
        }
        bitmap  = mImageRequest.requestFromHttp(url,reqWidth,reqHeight);
        if (bitmap != null){
            Log.d(TAG, "request: from http");
            return bitmap;
        }
        return null;
    }


    public  RequestRunnable getRequestRunnable(ImageView imageView) {
        if (imageView!=null){
            Drawable drawable = imageView.getDrawable();
            if (drawable !=null){
                if (drawable instanceof RequestDrawable){
                    Log.d(TAG, "getRequestRunnable: drawable instanceOf requestDrawable");
                    RequestDrawable requestDrawable = (RequestDrawable) drawable;
                    return requestDrawable.getRunnable();
                }else{
                    Log.d(TAG, "getRequestRunnable: drawable not instanceOf requestDrawable");
                }
            }else{
                Log.d(TAG, "getRequestRunnable: drawable is null");
            }

        }
        return null;
    }

    public boolean cancelRequestRunnable(String url,ImageView imageView){
        RequestRunnable previous = getRequestRunnable(imageView);
        if (previous != null){
            String imageUrl = previous.mUrl;
            if (imageUrl== null || !imageUrl.equals(url)){
                previous.setIsCanceled(true);
                boolean remove = THREAD_POOL_EXECUTOR.remove(previous);
                Log.d(TAG, "cancelRequestRunnable: remove previous "+remove);
            }else{
                Log.d(TAG, "cancelRequestRunnable: didn't remove");
                //当前imageView已经有drawable
                return false;
            }
        }

        if (previous ==null){
            Log.d(TAG, "cancelRequestRunnable: runnable is null, cancel terminated");
        }else{
            Log.d(TAG, "cancelRequestRunnable: cancel request success");
        }
        return true;
    }

    public Context getContext(){
        return mContext;
    }


}
