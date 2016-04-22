package com.example.jeongho.mediaplayer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;

public class MusicPlayer extends AppCompatActivity {

    final Context ctx = this;
    Bitmap bitmap = null;

    Long duration;
    //String path;
    boolean hasError = false;
    String errorMsg;

    final String logTag = "MusicPlayer";
    private PlayerUpdateReceiver playerUpdateReceiver;
    String state = null;
    File[] files = null;

    Integer currentPosition = null;

    enum SeekBarState {
        NATURAL,
        PRESSED
    }

    SeekBarState seekBarState = SeekBarState.NATURAL;

    /* Utility Function */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        FileBunch fileBunch = (FileBunch) intent.getSerializableExtra("files");
        files = fileBunch.getFiles();
        Integer newPosition = null;

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getAbsolutePath().contentEquals(path)) {
                    newPosition = i;
                    break;
                }
            }
        }

        playMusic(newPosition, null);

        ImageView dots = (ImageView) findViewById(R.id.horizontalDots);
        dots.setColorFilter(ContextCompat.getColor(ctx, R.color.colorPrimaryDark));

        setupListeners();
    }

    private void playMusic(Integer position, String direction) {
        if (position == null || position < 0 || position >= files.length) {
            Log.e(logTag, "Wrong file. Stop loading.");

            hasError = true;
            errorMsg = "Couldn't find such file.";
        } else {
            currentPosition = position;

            new MetadataLoadTask().execute(direction);
            Intent serviceIntent = new Intent(this, MusicPlayerService.class);
            serviceIntent.putExtra("command", "play");
            serviceIntent.putExtra("path", files[position].getAbsolutePath());
            startService(serviceIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerUpdateReceiver == null) {
            playerUpdateReceiver = new PlayerUpdateReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.update_player_state));
        registerReceiver(playerUpdateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerUpdateReceiver != null) {
            unregisterReceiver(playerUpdateReceiver);
        }
    }

    private void setupListeners() {
        ImageView playBtn = (ImageView) findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != null) {
                    Intent intent = new Intent(ctx, MusicPlayerService.class);

                    if (state.equals("PAUSE") || state.equals("STOP")) {
                        intent.putExtra("command", "play");
                        intent.putExtra("path", files[currentPosition].getAbsolutePath());
                    } else if (state.equals("PLAY")) {
                        intent.putExtra("command", "pause");
                        intent.putExtra("path", files[currentPosition].getAbsolutePath());
                    }

                    ctx.startService(intent);
                }
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.musicSeekBar);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        seekBarState = SeekBarState.PRESSED;
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL: {
                        seekBarState = SeekBarState.NATURAL;

                        int newProgress = ((SeekBar) v).getProgress();

                        Intent serviceIntent = new Intent(ctx, MusicPlayerService.class);
                        serviceIntent.putExtra("command", "seek");
                        serviceIntent.putExtra("position", newProgress);
                        ctx.startService(serviceIntent);

                        break;
                    }
                    case android.view.MotionEvent.ACTION_MOVE: {
                        long newProgress = ((SeekBar) v).getProgress();

                        TextView musicCurrentPostion = (TextView) findViewById(R.id.musicCurrentPosition);
                        musicCurrentPostion.setText(Util.getTimeString(newProgress));

                        break;
                    }
                }

                return false;
            }
        });

        ImageView prevBtn = (ImageView) findViewById(R.id.prevBtn);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer newPosition = currentPosition - 1;
                if (newPosition < 0) {
                    newPosition = files.length - 1;
                }

                playMusic(newPosition, "TO_LEFT");
            }
        });

        ImageView nextBtn = (ImageView) findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer newPosition = (currentPosition + 1) % files.length;

                playMusic(newPosition, "TO_RIGHT");
            }
        });
    }

    private class MetadataLoadResult {
        public final String title, albumTitle, artist;
        public final Bitmap bitmap;
        public final long duration;
        public final String direction;

        public MetadataLoadResult(String title, String albumTitle, String artist, Bitmap bitmap, long duration, String direction) {
            this.title = title;
            this.albumTitle = albumTitle;
            this.artist = artist;
            this.bitmap = bitmap;
            this.duration = duration;
            this.direction = direction;
        }

        public boolean isToLeft() {
            return direction != null && direction.contentEquals("TO_LEFT");
        }

        public boolean isToRight() {
            return direction != null && direction.contentEquals("TO_RIGHT");
        }
    }

    private class MetadataLoadTask extends AsyncTask<String, Void, MetadataLoadResult> {
        protected MetadataLoadResult doInBackground(String... params) {
            String path = files[currentPosition].getAbsolutePath();

            File file = new File(path);

            if (file.exists()) {
                Log.v(logTag, String.format("File exists: %s. Start loading.", path));

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(path);

                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String albumTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String strDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                Long duration = null;

                if (strDuration != null) {
                    duration = Long.parseLong(strDuration) / 1000;
                } else {
                    duration = 0L;
                }

                byte[] thumbnailBinary = mmr.getEmbeddedPicture();

                if (thumbnailBinary != null) {
                    bitmap = BitmapFactory.decodeByteArray(thumbnailBinary, 0, thumbnailBinary.length);
                }

                return new MetadataLoadResult(title, albumTitle, artist, bitmap, duration, (params != null && params[0] != null) ? params[0] : null);
            } else {
                Log.v(logTag, String.format("File doesn't exist: %s. Stop loading.", path));
            }

            return null;
        }

        protected void onPostExecute(MetadataLoadResult result) {
            if (result == null) {
                Log.e(logTag, "File load failed.");
                return;
            }

            duration = result.duration;

            Log.v(logTag, "File load complete.");

            TextView titleTv = (TextView) findViewById(R.id.musicTitleInPlayer);
            TextView albumTv = (TextView) findViewById(R.id.musicAlbumInPlayer);
            TextView artistTv = (TextView) findViewById(R.id.musicArtistInPlayer);

            titleTv.setText(result.title != null ? result.title : "Unknown Title");
            albumTv.setText(result.albumTitle != null ? result.albumTitle : "Unknown Album");
            artistTv.setText(result.artist != null ? result.artist : "Unknown Artist");

            if (duration != null && duration > 0) {
                TextView durationView = (TextView) findViewById(R.id.musicDuration);
                durationView.setText(Util.getTimeString(duration));
            }

            if (result.isToLeft()) {
                Animation inAnimation = AnimationUtils.loadAnimation(ctx, R.anim.left_to_center);
                Animation outAnimation = AnimationUtils.loadAnimation(ctx, R.anim.center_to_right);

                ImageView oldThumbnail = (ImageView) findViewById(R.id.musicThumbnailInPlayerPrevImage);
                ImageView newThumbnail = (ImageView) findViewById(R.id.musicThumbnailInPlayer);

                if (bitmap != null) {
                    oldThumbnail.setImageBitmap(bitmap);
                }

                oldThumbnail.startAnimation(outAnimation);
                newThumbnail.startAnimation(inAnimation);

                newThumbnail.setImageBitmap(result.bitmap);
            } else if (result.isToRight()) {
                Animation inAnimation = AnimationUtils.loadAnimation(ctx, R.anim.right_to_center);
                Animation outAnimation = AnimationUtils.loadAnimation(ctx, R.anim.center_to_left);

                ImageView oldThumbnail = (ImageView) findViewById(R.id.musicThumbnailInPlayerPrevImage);
                ImageView newThumbnail = (ImageView) findViewById(R.id.musicThumbnailInPlayer);

                if (bitmap != null) {
                    oldThumbnail.setImageBitmap(bitmap);
                }

                oldThumbnail.startAnimation(outAnimation);
                newThumbnail.startAnimation(inAnimation);

                newThumbnail.setImageBitmap(result.bitmap);
            } else {
                ImageView thumbnail = (ImageView) findViewById(R.id.musicThumbnailInPlayer);

                if (result.bitmap != null) {
                    thumbnail.setImageBitmap(result.bitmap);
                } else {
                    // Any blank CD image
                    thumbnail.setImageResource(R.drawable.ic_album_black_24dp);
                    thumbnail.setColorFilter(R.color.colorPrimary);
                }

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(250);
                thumbnail.startAnimation(animation);
            }

            RelativeLayout loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
            loadingPanel.setVisibility(View.GONE);
        }
    }

    private Long min(Long x, Long y) {
        if (x < y) {
            return x;
        } else {
            return y;
        }
    }

    private class PlayerUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.update_player_state))) {
                String newState = intent.getStringExtra("state");
                String error = intent.getStringExtra("error");

                if (error == null) {
                    boolean hasNewPosition = intent.getBooleanExtra("hasNewPosition", false);
                    if (hasNewPosition && !(seekBarState == SeekBarState.PRESSED)) {
                        Long newPosition = intent.getLongExtra("position", 0) / 1000;

                        if (duration != null && duration > 0) {
                            int intNewPosition = min(newPosition, duration).intValue();
                            int intMax = duration.intValue();

                            SeekBar seekBar = (SeekBar) findViewById(R.id.musicSeekBar);

                            // Just because of the Android bug... :(
                            seekBar.setMax(intMax);
                            seekBar.setProgress(intNewPosition);
                            seekBar.setProgress(0);
                            seekBar.setMax(intMax);
                            seekBar.setProgress(intNewPosition);

                            TextView musicCurrentPostion = (TextView) findViewById(R.id.musicCurrentPosition);
                            musicCurrentPostion.setText(Util.getTimeString(newPosition));
                        }
                    }

                    if (newState != null) {
                        state = newState;

                        Log.v(logTag, "Player status update: " + newState);

                        if (newState.equals("PLAY")) {
                            ImageView playBtn = (ImageView) findViewById(R.id.playBtn);
                            playBtn.setImageResource(R.drawable.ic_pause_white_48dp);
                        } else if (newState.equals("PAUSE") || newState.equals("STOP")) {
                            ImageView playBtn = (ImageView) findViewById(R.id.playBtn);
                            playBtn.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        }
                    }

                    TextView errorMsg = (TextView) findViewById(R.id.musicPlayerErrorMsg);
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    TextView errorMsg = (TextView) findViewById(R.id.musicPlayerErrorMsg);
                    errorMsg.setText(error);
                }
            }
        }
    }
}
