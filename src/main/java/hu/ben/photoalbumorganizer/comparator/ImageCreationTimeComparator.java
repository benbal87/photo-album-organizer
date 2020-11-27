package hu.ben.photoalbumorganizer.comparator;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

import hu.ben.photoalbumorganizer.util.FileUtil;

public class ImageCreationTimeComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        Date fileCreationTime1 = FileUtil.getImageFileCreationTime(o1);
        Date fileCreationTime2 = FileUtil.getImageFileCreationTime(o2);

        if (fileCreationTime1.before(fileCreationTime2)) {
            return -1;
        } else if (fileCreationTime1.after(fileCreationTime2)) {
            return 1;
        }

        return 0;
    }

}
