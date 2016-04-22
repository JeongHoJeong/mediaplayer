package com.example.jeongho.mediaplayer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

public class Util {
    static public File[][] recursiveSearch() {
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            File root = Environment.getExternalStorageDirectory().getAbsoluteFile();

            Stack<File> stack = new Stack<>();
            stack.push(root);

            ArrayList<ArrayList<File>> fileList = new ArrayList<>();
            for (MediaType mediaType : MediaType.values()) {
                fileList.add(new ArrayList<File>());
            }

            while (!stack.isEmpty()) {
                File file = stack.pop();
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        stack.push(f);
                    }
                } else {
                    String fileName = file.getName();
                    MediaType mediaType = MediaType.getMediaType(fileName);

                    if (mediaType != null) {
                        fileList.get(mediaType.ordinal()).add(file);
                    }
                }
            }

            File[][] ret = new File[fileList.size()][];
            for (int i = 0; i < fileList.size(); i++) {
                ArrayList<File> files = fileList.get(i);
                ret[i] = new File[files.size()];
                files.toArray(ret[i]);
            }

            return ret;
        } else {
            Log.e("File", "No external storage found.");
        }

        return null;
    }

    static String getTimeString(Long duration) {
        Long minutes = duration / 60;
        Long seconds = duration % 60;

        String secondsString = String.valueOf(seconds);
        if (seconds < 10) {
            secondsString = "0" + secondsString;
        }

        return String.valueOf(minutes) + ":" + secondsString;
    }

    public enum AnimationStatus {
        NOT_STARTED,
        DONE
    }
}
