package hu.ben.photoalbumrenaming.model;

import java.io.File;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.ben.photoalbumrenaming.comparator.VideoCreationTimeComparator;

public class VideoFileWrapper {

    private Date fileCreationDate;

    private SortedSet<File> videoFiles = new TreeSet<>(new VideoCreationTimeComparator());

    public Date getFileCreationDate() {
        return fileCreationDate;
    }

    public void setFileCreationDate(Date fileCreationDate) {
        this.fileCreationDate = fileCreationDate;
    }

    public SortedSet<File> getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(SortedSet<File> videoFiles) {
        this.videoFiles = videoFiles;
    }

}
