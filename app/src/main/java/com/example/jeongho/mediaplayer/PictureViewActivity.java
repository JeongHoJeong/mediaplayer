package com.example.jeongho.mediaplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;

public class PictureViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        Intent intent = getIntent();
        new ImageLoadTask().execute(intent.getStringExtra("path"));
    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        public Bitmap doInBackground(String... params) {
            File imgFile = new File(params[0]);

            if (imgFile.exists()) {
                return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            } else {
                return null;
            }
        }

        public void onPostExecute(Bitmap bitmap) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            ImageView imageView = (ImageView) findViewById(R.id.picture);

            imageView.setImageBitmap(bitmap);

            Animation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(500);

            imageView.startAnimation(animation);
        }
    }
}
