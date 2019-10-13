package hu.ben.photoalbumrenaming.comparator;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class VideoCreationTimeComparator implements Comparator<File> {

    private static final String CREATION_TIME_TAG_NAME = "0x0100";

    @Override
    public int compare(File o1, File o2) {
        ZonedDateTime fileCreationTime1 = getFileCreationTime(o1);
        ZonedDateTime fileCreationTime2 = getFileCreationTime(o2);

        if (fileCreationTime1.isBefore(fileCreationTime2)) {
            return -1;
        } else if (fileCreationTime1.isAfter(fileCreationTime2)) {
            return 1;
        }

        return 0;
    }

    private ZonedDateTime getFileCreationTime(File file) {
        ZonedDateTime fileCreationTime = null;

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
                            // create formatter (using English locale to make sure it parses weekday and month names correctly)
                            .toFormatter(Locale.ENGLISH);

                        fileCreationTime = ZonedDateTime.parse(tagDescription, dateTimeFormatter);
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return fileCreationTime;
    }

}
