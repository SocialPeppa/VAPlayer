package com.example.panda.mobileplayer.base;

import android.content.Context;
import android.view.View;

public abstract class BasePager {

    public final Context context;
    public View rootview;
    public boolean isInitData;

    public BasePager(Context context) {
        this.context = context;
        rootview = initView();
    }

    public abstract View initView();

    public void initData(){

    }

}

