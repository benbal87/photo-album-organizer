package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import hu.ben.photoalbumorganizer.constant.Constants;

public final class FileUtil {

    public static final String CREATION_TIME_TAG_NAME = "0x0100";

    public static final int MAX_RANDOM_NUMBER = 999;

    public static final int MIN_RANDOM_NUMBER = 1;

    private FileUtil() {
    }

    public static Collection<File> getVideoFiles(String directoryLocation) {
        return FileUtils.listFiles(
            new File(directoryLocation),
            getVideoFileFilter(),
            TrueFileFilter.TRUE
        );
    }

    public static Collection<File> getImageFiles(String directoryLocation) {
        return FileUtils.listFiles(
            new File(directoryLocation),
            getImageFileFilter(),
            TrueFileFilter.TRUE
        );
    }

    public static Collection<File> getImageAndVideoFiles(String directoryLocation) {
        return FileUtils.listFiles(
            new File(directoryLocation),
            getImageAndVideoFileFilter(),
            TrueFileFilter.TRUE
        );
    }

    public static WildcardFileFilter getVideoFileFilter() {
        List<String> wildcardList = getWildCardList(Constants.ALLOWED_VIDEO_FILES);
        return new WildcardFileFilter(wildcardList);
    }

    public static WildcardFileFilter getImageFileFilter() {
        List<String> wildcardList = getWildCardList(Constants.ALLOWED_IMAGE_FILES);
        return new WildcardFileFilter(wildcardList);
    }

    public static WildcardFileFilter getImageAndVideoFileFilter() {
        List<String> videoWildcardList = getWildCardList(Constants.ALLOWED_VIDEO_FILES);
        List<String> imageWildcardList = getWildCardList(Constants.ALLOWED_IMAGE_FILES);
        videoWildcardList.addAll(imageWildcardList);
        return new WildcardFileFilter(videoWildcardList);
    }

    private static List<String> getWildCardList(List<String> allowedFileExtensions) {
        List<String> wildcardList = new ArrayList<>();
        for (String allowedVideoFile : allowedFileExtensions) {
            StringBuilder stringBuilder = new StringBuilder(allowedVideoFile);
            stringBuilder.insert(0, '*');
            String wildcard = stringBuilder.toString();
            wildcardList.add(wildcard);
        }
        return wildcardList;
    }

    public static Date getImageFileCreationTime(File file) {
        Date result = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (directory != null) {
                Date originalFileCreationTime = directory.getDateOriginal();
                if (originalFileCreationTime != null) {
                    result = correctDate(originalFileCreationTime);
                }
            }

            if (result == null) {
                Path filePath = Paths.get(file.getAbsolutePath());
                BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                FileTime creationTime = basicFileAttributes.creationTime();
                FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
                int IS_CREATION_TIME_SMALLER = -1;
                FileTime fileTime = creationTime.compareTo(lastModifiedTime) == IS_CREATION_TIME_SMALLER
                    ? creationTime
                    : lastModifiedTime;
                ZonedDateTime zd = ZonedDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
                result = Date.from(zd.toInstant());
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isFileVideo(File file) {
        return file.isFile() && Constants.ALLOWED_VIDEO_FILES.contains(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean isFileImage(File file) {
        return file.isFile() && Constants.ALLOWED_IMAGE_FILES.contains(FilenameUtils.getExtension(file.getName()));
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
                        set.add(ZoneId.systemDefault());

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

                        ZonedDateTime fileCreationZoneDateTime = ZonedDateTime.parse(tagDescription, dateTimeFormatter);
                        Date fileCreationDate = Date.from(fileCreationZoneDateTime.toInstant());
                        result = correctDate(fileCreationDate);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = getDateFromFileName(file);
        }

        return result;
    }

    public static String getDateStringFromFileName(File file) {
        return file.getName().substring(0, 10);
    }

    public static String getDateStringFromFileParentDirName(File file) {
        return file.getParentFile().getName().substring(0, 10);
    }

    public static Date getDateFromFileName(File file) {
        String fileName = file.getName();
        return getDateFromString(fileName);
    }

    public static Date getDateFromString(String fileName) {
        String yearString = fileName.substring(0, 4);
        String monthString = fileName.substring(5, 7);
        String dayString = fileName.substring(8, 10);
        int year = Integer.parseInt(yearString);
        int month = Integer.parseInt(monthString);
        int day = Integer.parseInt(dayString);

        ZonedDateTime zd = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.systemDefault());
        return Date.from(zd.toInstant());
    }

    public static Date correctDate(Date fileCreationDate) {
        long time = fileCreationDate.getTime();
        int randomNumber = generateRandomNumber();

        return new Date(time + randomNumber);
    }

    public static int generateRandomNumber() {
        return (int) ((Math.random() * ((MAX_RANDOM_NUMBER - MIN_RANDOM_NUMBER) + 1)) + MIN_RANDOM_NUMBER);
    }

}
