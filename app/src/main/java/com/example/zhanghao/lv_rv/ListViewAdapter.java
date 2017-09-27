package com.example.zhanghao.lv_rv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.zhanghao.imageloader.ImageLoader;

import java.util.List;

/**
 * Created by zhanghao on 2017/9/24.
 */

public class ListViewAdapter extends BaseAdapter{
    private List<String> mUrls;
    private Context mContext;

    public ListViewAdapter(Context context,List<String> urls) {
        this.mUrls = urls;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return mUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_itemview,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String url = mUrls.get(position);
        ImageLoader.getInstance(mContext).into(url,viewHolder.imageView);
        return convertView;
    }

    private static class ViewHolder{
        ImageView imageView;
    }

}
