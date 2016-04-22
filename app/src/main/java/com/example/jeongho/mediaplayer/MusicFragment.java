package com.example.jeongho.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MusicFragment extends MediaFragment<Music> {

    final String logTag = "MusicFragment";
    MusicAdapter adapter;
    View view;
    File[] files;

    @Override
    protected void onCreateChildView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_music, container, false);

        ListView pictureList = (ListView) view.findViewById(R.id.musicListView);
        adapter = new MusicAdapter(getContext(), medias);

        pictureList.setAdapter(adapter);
    }

    @Override
    protected void updateFilesArray(File[] files) {
        this.files = files;

        for (File file : files) {
            medias.add(new Music(file));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected View getChildView() {
        return view;
    }

    private class MusicAdapter extends ArrayAdapter<Music> {
        public MusicAdapter(Context context, ArrayList<Music> musics) {
            super(context, 0, musics);
        }

        @Override
        public View getView(int position, View givenConvertView, ViewGroup parent) {
            final Music music = getItem(position);
            final Context ctx = getContext();

            System.out.printf("getView: %s\n", music.getFile().getAbsolutePath());

            if (givenConvertView == null) {
                givenConvertView = LayoutInflater.from(getContext()).inflate(R.layout.music_item, parent, false);
            }

            final View convertView = givenConvertView;

            if (!music.isLoaded() && !music.isLoading()) {
                music.setLoading();
                new MetadataLoadTask().execute(position);
            } else {
                TextView tv = (TextView) convertView.findViewById(R.id.musicTitle);
                tv.setText(music.getFile().getName());

                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (!music.hasError()) {
                            Intent intent = new Intent(ctx, MusicPlayer.class);
                            intent.putExtra("path", music.getFile().getAbsolutePath());
                            intent.putExtra("files", new FileBunch(files));
                            startActivity(intent);
                        } else {
                            Toast.makeText(ctx, "You cannot play this due to some error.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            /* Bitmap */
                Bitmap bitmap = music.getBitmap();
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.musicThumbnail);
                if (bitmap != null) {
                    thumbnail.setImageBitmap(bitmap);
                } else {
                    thumbnail.setImageResource(R.drawable.ic_album_black_24dp);
                }

            /* Title */
                String title = music.getTitle();

                if (title == null) {
                    title = music.getFile().getName();
                }

                TextView titleView = (TextView) convertView.findViewById(R.id.musicTitle);
                titleView.setText(title);

            /* Artist */
                String artist = music.getArtist();
                TextView artistView = (TextView) convertView.findViewById(R.id.musicArtist);

                if (artist == null) {
                    artist = "";
                }

                artistView.setText(artist);

            /* Duration */
                Long duration = music.getDuration();
                TextView durationView = (TextView) convertView.findViewById(R.id.musicDuration);

                if (duration != null) {
                    durationView.setText(Util.getTimeString(duration));
                } else {
                    durationView.setText("Unknown");
                }

                if (music.isLoaded()) {
                    convertView.setVisibility(View.VISIBLE);
                } else {
                    convertView.setVisibility(View.INVISIBLE);
                }
            }

            /* First time loaded: fade in animation */
            if (music.shouldPlayFadeInAnimation()) {
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(300);
                convertView.startAnimation(animation);
                music.setFadeInAnimationDone();
            }

            return convertView;
        }
    }

    private class MetadataLoadResult {
        public final String title, albumTitle, artist;
        public final Bitmap bitmap;
        public final long duration;

        public MetadataLoadResult(String title, String albumTitle, String artist, Bitmap bitmap, long duration) {
            this.title = title;
            this.albumTitle = albumTitle;
            this.artist = artist;
            this.bitmap = bitmap;
            this.duration = duration;
        }
    }

    private class MetadataLoadTask extends AsyncTask<Integer, Void, MetadataLoadResult> {
        Integer pos = null;

        protected MetadataLoadResult doInBackground(Integer... params) {
            pos = params[0];

            if (pos == null) return null;

            File file = medias.get(pos).getFile();

            if (file.exists()) {
                String path = file.getAbsolutePath();

                Log.v(logTag, String.format("File exists: %s. Start loading.", path));

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(path);

                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String albumTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String strDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                Bitmap bitmap = null;
                Long duration = 0L;

                if (strDuration != null) duration = Long.parseLong(strDuration) / 1000;

                byte[] thumbnailBinary = mmr.getEmbeddedPicture();

                if (thumbnailBinary != null) {
                    bitmap = BitmapFactory.decodeByteArray(thumbnailBinary, 0, thumbnailBinary.length);
                }

                return new MetadataLoadResult(title, albumTitle, artist, bitmap, duration);
            }

            return null;
        }

        protected void onPostExecute(MetadataLoadResult result) {
            if (result == null) {
                Log.e(logTag, "File load failed.");
                return;
            }

            Music music = medias.get(pos);
            if (music != null) {
                music.setTitle(result.title);
                music.setArtist(result.artist);
                music.setBitmap(result.bitmap);
                music.setDuration(result.duration);
                music.setLoaded();

                if (result.duration <= 0) {
                    music.setHasError();
                }

                adapter.notifyDataSetChanged();
            }
        }
    }
}
