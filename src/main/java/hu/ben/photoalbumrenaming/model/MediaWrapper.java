package hu.ben.photoalbumrenaming.model;

import java.util.ArrayList;

public class MediaWrapper {

    private ArrayList<MediaDirectory> mediaDirectoryList = new ArrayList<>();

    public ArrayList<MediaDirectory> getMediaDirectoryList() {
        return mediaDirectoryList;
    }

    public void setMediaDirectoryList(ArrayList<MediaDirectory> mediaDirectoryList) {
        this.mediaDirectoryList = mediaDirectoryList;
    }

}
