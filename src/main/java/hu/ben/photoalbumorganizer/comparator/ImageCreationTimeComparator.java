package hu.ben.photoalbumorganizer.comparator;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Comparator;

import hu.ben.photoalbumorganizer.util.FileUtil;

public class ImageCreationTimeComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        ZonedDateTime fileCreationTime1 = FileUtil.getImageFileCreationTime(o1);
        ZonedDateTime fileCreationTime2 = FileUtil.getImageFileCreationTime(o2);

        if (fileCreationTime1.isBefore(fileCreationTime2) || fileCreationTime1.isEqual(fileCreationTime2)) {
            return -1;
        } else if (fileCreationTime1.isAfter(fileCreationTime2)) {
            return 1;
        }

        return 0;
    }

}
