package com.example.panda.mobileplayer.domain;


public class Lyric {

    private long timePoint;

    private String content;

    private long sleepTime;

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "timePoint=" + timePoint +
                ", content='" + content + '\'' +
                ", sleepTime=" + sleepTime +
                '}';
    }
}
