package hu.ben.photoalbumrenaming.model;

import java.io.File;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.ben.photoalbumrenaming.comparator.ImageCreationTimeComparator;

public class ImageFileWrapper {

    private Date fileCreationDate;

    private SortedSet<File> imageFiles = new TreeSet<>(new ImageCreationTimeComparator());

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

}
