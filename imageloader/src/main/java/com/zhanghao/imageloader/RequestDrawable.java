package com.zhanghao.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by zhanghao on 17-9-26.
 */

class RequestDrawable extends BitmapDrawable {

    private WeakReference<RequestRunnable> mRunnableRef;

    RequestDrawable(Resources res, Bitmap bitmap, RequestRunnable runnable) {
        super(res, bitmap);
        mRunnableRef = new WeakReference<>(runnable);
    }

    RequestRunnable getRunnable() {
        return mRunnableRef.get();
    }
}
