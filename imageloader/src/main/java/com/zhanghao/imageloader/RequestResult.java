package com.zhanghao.imageloader;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Created by zhanghao on 17-9-26.
 */

class RequestResult {
    BitmapDrawable bitmapDrawable;
    ImageView imageView;

    RequestResult(BitmapDrawable bitmapDrawable, ImageView imageView) {
        this.bitmapDrawable = bitmapDrawable;
        this.imageView = imageView;
    }
}
