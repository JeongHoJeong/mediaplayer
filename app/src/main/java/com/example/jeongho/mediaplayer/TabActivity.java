package com.example.jeongho.mediaplayer;

import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TabActivity extends AppCompatActivity {

    PictureFragment pictureFragment;
    MovieFragment movieFragment;
    MusicFragment musicFragment;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    File[][] wholeFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        wholeFiles = null;

        new ScanFilesTask().execute();

        System.out.println(">> onCreate");

        if (savedInstanceState != null) {
            System.out.println(">> has savedInstanceState");

            int index = savedInstanceState.getInt("currentFragmentIndex", -1);
            if (index >= 0 && index <= 2) {
                viewPager.setCurrentItem(index);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println(">> onSaveInstanceState");

        int index = viewPager.getCurrentItem();
        savedInstanceState.putInt("currentFragmentIndex", index);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupViewPager(ViewPager viewpager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        pictureFragment = new PictureFragment();
        //movieFragment = new MovieFragment();
        musicFragment = new MusicFragment();

        adapter.addFragment(pictureFragment, "PICTURE");
        //adapter.addFragment(movieFragment, "MOVIE");
        adapter.addFragment(musicFragment, "MUSIC");

        viewpager.setAdapter(adapter);

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //System.out.println("onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                updateFragments();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                System.out.println("onPageScrollStateChange");
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void updateFragments() {
        if (wholeFiles != null) {
            FragmentManager fm = getSupportFragmentManager();

            if (fm != null) {
                List<Fragment> fragments = fm.getFragments();

                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        MediaFragment<?> mediaFragment = (MediaFragment<?>) fragment;
                        if (!mediaFragment.isFilesLoaded()) {
                            if (mediaFragment instanceof PictureFragment) {
                                mediaFragment.updateFiles(wholeFiles[0]);
                            } else if (mediaFragment instanceof MovieFragment) {
                                mediaFragment.updateFiles(wholeFiles[1]);
                            } else if (mediaFragment instanceof MusicFragment) {
                                mediaFragment.updateFiles(wholeFiles[2]);
                            }
                        }
                    }
                }
            }
        }
    }

    private class ScanFilesTask extends AsyncTask<Void, Void, File[][]> {
        protected File[][] doInBackground(Void... x) {
            return Util.recursiveSearch();
        }

        protected void onPostExecute(File[][] result) {
            wholeFiles = result;

            updateFragments();
        }
    }
}
