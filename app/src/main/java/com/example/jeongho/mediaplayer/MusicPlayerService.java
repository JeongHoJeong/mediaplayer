package com.example.jeongho.mediaplayer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class MusicPlayerService extends Service {
    private enum STATE {
        STOP,
        PAUSE,
        LOADING,
        PLAY
    }

    final Object lock = new Object();
    final Object errorLock = new Object();

    final String logTag = "MusicPlayerService";

    MediaPlayer mediaPlayer = null;

    boolean doesCounterThreadExist = false;
    long currentCounterThreadId = 0;

    Integer currentPosition = null;
    STATE state = STATE.STOP;
    String currentPath = null;
    boolean pendingPlay = false;
    boolean isWaitingFor = false;
    Boolean hasError = false;
    String errorMessage = null;
    long waitingFor = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // In case when system restarts the service, intent is null
        if (intent == null) {
            Log.v(logTag, "Restarted by Android.");
        } else {
            Log.v(logTag, "onStartCommand");
            String command = intent.getStringExtra("command");

            if (command != null) {
                if (command.contentEquals("play")) {
                    String path = intent.getStringExtra("path");

                    if (path != null) {
                        boolean isSameAsPreviousPath;

                        synchronized (lock) { isSameAsPreviousPath = (currentPath != null && currentPath.contentEquals(path)); }

                        if (isSameAsPreviousPath) {
                            switch (state) {
                                case PLAY:
                                    // Just keep playing without any change
                                    break;
                                case PAUSE:
                                    Log.v(logTag, "Resume");
                                    resume();
                                    break;
                                default:
                                    Log.v(logTag, "Play");
                                    play(path);
                                    break;
                            }
                        } else {
                            Log.v(logTag, "Play");
                            play(path);
                        }
                    }
                } else if (command.contentEquals("pause")) {
                    Log.v(logTag, "Pause");
                    pause();
                } else if (command.contentEquals("seek")) {
                    Log.v(logTag, "Seek");

                    int newPosition = intent.getIntExtra("position", -1);

                    if (newPosition >= 0) {
                        seek(newPosition);
                    }
                }
            }
        }

        if (!doesCounterThreadExist) {
            SyncManager syncManager = new SyncManager();
            currentCounterThreadId = syncManager.getId();
            doesCounterThreadExist = true;
            syncManager.start();
        }

        broadcastState();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(logTag, "MusicPlayerService destroyed.");

        synchronized (lock) {
            if (mediaPlayer != null) {
                changeState(STATE.STOP);
                mediaPlayer.release();
                mediaPlayer = null;
            }

            doesCounterThreadExist = false;
        }
    }

    public void play(final String path) {
        synchronized (lock) {
            currentPath = path;
            pendingPlay = true;

            MediaLoader thread = new MediaLoader(path);
            waitingFor = thread.getId();
            isWaitingFor = true;

            changeState(STATE.LOADING);

            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable e) {
                    System.out.println("!!!Exception");
                }
            });
            thread.start();
        }
    }

    public void pause() {
        synchronized (lock) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                currentPosition = mediaPlayer.getCurrentPosition();
                changeState(STATE.PAUSE);
            }
        }
    }

    public void seek(int position) {
        synchronized (lock) {
            if (mediaPlayer != null) {
                System.out.printf("seekTo: %d\n", position);
                //mediaPlayer.seekTo(position * 1000);
                switch (state) {
                    case PLAY:
                        mediaPlayer.seekTo(position * 1000);
                        break;
                    case PAUSE:
                        currentPosition = position * 1000;
                        break;
                }
            }
        }
    }

    public void resume() {
        synchronized (lock) {
            if (mediaPlayer != null) {
                if (currentPosition != null) {
                    mediaPlayer.seekTo(currentPosition);
                }
                mediaPlayer.start();
                changeState(STATE.PLAY);
            }
        }
    }

    private void updateMediaPlayer(MediaPlayer mp, long id) {
        synchronized (lock) {
            if (isWaitingFor && id == waitingFor) {
                if (mediaPlayer != null) {
                    changeState(STATE.STOP);
                    mediaPlayer.release();
                }

                mediaPlayer = mp;
                isWaitingFor = false;

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mp != null) {
                            synchronized (lock) {
                                mp.seekTo(0);
                                currentPosition = 0;
                                mediaPlayer.pause();
                                changeState(STATE.PAUSE);

                                Intent intent = new Intent(getResources().getString(R.string.update_player_state));

                                intent.putExtra("hasNewPosition", true);
                                intent.putExtra("position", 0);

                                sendBroadcast(intent);
                            }
                        }
                    }
                });

                if (pendingPlay) {
                    pendingPlay = false;
                    mediaPlayer.start();
                    changeState(STATE.PLAY);
                }
            }
        }
    }

    private void syncTime() {
        boolean didGetPosition = false;
        long currentPosition = 0;

        synchronized (lock) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                didGetPosition = true;
            }
        }

        if (didGetPosition) {
            Intent intent = new Intent(getResources().getString(R.string.update_player_state));

            intent.putExtra("hasNewPosition", true);
            intent.putExtra("position", currentPosition);

            sendBroadcast(intent);
        }
    }

    // Thread-safe
    private void changeState(STATE newState) {
        state = newState;

        broadcastState();
    }

    // Thread-safe
    private void broadcastState() {
        Intent intent = new Intent(getResources().getString(R.string.update_player_state));
        intent.putExtra("state", state.toString());
        if (hasError) {
            intent.putExtra("error", errorMessage);
        }

        sendBroadcast(intent);
    }

    private void setError(boolean error, String errorMsg) {
        synchronized (errorLock) {
            hasError = error;
            this.errorMessage = errorMsg;
        }

        broadcastState();
    }

    private boolean hasError() {
        synchronized (errorLock) {
            return hasError;
        }
    }

    /***********
       THREADS
     ***********/

    private class MediaLoader extends Thread {
        final String path;

        public MediaLoader(String path) {
            super();
            this.path = path;
        }

        public void run() {
            MediaPlayer mp = null;

            try {
                mp = new MediaPlayer();
                mp.setDataSource(path);
                mp.prepare();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to initialize MediaPlayer: %s", e.getMessage()));
                mp = null;
            } finally {
                if (mp != null) {
                    updateMediaPlayer(mp, getId());
                    setError(false, "");
                } else {
                    Log.e(logTag, String.format("Failed to load media: %s", path));
                    setError(true, "Failed to load media file.");
                }
            }
        }
    }

    private class SyncManager extends Thread {
        public void run() {
            while (true) {
                if (!doesCounterThreadExist || getId() != currentCounterThreadId) break;

                android.os.SystemClock.sleep(1000);
                syncTime();
            }
        }
    }
}
