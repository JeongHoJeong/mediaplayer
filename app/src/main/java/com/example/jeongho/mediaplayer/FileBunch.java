package com.example.jeongho.mediaplayer;

import java.io.File;
import java.io.Serializable;

public class FileBunch implements Serializable {
    private static final long serialVersionUID = -4343434299L;
    private final File[] files;

    public FileBunch(File[] files) {
        this.files = files;
    }

    public File[] getFiles() {
        return files;
    }
}
