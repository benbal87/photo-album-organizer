package hu.ben.photoalbumrenaming.comparator;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

import hu.ben.photoalbumrenaming.util.PhotoAlbumRenamingUtil;

public class ImageCreationTimeComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        Date fileCreationTime1 = PhotoAlbumRenamingUtil.getImageFileCreationTime(o1);
        Date fileCreationTime2 = PhotoAlbumRenamingUtil.getImageFileCreationTime(o2);

        if (fileCreationTime1.before(fileCreationTime2)) {
            return -1;
        } else if (fileCreationTime1.after(fileCreationTime2)) {
            return 1;
        }

        return 0;
    }

}
