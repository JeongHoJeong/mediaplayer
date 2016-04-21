package com.example.jeongho.mediaplayer;

import java.io.File;

/**
 * Created by jeongho on 4/22/16.
 */
public class Directory {
    final String name;
    final File[] files;
    final int icon;

    public Directory(String name, File[] files, int icon)
    {
        this.name = name;
        this.files = files;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }
    public int getNumFiles() { return files.length; }
    public int getIcon() { return icon; }
}
