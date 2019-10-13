package hu.ben.photoalbumrenaming.model;

import java.io.File;
import java.util.ArrayList;

public class MediaDirectory {

    private File directoryFile;

    private String latestCreationDate;

    private ArrayList<ImageFileWrapper> imageFileWrapperList = new ArrayList<>();

    private ArrayList<VideoFileWrapper> videoFileWrapperList = new ArrayList<>();

    public File getDirectoryFile() {
        return directoryFile;
    }

    public void setDirectoryFile(File directoryFile) {
        this.directoryFile = directoryFile;
    }

    public String getLatestCreationDate() {
        return latestCreationDate;
    }

    public void setLatestCreationDate(String latestCreationDate) {
        this.latestCreationDate = latestCreationDate;
    }

    public ArrayList<ImageFileWrapper> getImageFileWrapperList() {
        return imageFileWrapperList;
    }

    public void setImageFileWrapperList(ArrayList<ImageFileWrapper> imageFileWrapperList) {
        this.imageFileWrapperList = imageFileWrapperList;
    }

    public ArrayList<VideoFileWrapper> getVideoFileWrapperList() {
        return videoFileWrapperList;
    }

    public void setVideoFileWrapperList(ArrayList<VideoFileWrapper> videoFileWrapperList) {
        this.videoFileWrapperList = videoFileWrapperList;
    }

}
