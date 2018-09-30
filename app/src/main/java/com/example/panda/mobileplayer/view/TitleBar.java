package com.example.panda.mobileplayer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.activity.MainActivity;

public class TitleBar extends LinearLayout implements View.OnClickListener {

    private View tv_search;
    private View rl_ganme;
    private View iv_record;
    private Context context;


    public TitleBar(Context context) {
        this(context,null);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_search=getChildAt(0);
        rl_ganme=getChildAt(1);
        iv_record=getChildAt(2);

        tv_search.setOnClickListener(this);
        rl_ganme.setOnClickListener(this);
        iv_record.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                Toast.makeText(context,"sousuo",Toast.LENGTH_SHORT).show();
              break;
            case R.id.rl_game:
                break;
            case R.id.iv_record:
                break;
        }

    }
}
