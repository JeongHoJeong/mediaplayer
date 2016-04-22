package com.example.jeongho.mediaplayer;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;


public class PictureFragment extends MediaFragment <Picture> {

    PictureAdapter adapter;
    View view;
    Integer scrollPosition = null;
    Integer scrollY = null;

    @Override
    protected void onCreateChildView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_picture, container, false);

        GridView pictureList = (GridView) view.findViewById(R.id.pictureGridView);
        adapter = new PictureAdapter(getContext(), medias);

        pictureList.setAdapter(adapter);
    }

    @Override
    protected void updateFilesArray(File[] files) {
        for (File file : files) {
            medias.add(new Picture(file));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected View getChildView() {
        return view;
    }

    private class PictureAdapter extends ArrayAdapter<Picture> {
        public PictureAdapter(Context context, ArrayList<Picture> medias) {
            super(context, 0, medias);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Picture picture = getItem(position);
            final Context ctx = getContext();

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.picture_item, parent, false);
                /*convertView.findViewById(R.id.pictureThumbnail).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(ctx, PictureViewActivity.class);
                        intent.putExtra("path", picture.getFile().getAbsolutePath());
                        startActivity(intent);
                    }
                });*/
            }

            convertView.findViewById(R.id.pictureThumbnail).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(ctx, PictureViewActivity.class);
                    intent.putExtra("path", picture.getFile().getAbsolutePath());
                    startActivity(intent);
                }
            });

            Bitmap bitmap = picture.getBitmap();
            if (!picture.isLoaded() && !picture.isLoading()) {
                picture.setLoading();
                new ImageLoadTask().execute(new ImageLoadTaskParam(position));
            } else {
                ImageView imageView = (ImageView) convertView.findViewById(R.id.pictureThumbnail);
                imageView.setImageBitmap(bitmap);

                if (picture.shouldPlayFadeInAnimation()) {
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(400);
                    imageView.startAnimation(animation);
                    picture.setFadeInAnimationDone();
                }
            }

            if (picture.isLoaded()) {
                convertView.setVisibility(View.VISIBLE);
            } else {
                convertView.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println(">> onResume");

        GridView gridView = (GridView) view.findViewById(R.id.pictureGridView);

        if (gridView != null && scrollPosition != null) {
            //gridView.scrollBy(0, scrollPosition);
            //gridView.scrollTo(0, scrollPosition);
            gridView.setSelection(scrollPosition);
        } else {
            System.out.println(">> scrollPosition Null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println(">> onPause");

        GridView gridView = (GridView) view.findViewById(R.id.pictureGridView);

        if (gridView != null) {
            //int position = gridView.getVerticalScrollbarPosition();
            int position = gridView.getFirstVisiblePosition();
            System.out.printf(">> Position: %d\n", position);
            scrollPosition = position;
        }
    }
/*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);
    }*/

    private class ImageLoadTaskParam {
        public int index;

        ImageLoadTaskParam(int index) {
            this.index = index;
        }
    }

    private class ImageLoadTask extends AsyncTask<ImageLoadTaskParam, Void, Bitmap> {
        int pos;

        public Bitmap doInBackground(ImageLoadTaskParam... params) {
            ImageLoadTaskParam param = params[0];

            pos = param.index;

            View view = getView();

            if (view == null) {
                return null;
            }

            Picture picture = medias.get(param.index);
            File file = picture.getFile();
            String path = file.getPath();
            File imgFile = new File(path);

            if (imgFile.exists()) {
                final int maxSize = 240;

                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                final int width = bitmap.getWidth();
                final int height = bitmap.getHeight();

                if (width > maxSize || height > maxSize) {
                    float aspectRatio = (float) height / (float) width;
                    return Bitmap.createScaledBitmap(bitmap, maxSize, (int) (aspectRatio * maxSize), false);
                }
                return bitmap;
            } else {
                return null;
            }
        }

        public void onPostExecute(Bitmap bitmap) {
            medias.get(pos).setBitmap(bitmap);
            adapter.notifyDataSetChanged();
        }
    }
}