package hu.ben.photoalbumrenaming.util;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class PhotoAlbumRenamingUtil {

    private static final String CREATION_TIME_TAG_NAME = "0x0100";

    public static Date getImageFileCreationTime(File file) {
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

    public static Date getVideoFileCreationTime(File file) {
        Date result = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    boolean isCreationTimeTag = tag.getTagTypeHex().equals(CREATION_TIME_TAG_NAME);

                    if (isCreationTimeTag) {
                        String tagDescription = tag.getDescription();

                        Set<ZoneId> set = new HashSet<>();
                        set.add(ZoneId.of("Europe/Budapest"));

                        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                            // your pattern (weekday, month, day, hour/minute/second)
                            .appendPattern("EE MMM dd HH:mm:ss ")
                            // optional timezone short name (like "CST" or "CEST")
                            .optionalStart().appendZoneText(TextStyle.SHORT, set).optionalEnd()
                            // optional GMT offset (like "GMT+02:00")
                            .optionalStart().appendPattern("OOOO").optionalEnd()
                            // year
                            .appendPattern(" yyyy")
                            // create formatter (using English locale to make sure it parses weekday and month names
                            // correctly)
                            .toFormatter(Locale.ENGLISH);

                        ZonedDateTime fileCreationTime = ZonedDateTime.parse(tagDescription, dateTimeFormatter);
                        result = Date.from(fileCreationTime.toInstant());
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
