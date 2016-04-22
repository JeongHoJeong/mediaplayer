package com.example.jeongho.mediaplayer;

import android.graphics.Bitmap;

import java.io.File;

public class Movie extends Media {
    private Bitmap thumbnailBitmap = null;
    private boolean isDirty = false;
    private boolean isLoadingBitmap = false;

    public Movie(File file) {
        super(file);
    }

    public void setBitmap(Bitmap thumbnailBitmap) {
        isDirty = true;
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public boolean shouldBeRefreshed() {
        return isDirty;
    }

    public void onRefresh() {
        isDirty = false;
    }

    public void onStartLoadingBitmap() {
        isLoadingBitmap = true;
    }

    public boolean isLoadingBitmap() {
        return isLoadingBitmap;
    }

    public Bitmap getBitmap() { return thumbnailBitmap; }
}
