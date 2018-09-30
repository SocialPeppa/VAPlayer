package com.example.panda.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


import com.example.panda.mobileplayer.domain.Lyric;
import com.example.panda.mobileplayer.utils.DensityUtil;


import java.util.ArrayList;

import io.vov.vitamio.utils.Log;


public class ShowLyricView extends android.support.v7.widget.AppCompatTextView {

    private ArrayList<Lyric> lyrics;
    private Paint paint;
    private int width;
    private int height;
    private int index;
    private int textHeight;
    private float currentPosition;
    private float sleepTimt;
    private float timePoint;

    public ShowLyricView(Context context) {
        this(context, null);
    }

    public ShowLyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    private void initView(Context context) {
        textHeight= DensityUtil.dip2px(context,20);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint = new Paint();
        paint.setTextSize(textHeight);
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0) {
            float push;

            push=textHeight+((currentPosition-timePoint)/sleepTimt)*textHeight;

            canvas.translate(0,-push);

            String currentContent = lyrics.get(index).getContent();
            canvas.drawText(currentContent, width / 2, height / 2, paint);

            float tempY = height / 2;

            paint.setColor(Color.WHITE);

            for (int i = index - 1; i >= 0; i--) {
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                paint.setAlpha((int) (510.0f* tempY/height));
                if (tempY < 0) {
                    break;
                }
                canvas.drawText(preContent, width / 2, tempY, paint);
            }

            tempY = height / 2;
            for (int i = index + 1; i < lyrics.size(); i++) {
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                paint.setAlpha((int) (510.0f*(height/2-tempY)/height));
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextContent, width / 2, tempY, paint);
            }


        } else {
            canvas.drawText("没有歌词", width / 2, height / 2, paint);
        }
    }

    public void setShowLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lyrics == null || lyrics.size() == 0)
            return;

        for (int i = 0; i < lyrics.size(); i++) {
            if (i!=lyrics.size()-1&&currentPosition < lyrics.get(i + 1).getTimePoint() && currentPosition >= lyrics.get(i).getTimePoint()) {
                index = i;
                sleepTimt = lyrics.get(index).getSleepTime();
                timePoint = lyrics.get(index).getTimePoint();
                break;
            }
            while (i==lyrics.size()-1){
                index=i;
                break;
            }
        }

        invalidate();
    }
}
