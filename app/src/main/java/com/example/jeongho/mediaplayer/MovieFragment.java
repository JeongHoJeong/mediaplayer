package com.example.jeongho.mediaplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MovieFragment extends MediaFragment<Movie> {

    MovieAdapter adapter;
    View view;

    @Override
    protected void onCreateChildView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_movie, container, false);

        ListView pictureList = (ListView) view.findViewById(R.id.movieListView);
        adapter = new MovieAdapter(getContext(), medias);

        pictureList.setAdapter(adapter);
    }

    @Override
    protected void updateFilesArray(File[] files) {
        for (File file : files) {
            medias.add(new Movie(file));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected View getChildView() {
        return view;
    }

    private class MovieAdapter extends ArrayAdapter<Movie> {
        public MovieAdapter(Context context, ArrayList<Movie> movies) {
            super(context, 0, movies);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Movie movie = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_item, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(R.id.movieTitle);
            tv.setText(movie.getFile().getName());

            return convertView;
        }
    }
}
