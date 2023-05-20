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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import hu.ben.photoalbumorganizer.constant.Constants;

public final class FileUtil {

    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    public static final String CREATION_TIME_TAG_NAME = "0x0100";

    public static final int MAX_RANDOM_NUMBER = 999;

    public static final int MIN_RANDOM_NUMBER = 1;

    private FileUtil() {
    }

    public static Collection<File> getVideoFiles(String directoryLocation) {
        return FileUtils.listFiles(new File(directoryLocation), getVideoFileFilter(), TrueFileFilter.TRUE);
    }

    public static Collection<File> getConvertedVideoFiles(String directoryLocation) {
        return FileUtils.listFiles(new File(directoryLocation), getConvertedVideoFileFilter(), TrueFileFilter.TRUE);
    }

    public static Collection<File> getImageFiles(String directoryLocation) {
        return FileUtils.listFiles(new File(directoryLocation), getImageFileFilter(), TrueFileFilter.TRUE);
    }

    public static Collection<File> getImageAndVideoFiles(String directoryLocation) {
        return FileUtils.listFiles(new File(directoryLocation), getImageAndVideoFileFilter(), TrueFileFilter.TRUE);
    }

    public static WildcardFileFilter getVideoFileFilter() {
        List<String> wildcardList = getWildCardList(Constants.ALLOWED_VIDEO_FILES, null);
        return new WildcardFileFilter(wildcardList);
    }

    public static WildcardFileFilter getConvertedVideoFileFilter() {
        List<String> wildcardList =
            getWildCardList(Constants.ALLOWED_VIDEO_FILES, "*" + Constants.VIDEO_CONVERSION_SUFFIX + ".");
        return new WildcardFileFilter(wildcardList);
    }

    public static WildcardFileFilter getImageFileFilter() {
        List<String> wildcardList = getWildCardList(Constants.ALLOWED_IMAGE_FILES, null);
        return new WildcardFileFilter(wildcardList);
    }

    public static WildcardFileFilter getImageAndVideoFileFilter() {
        List<String> videoWildcardList = getWildCardList(Constants.ALLOWED_VIDEO_FILES, null);
        List<String> imageWildcardList = getWildCardList(Constants.ALLOWED_IMAGE_FILES, null);
        videoWildcardList.addAll(imageWildcardList);
        return new WildcardFileFilter(videoWildcardList);
    }

    private static List<String> getWildCardList(List<String> allowedFileExtensions, String prefix) {
        String prefixCorrected = prefix == null ? "*." : prefix;
        return allowedFileExtensions
            .stream()
            .map(ext -> prefixCorrected + ext)
            .collect(Collectors.toList());
    }

    public static boolean isFileVideo(File file) {
        return file != null
               && file.isFile()
               && Constants.ALLOWED_VIDEO_FILES.contains(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean isFileImage(File file) {
        return file != null
               && file.isFile()
               && Constants.ALLOWED_IMAGE_FILES.contains(FilenameUtils.getExtension(file.getName()));
    }

    public static ZonedDateTime getImageFileCreationTime(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date originalFileCreationTime = directory.getDateOriginal();
                if (originalFileCreationTime != null) {
                    return ZonedDateTime.ofInstant(originalFileCreationTime.toInstant(), ZoneId.systemDefault());
                }
            }

            Path filePath = Paths.get(file.getAbsolutePath());
            BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileTime creationTime = basicFileAttributes.creationTime();
            FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
            int IS_CREATION_TIME_SMALLER = -1;
            FileTime fileTime = creationTime.compareTo(lastModifiedTime) == IS_CREATION_TIME_SMALLER
                ? creationTime
                : lastModifiedTime;
            return ZonedDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        } catch (ImageProcessingException | IOException e) {
            return getFileCreationTimeFromFileName(file, e);
        }
    }

    public static ZonedDateTime getVideoFileCreationTime(File file) {
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

                        return ZonedDateTime.parse(tagDescription, dateTimeFormatter);
                    }
                }
            }
        } catch (Exception e) {
            return getFileCreationTimeFromFileName(file, e);
        }

        return getFileCreationTimeFromFileName(file, null);
    }

    private static ZonedDateTime getFileCreationTimeFromFileName(File file, Exception e) {
        String path = file.getAbsolutePath();
        logger.warn("Can not get file creation time from video metadata. " + path);
        if (e != null) {
            logger.warn(e.toString());
        }
        logger.info("Trying to get file creation time from file name. " + path);
        ZonedDateTime dateFromFileName = RenameUtil.getDateFromFileName(file);
        if (dateFromFileName == null) {
            logger.warn("Did not succeed to retrieve date from file name: " + path);
        }
        return dateFromFileName;
    }

//    public static Date correctDate(Date fileCreationDate) {
//        long time = fileCreationDate.getTime();
//        int randomNumber = generateRandomNumber();
//
//        return new Date(time + randomNumber);
//    }

//    public static int generateRandomNumber() {
//        return (int) ((Math.random() * ((MAX_RANDOM_NUMBER - MIN_RANDOM_NUMBER) + 1)) + MIN_RANDOM_NUMBER);
//    }

}
