package com.example.jeongho.mediaplayer;

import java.io.File;

/**
 * Created by jeongho on 4/23/16.
 */
public class Media {
    private boolean isLoaded = false;
    private boolean isLoading = false;
    private final File file;
    private Util.AnimationStatus animationStatus = Util.AnimationStatus.NOT_STARTED;
    private boolean hasError = false;

    public Media(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setHasError() {
        hasError = true;
    }

    public void setLoading() {
        isLoading = true;
        isLoaded = false;
    }
    protected void setLoaded() {
        isLoading = false;
        isLoaded = true;
    }
    public boolean isLoaded() { return isLoaded; }
    public boolean isLoading() { return isLoading; }
    public boolean shouldPlayFadeInAnimation() { return isLoaded && animationStatus == Util.AnimationStatus.NOT_STARTED; }
    public void setFadeInAnimationDone() { animationStatus = Util.AnimationStatus.DONE; }
    public boolean hasError() { return hasError; }
}
