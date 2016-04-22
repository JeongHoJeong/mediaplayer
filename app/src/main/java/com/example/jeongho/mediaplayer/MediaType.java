package com.example.jeongho.mediaplayer;

public enum MediaType {
    PICTURE(new String[]{"jpeg", "jpg", "png"}, R.drawable.ic_photo_album_black_24dp),
    MOVIE(new String[]{"mp4", "mkv"}, R.drawable.ic_movie_black_24dp),
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

    static MediaType getMediaType(String fileName) {
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
}
