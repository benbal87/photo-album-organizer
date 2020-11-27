package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;
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
import hu.ben.photoalbumorganizer.exception.FileDeletionException;
import hu.ben.photoalbumorganizer.exception.FileRenamingException;

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

    public static WildcardFileFilter getVideoFileFilter() {
        List<String> wildcardList = new ArrayList<>();
        for (String allowedVideoFile : Constants.ALLOWED_VIDEO_FILES) {
            StringBuilder stringBuilder = new StringBuilder(allowedVideoFile);
            stringBuilder.insert(0, '*');
            String wildcard = stringBuilder.toString();
            wildcardList.add(wildcard);
        }
        return new WildcardFileFilter(wildcardList);
    }

    public static Date getImageFileCreationTime(File file) {
        Date result = null;

        try {
            System.out.println(file.getAbsolutePath());
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (directory != null) {
                Date originalFileCreationTime = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                result = correctDate(originalFileCreationTime);
            } else {
                Path filePath = Paths.get(file.getAbsolutePath());
                BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                FileTime fileTime = basicFileAttributes.creationTime();
                result = new Date(fileTime.toMillis());
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isFileVideo(File file) {
        return file.isFile() && Constants.ALLOWED_VIDEO_FILES.contains(FilenameUtils.getExtension(file.getName()));
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

                        ZonedDateTime fileCreationZoneDateTime = ZonedDateTime.parse(tagDescription, dateTimeFormatter);
                        Date fileCreationDate = Date.from(fileCreationZoneDateTime.toInstant());
                        result = correctDate(fileCreationDate);
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Date correctDate(Date fileCreationDate) {
        long time = fileCreationDate.getTime();
        int randomNumber = generateRandomNumber();

        return new Date(time + randomNumber);
    }

    public static int generateRandomNumber() {
        return (int) ((Math.random() * ((MAX_RANDOM_NUMBER - MIN_RANDOM_NUMBER) + 1)) + MIN_RANDOM_NUMBER);
    }

    public File moveFile(File file, String destination) {
        File movedFile;
        try {
            String fileName = file.getName();
            String destinationAbsolutePath = destination + fileName;
            movedFile = new File(destinationAbsolutePath);
            FileUtils.moveFile(file, movedFile);
        } catch (IOException e) {
            throw new FileRenamingException(MessageFormat.format(
                "File moving failed! \"{0}\" --> to destination: \"{1}\"",
                file.getAbsolutePath(),
                destination
            ));
        }
        return movedFile;
    }

    public static File renameFile(File file, String modifyName) {
        File fileWithModifiedName;
        if (file.getName().equals(modifyName)) {
            fileWithModifiedName = file;
        } else {
            try {
                String filePath = FilenameUtils.getFullPath(file.getAbsolutePath());
                String newAbsolutePath = filePath + modifyName;
                fileWithModifiedName = new File(newAbsolutePath);
                FileUtils.moveFile(file, fileWithModifiedName);
            } catch (IOException e) {
                throw new FileRenamingException(MessageFormat.format(
                    "File reaming failed! \"{0}\" --> to file name: \"{1}\"",
                    file.getAbsolutePath(),
                    modifyName
                ));
            }
        }
        return fileWithModifiedName;
    }

    public void deleteFile(File file) {
        String absolutePath = file.getAbsolutePath();
        boolean deleteResult = file.delete();
        if (deleteResult) {
            System.out.println("File deleted! " + absolutePath);
        } else {
            throw new FileDeletionException("Can not delete file: "
                                            + absolutePath);
        }
    }

}
