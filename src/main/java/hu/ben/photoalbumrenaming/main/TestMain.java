package hu.ben.photoalbumrenaming.main;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class TestMain {

    public static void main(String[] args) {

        String location1 = "C:/vid.mp4";
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(location1));
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    boolean isCreationDateTag = tag.getTagTypeHex().equals("0x0100");

                    if (isCreationDateTag) {
                        String tagDescription = tag.getDescription();
                        System.out.println(tagDescription);
                    }
                }
            }

        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        Set<ZoneId> set = new HashSet<>();
        set.add(ZoneId.of("Europe/Budapest"));

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
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


        String time = "Sun Oct 06 18:20:35 CEST 2019";
        ZonedDateTime z1 = ZonedDateTime.parse(time, fmt);
        System.out.println(z1);

    }

}
