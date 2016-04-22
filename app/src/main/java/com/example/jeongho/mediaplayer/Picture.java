package com.example.jeongho.mediaplayer;

import android.graphics.Bitmap;

import java.io.File;

public class Picture extends Media {
    private Bitmap bitmap = null;

    public Picture(File file) {
        super(file);
    }

    public void setBitmap(Bitmap bitmap) {
        setLoaded();
        this.bitmap = bitmap;
    }
    public Bitmap getBitmap() { return bitmap; }
}