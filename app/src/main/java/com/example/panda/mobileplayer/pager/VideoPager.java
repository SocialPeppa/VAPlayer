package com.example.panda.mobileplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.activity.SystemVideoPlayer;
import com.example.panda.mobileplayer.adapter.VideoAdapter;
import com.example.panda.mobileplayer.base.BasePager;
import com.example.panda.mobileplayer.domain.MediaItem;
import java.util.ArrayList;



public class VideoPager extends BasePager {

    private ListView listview;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mediaItems;
    private VideoAdapter videoAdapter;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems!=null&&mediaItems.size()>0){
                videoAdapter=new VideoAdapter(context,mediaItems,true);
                listview.setAdapter(videoAdapter);
                tv_nomedia.setVisibility(View.GONE);
            }else{
                tv_nomedia.setVisibility(View.VISIBLE);
            }
            pb_loading.setVisibility(View.GONE);
        }
    };

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
      View view=View.inflate(context, R.layout.video_pager,null);
        listview = view.findViewById(R.id.listview);
        tv_nomedia = view.findViewById(R.id.tv_nomedia);
        pb_loading = view.findViewById(R.id.pb_loading);
        listview.setOnItemClickListener(new MyOnItemClickListener());
       return view;

    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // MediaItem mediaItem=mediaItems.get(position);
            Intent intent=new Intent(context,SystemVideoPlayer.class);
           // intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/* ");
            Bundle bundle=new Bundle();
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }

    @Override
    public void initData() {
        super.initData();
        getDataFromLocal();

    }

    private void getDataFromLocal() {

        new Thread(){
            @Override
            public void run() {
                super.run();
                mediaItems=new ArrayList<>();
                ContentResolver resolver=context.getContentResolver();
                Uri url= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs={
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.ARTIST
                };
                Cursor cursor=resolver.query(url,objs,null,null,null);
                if (cursor!=null){
                    while(cursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        String name=cursor.getString(0);
                        mediaItem.setName(name);
                        long duration=cursor.getLong(1);
                        mediaItem.setDuration(duration);
                        long size=cursor.getLong(2);
                        mediaItem.setSize(size);
                        String data=cursor.getString(3);
                        mediaItem.setData(data);
                        String artist=cursor.getString(4);
                        mediaItem.setArtist(artist);
                        mediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(10);
            }

        }.start();
    }


}
