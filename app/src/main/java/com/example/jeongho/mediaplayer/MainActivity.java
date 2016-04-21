package com.example.jeongho.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jeongho.mediaplayer.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    Context ctx = this;
    ActivityMainBinding binding;
    User user = new User("SomeId", "SomeName");
    ArrayList<ArrayList<File>> fileList = new ArrayList<>();

    private enum MediaType {
        PICTURE(new String[]{"jpeg", "jpg", "png"}, R.drawable.ic_photo_album_black_24dp),
        MOVIE(new String[]{"mp3", "mkv"}, R.drawable.ic_movie_black_24dp),
        MUSIC(new String[]{"mp3"}, R.drawable.ic_library_music_black_24dp);

        public final String[] extensions;
        public final String mediaName;
        public final int icon;

        MediaType(String[] extensions, int icon) {
            this.extensions = extensions;

            String[] words = this.name().split("_");
            String mediaName = "";

            for (String word : words) {
                if (mediaName.length() > 0) {
                    mediaName += " ";
                }
                mediaName += (word.substring(0, 1).toUpperCase()
                        + word.substring(1).toLowerCase());
            }

            this.mediaName = mediaName;
            this.icon = icon;
        }

        public String getName() {
            return mediaName;
        }

        public int getIcon() {
            return icon;
        }
    }

    MediaType getMediaType(String fileName) {
        String[] str = fileName.split("\\.");
        String extension = str[str.length - 1];

        for (MediaType mediaType : MediaType.values()) {
            for (String ext : mediaType.extensions) {
                if (ext.equalsIgnoreCase(extension)) {
                    return mediaType;
                }
            }
        }

        return null;
    }

    private class DirectoryAdapter extends ArrayAdapter<Directory> {
        public DirectoryAdapter(Context context, ArrayList<Directory> directories) {
            super(context, 0, directories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Directory directory = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.directory_item, parent, false);
            }

            ImageView directoryIcon = (ImageView) convertView.findViewById(R.id.directoryIcon);
            directoryIcon.setImageResource(directory.getIcon());
            directoryIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.colorPrimary));

            TextView directoryName = (TextView) convertView.findViewById(R.id.directoryName);
            TextView directoryNumContents = (TextView) convertView.findViewById(R.id.directoryNumContents);

            directoryName.setText(directory.getName());
            directoryNumContents.setText(String.valueOf(directory.getNumFiles()) + " contents");

            return convertView;
        }
    }

    Button.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.button: {
                    Intent intent = new Intent(ctx, SubActivity.class);
                    startActivity(intent);

                    /*if (binding != null) {
                        user = new User("Changed", "Hey");
                        binding.setUser(user);
                    }*/
                    break;
                }
            }
        }
    };

    /*public MyFile[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if (fileRoot.isDirectory()) {
            ArrayList<MyFile> myFileList = new ArrayList<>();
            String[] fileList = fileRoot.list();

            for (String fileName : fileList) {
                myFileList.add(new MyFile(fileName));
            }

            MyFile[] array = new MyFile[myFileList.size()];
            myFileList.toArray(array);

            return array;
        } else {
            return null;
        }
    }*/

    public void recursiveSearch() {
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            File root = Environment.getExternalStorageDirectory().getAbsoluteFile();

            Stack<File> stack = new Stack<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                File file = stack.pop();
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        stack.push(f);
                    }
                } else {
                    String fileName = file.getName();
                    MediaType mediaType = getMediaType(fileName);

                    if (mediaType != null) {
                        fileList.get(mediaType.ordinal()).add(file);
                    }
                }
            }
        } else {
            Log.e("File", "No external storage found.");
        }
    }

    public File[] getFileList(String strPath) {
        File fileRoot = new File(strPath);

        if (fileRoot.isDirectory()) {
            return fileRoot.listFiles();
        } else {
            return null;
        }
    }

    /*public boolean updateFileView(String strPath) {
        MyFile[] files = getFileList(strPath);

        if (files != null) {
            //DataBindingUtil.inflate(getLayoutInflater(), );
        } else {
            Log.e("File", "Failed to update file view.");
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");

        // DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setUser(user);

        // Click event listeners
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(onClickListener);

        for (int i = 0; i < MediaType.values().length; i++) {
            fileList.add(new ArrayList<File>());
        }

        recursiveSearch();

        ArrayList<Directory> directories = new ArrayList<>();

        int index = 0;
        for (MediaType mediaType : MediaType.values()) {
            ArrayList<File> fileArrayList = fileList.get(index);
            File[] files = new File[fileArrayList.size()];
            fileArrayList.toArray(files);
            directories.add(new Directory(mediaType.getName(), files, mediaType.getIcon()));
            index++;
        }

        DirectoryAdapter directoryAdapter = new DirectoryAdapter(this, directories);
        ListView listView = (ListView) findViewById(R.id.directoryListView);
        listView.setAdapter(directoryAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        boolean res = super.onCreateOptionsMenu(menu);

        /*if (res) {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("A");
            } else {
                System.out.println("No ActionBar");
            }
        }*/

        return res;
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        System.out.println("onRestoreInstanceState");
    }
}
