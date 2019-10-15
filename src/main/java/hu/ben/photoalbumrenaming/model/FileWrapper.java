package hu.ben.photoalbumrenaming.model;

import java.io.File;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.ben.photoalbumrenaming.comparator.ImageCreationTimeComparator;
import hu.ben.photoalbumrenaming.comparator.VideoCreationTimeComparator;

public class FileWrapper {

    private Date fileCreationDate;

    private SortedSet<File> imageFiles = new TreeSet<>(new ImageCreationTimeComparator());

    private SortedSet<File> videoFiles = new TreeSet<>(new VideoCreationTimeComparator());

    public Date getFileCreationDate() {
        return fileCreationDate;
    }

    public void setFileCreationDate(Date fileCreationDate) {
        this.fileCreationDate = fileCreationDate;
    }

    public SortedSet<File> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(SortedSet<File> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public SortedSet<File> getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(SortedSet<File> videoFiles) {
        this.videoFiles = videoFiles;
    }

}
