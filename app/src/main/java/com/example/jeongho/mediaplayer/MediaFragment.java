package com.example.jeongho.mediaplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;


public abstract class MediaFragment <T extends Media> extends Fragment {
    protected ArrayList<T> medias = new ArrayList<>();
    private boolean filesLoaded = false;

    public MediaFragment() {
    }

    /* Lifecycle Functions */

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);

        onCreateChildView(inflater, (ViewGroup) view.findViewById(R.id.mediaViewGroup), savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    /* File View */

    public void updateFiles(File[] files) {
        updateFilesArray(files);

        filesLoaded = true;
        updateView();
    }

    private void updateView() {
        if (filesLoaded) {
            View view = getView();

            if (view != null) {
                ViewGroup viewGroup = getContainer();
                if (viewGroup.getChildCount() == 0) {
                    View child = getChildView();
                    view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    viewGroup.addView(child);
                }
            }
        }
    }

    protected ViewGroup getContainer() {
        View view = getView();

        if (view != null) {
            return (ViewGroup) view.findViewById(R.id.mediaViewGroup);
        } else {
            return null;
        }
    }

    public boolean isFilesLoaded() {
        return filesLoaded;
    }

    protected abstract void updateFilesArray(File[] files);
    protected abstract View getChildView();
    protected abstract void onCreateChildView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
}
