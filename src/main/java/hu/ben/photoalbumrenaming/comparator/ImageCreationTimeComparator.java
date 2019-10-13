package hu.ben.photoalbumrenaming.comparator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImageCreationTimeComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        Date fileCreationTime1 = getFileCreationTime(o1);
        Date fileCreationTime2 = getFileCreationTime(o2);

        if (fileCreationTime1.before(fileCreationTime2)) {
            return -1;
        } else if (fileCreationTime1.after(fileCreationTime2)) {
            return 1;
        }

        return 0;
    }

    private Date getFileCreationTime(File file) {
        Date fileCreationTime = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            fileCreationTime = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return fileCreationTime;
    }

}
