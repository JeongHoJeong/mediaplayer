package com.example.jeongho.mediaplayer;

import android.graphics.Bitmap;

import java.io.File;

public class Music extends Media {
    private Bitmap coverBitmap = null;
    private String title = null;
    private String artist = null;
    private Long duration = null;

    public Music(File file) {
        super(file);
    }

    public void setBitmap(Bitmap coverBitmap) { this.coverBitmap = coverBitmap; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) {  this.artist = artist; }
    public void setDuration(Long duration) { this.duration = duration; }

    public Bitmap getBitmap() { return coverBitmap; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public Long getDuration() { return duration; }
}
