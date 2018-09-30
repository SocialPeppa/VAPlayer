package com.example.panda.mobileplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.domain.MediaItem;
import com.example.panda.mobileplayer.utils.Utils;

import java.util.ArrayList;

public class VideoAdapter extends BaseAdapter {

    private final boolean isVidoe;
    private Utils utils;
    private final Context context;
    private final ArrayList<MediaItem> mediaItems;

    public VideoAdapter(Context context, ArrayList<MediaItem> mediaItems,boolean isVideo){
        this.context=context;
        this.mediaItems=mediaItems;
        this.isVidoe=isVideo;
        utils=new Utils();
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView==null){
            convertView=View.inflate(context, R.layout.item_video_pager,null);
            viewHoder=new ViewHoder();
            viewHoder.iv_icon=convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name=convertView.findViewById(R.id.tv_name);
            viewHoder.tv_time=convertView.findViewById(R.id.tv_time);
            viewHoder.tv_size=convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHoder);
        }else{
            viewHoder= (ViewHoder) convertView.getTag();
        }
        MediaItem mediaItem=mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
        viewHoder.tv_time.setText(utils.stringForTime((int) mediaItem.getDuration()));
        if (isVidoe){
            Glide.with( context ).load(mediaItem.getData()).into(viewHoder.iv_icon);
        }else {
            viewHoder.iv_icon.setImageResource(R.drawable.music_default_bg);
        }


        return convertView;
    }
    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }

} 
