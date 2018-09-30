package com.example.panda.mobileplayer.activity;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.base.BasePager;
import com.example.panda.mobileplayer.pager.AudioPager;
import com.example.panda.mobileplayer.pager.NetAudioPager;
import com.example.panda.mobileplayer.pager.NetVideoPager;
import com.example.panda.mobileplayer.pager.VideoPager;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {

    private RadioGroup rg_button_tag;
    private static ArrayList<BasePager> basePagers;
    private static int position;
    private boolean isExit=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_button_tag = findViewById(R.id.rg_button_tag);
        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));
        basePagers.add(new AudioPager(this));
        basePagers.add(new NetVideoPager(this));
        basePagers.add(new NetAudioPager(this));

        rg_button_tag.setOnCheckedChangeListener(new MyonOnCheckedChangeListener());
        rg_button_tag.check(R.id.rb_video);
        setStatusColor();

    }

    private void setStatusColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.status));
        }
    }


    class MyonOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_net_audio:
                    position = 3;
                    break;
            }

            setFragment();

        }
    }

    public static class myFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            BasePager basePager = getBasePager();
            if (basePager != null) {
                return basePager.rootview;
            }
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    private static BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.initData();
            basePager.isInitData = true;
        }
        return basePager;
    }

    private void setFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.fl_main_content, new myFragment());
        ft.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK){
            if(!isExit){
                isExit=true;
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        isExit=false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
