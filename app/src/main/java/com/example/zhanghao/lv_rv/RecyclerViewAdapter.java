package com.example.zhanghao.lv_rv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhanghao.imageloader.ImageLoader;

/**
 *
 * Created by zhanghao on 17-9-27.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{


    private String [] mUrls;
    private Context mContext;
    public  RecyclerViewAdapter(Context contexts,String [] urls){
        mUrls = urls;
        mContext = contexts;
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder
                (LayoutInflater.from(mContext).inflate(R.layout.listview_itemview,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        ImageLoader.getInstance(mContext).into(mUrls[position],holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUrls.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
        }

    }
}
