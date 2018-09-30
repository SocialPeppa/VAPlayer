package com.example.panda.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.domain.MediaItem;

import java.util.ArrayList;

public class NetAudioAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<MediaItem> mediaItems;

    public NetAudioAdapter(Context context, ArrayList<MediaItem> mediaItems){
        this.context=context;
        this.mediaItems=mediaItems;
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
            convertView=View.inflate(context, R.layout.item_net_aideo_pager,null);
            viewHoder=new ViewHoder();
            viewHoder.iv_icon=convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name=convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc=convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHoder);
        }else{
            viewHoder= (ViewHoder) convertView.getTag();
        }
        MediaItem mediaItem=mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getArtist());
      // x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());



        return convertView;
    }
    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }

} 
