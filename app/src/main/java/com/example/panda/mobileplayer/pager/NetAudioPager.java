package com.example.panda.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.activity.AudioPlayerActivity;
import com.example.panda.mobileplayer.adapter.NetAudioAdapter;
import com.example.panda.mobileplayer.base.BasePager;
import com.example.panda.mobileplayer.domain.MediaItem;
import com.example.panda.mobileplayer.utils.CacheUtils;
import com.example.panda.mobileplayer.utils.Constants;
import com.example.panda.mobileplayer.view.XListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class NetAudioPager extends BasePager {
    @ViewInject(R.id.net_listview)
    private XListView net_listview;
    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;
    @ViewInject(R.id.pb_net_loading)
    private ProgressBar pb_net_loading;

    private ArrayList<MediaItem> mediaItems;
    private NetAudioAdapter adapter;
    private boolean isLoadMore = false;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager, null);
        x.view().inject(this, view);
        net_listview.setOnItemClickListener(new NetAudioPager.MyOnItemClickListener());
        net_listview.setPullLoadEnable(true);
        net_listview.setXListViewListener(new NetAudioPager.MyIXListViewListener());
        return view;
    }

    class MyIXListViewListener implements XListView.IXListViewListener {

        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {
            getMoreDataFromNet();
        }
    }

    private void getMoreDataFromNet() {
        RequestParams params = new RequestParams(Constants.Audio_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                isLoadMore = true;
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                isLoadMore = false;
            }
        });
    }

    private void onLoad() {
        net_listview.stopRefresh();
        net_listview.stopLoadMore();
        net_listview.setRefreshTime("" + getSystemTime());
    }

    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String systemTime = format.format(new Date());
        return systemTime;
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                Toast.makeText(context, "正在刷新", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("audiolist", mediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position", position - 1);
                context.startActivity(intent);
            }

        }
    }

    @Override
    public void initData() {
        super.initData();
        getDataFromNet();
        String saveJson = CacheUtils.getString(context, Constants.Audio_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            processData(saveJson);
        }

    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.Audio_URL);

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                processData(result);
                CacheUtils.putString(context, Constants.Audio_URL, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void processData(String json) {
        if (!isLoadMore) {
            mediaItems = parseJson(json);
            showData();
        } else {
            isLoadMore = false;
            mediaItems.addAll(parseJson(json));
            adapter.notifyDataSetChanged();
            onLoad();
        }

    }

    private void showData() {

        if (mediaItems != null && mediaItems.size() > 0) {
            adapter = new NetAudioAdapter(context, mediaItems);
            net_listview.setAdapter(adapter);
            onLoad();
            tv_nonet.setVisibility(View.GONE);
        } else {
            tv_nonet.setVisibility(View.VISIBLE);
        }
        pb_net_loading.setVisibility(View.GONE);
    }

    private ArrayList<MediaItem> parseJson(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("songlist");

            if (jsonArray != null && jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject datajson = jsonArray.getJSONObject(i);
                    String data = datajson.get("data").toString();
                    JSONObject jsonObjectItem = new JSONObject(data);
                    if (jsonObjectItem != null) {
                        MediaItem mediaItem = new MediaItem();
                        String audioName = jsonObjectItem.optString("songname");
                        mediaItem.setName(audioName);
                        JSONArray jsonArray2 = jsonObjectItem.optJSONArray("singer");
                        for (int n = 0; n < jsonArray2.length(); n++) {
                            JSONObject singerName = jsonArray2.getJSONObject(n);
                            if (singerName != null) {
                                String audioTitle = singerName.optString("name");

                                mediaItem.setArtist(audioTitle);
                            }

                        }
                        String audioUrl = jsonObjectItem.optString("songmid");
                        mediaItem.setData("http://ws.stream.qqmusic.qq.com/C100" + audioUrl + ".m4a?fromtag=0&guid=126548448");
                        mediaItems.add(mediaItem);

                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaItems;
    }

}
