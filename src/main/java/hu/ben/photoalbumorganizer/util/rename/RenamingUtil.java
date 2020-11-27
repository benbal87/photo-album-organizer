package hu.ben.photoalbumorganizer.util.rename;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;

import hu.ben.photoalbumorganizer.constant.Constants;
import hu.ben.photoalbumorganizer.exception.FileRenamingException;
import hu.ben.photoalbumorganizer.model.FileWrapper;
import hu.ben.photoalbumorganizer.model.ImageFileWrapper;
import hu.ben.photoalbumorganizer.model.MediaDirectory;
import hu.ben.photoalbumorganizer.model.MediaWrapper;
import hu.ben.photoalbumorganizer.model.VideoFileWrapper;
import hu.ben.photoalbumorganizer.util.FileUtil;

public final class RenamingUtil {

    private static final String IMAGE_FILE_NAME_FORMAT_PATTERN = "{0} {1} {2}.{3}";

    private static final String VIDEO_FILE_NAME_FORMAT_PATTERN = "{0} {1} {2} {3}.{4}";

    private static final String VIDEO = "video";

    private static final String SEQUENTIAL_NUMBER_FORMAT_PATTERN = "%0{0}d";

    private static int NUMBER_OF_FILES_TO_BE_PROCESSED = 0;

    private static final MediaWrapper mediaWrapper = new MediaWrapper();

    private RenamingUtil() {
    }

    public static void renameAlbumFiles(String containerDirAbsPath) {
        getFiles(containerDirAbsPath);
        setLatestCreationDates();
        renameFiles();

        System.out.println(MessageFormat.format("{0} files renamed.", NUMBER_OF_FILES_TO_BE_PROCESSED));
    }

    private static void getFiles(String workingDirectory) {
        File[] filesInWorkingDirectory = Objects.requireNonNull(new File(workingDirectory).listFiles());

        for (File file : filesInWorkingDirectory) {
            if (!file.isDirectory()) {
                boolean isFileAnImage =
                    Constants.ALLOWED_IMAGE_FILES.contains(FilenameUtils.getExtension(file.getName()));
                boolean isFileAVideo = FileUtil.isFileVideo(file);

                if (isFileAnImage || isFileAVideo) {
                    File parentFile = file.getParentFile();
                    MediaDirectory mediaDirectory = getMediaDirectoryFromMediaWrapperIfExists(parentFile);

                    if (mediaDirectory == null) {
                        MediaDirectory newMediaDirectory = new MediaDirectory();
                        newMediaDirectory.setDirectoryFile(parentFile);

                        if (isFileAVideo) {
                            addFileToVideoWrapperList(newMediaDirectory, file);
                        } else {
                            addFileToImageWrapperList(newMediaDirectory, file);
                        }

                        mediaWrapper.getMediaDirectoryList().add(newMediaDirectory);
                    } else {
                        if (isFileAVideo) {
                            addFileToVideoWrapperList(mediaDirectory, file);
                        } else {
                            addFileToImageWrapperList(mediaDirectory, file);
                        }
                    }

                    NUMBER_OF_FILES_TO_BE_PROCESSED++;
                }
            } else {
                getFiles(file.getAbsolutePath());
            }
        }
    }

    private static void setLatestCreationDates() {
        if (!mediaWrapper.getMediaDirectoryList().isEmpty()) {
            for (MediaDirectory mediaDirectory : mediaWrapper.getMediaDirectoryList()) {
                String latestCreationDate = getLatestCreationDate(mediaDirectory);
                mediaDirectory.setLatestCreationDate(latestCreationDate);
            }
        }
    }

    private static void renameFiles() {
        ArrayList<MediaDirectory> mediaDirectoryList = mediaWrapper.getMediaDirectoryList();

        if (!mediaDirectoryList.isEmpty()) {
            for (MediaDirectory mediaDirectory : mediaDirectoryList) {
                renameImageFilesInMediaDirectory(mediaDirectory);
                renameVideoFilesInMediaDirectory(mediaDirectory);
                renameMediaDirectory(mediaDirectory);
            }
        }
    }

    private static void renameImageFilesInMediaDirectory(MediaDirectory mediaDirectory) {
        for (ImageFileWrapper imageFileWrapper : mediaDirectory.getImageFileWrapperList()) {
            SortedSet<File> imageFiles = imageFileWrapper.getImageFiles();
            renameImageOrVideoFiles(imageFileWrapper, imageFiles, mediaDirectory.getDirectoryFile().getName());
        }
    }

    private static void renameVideoFilesInMediaDirectory(MediaDirectory mediaDirectory) {
        for (VideoFileWrapper videoFileWrapper : mediaDirectory.getVideoFileWrapperList()) {
            SortedSet<File> videoFiles = videoFileWrapper.getVideoFiles();
            renameImageOrVideoFiles(videoFileWrapper, videoFiles, mediaDirectory.getDirectoryFile().getName());
        }
    }

    private static void renameImageOrVideoFiles(
        FileWrapper fileWrapper,
        SortedSet<File> imageOrVideoFiles,
        String directoryName
    ) {
        String creationDate = getFormattedDateString(fileWrapper.getFileCreationDate());
        int numberOfDigits = getNumberOfDigits(imageOrVideoFiles);

        for (File file : imageOrVideoFiles) {
            int indexOfFileInCollection = new ArrayList<>(imageOrVideoFiles).indexOf(file);
            String sequentialNumber = getFormattedSequentialNumber(indexOfFileInCollection, numberOfDigits);
            String extension = FilenameUtils.getExtension(file.getName());

            String newFileName = null;
            if (fileWrapper instanceof ImageFileWrapper) {
                newFileName =
                    MessageFormat.format(
                        IMAGE_FILE_NAME_FORMAT_PATTERN,
                        creationDate,
                        directoryName,
                        sequentialNumber,
                        extension
                    );
            } else if (fileWrapper instanceof VideoFileWrapper) {
                newFileName =
                    MessageFormat.format(
                        VIDEO_FILE_NAME_FORMAT_PATTERN,
                        creationDate,
                        directoryName,
                        VIDEO,
                        sequentialNumber,
                        extension
                    );
            }
            System.out.println("ORIGINAL_FILE_PATH: " + file.getAbsolutePath());
            System.out.println("NEW_FILE_NAME: " + newFileName);
            System.out.println("------------------------------------------------------------------------------------");

            renameFile(file.getAbsolutePath(), newFileName);
        }
    }

    private static String getFormattedSequentialNumber(int indexOfFileInCollection, int numberOfDigits) {
        String pattern = MessageFormat.format(SEQUENTIAL_NUMBER_FORMAT_PATTERN, numberOfDigits);
        return String.format(pattern, indexOfFileInCollection + 1);
    }

    private static <T> int getNumberOfDigits(SortedSet<T> list) {
        return list.size() < 1000 ? 3 : String.valueOf(list.size()).length();
    }

    private static void renameMediaDirectory(MediaDirectory mediaDirectory) {
        File directoryFile = mediaDirectory.getDirectoryFile();
        String directoryAbsolutePath = directoryFile.getAbsolutePath();
        String newFileName =
            MessageFormat.format("{0} {1}", mediaDirectory.getLatestCreationDate(), directoryFile.getName());
        renameFile(directoryAbsolutePath, newFileName);
    }

    private static void renameFile(String absoluteFilePath, String newFileName) {
        String fullPath = FilenameUtils.getFullPath(absoluteFilePath);
        boolean result = FileUtils
            .getFile(absoluteFilePath)
            .renameTo(FileUtils.getFile(fullPath + newFileName));

        if (!result) {
            throw new FileRenamingException("Renaming was unsuccessful. " + absoluteFilePath);
        }
    }

    private static void addFileToImageWrapperList(MediaDirectory mediaDirectory, File file) {
        ArrayList<ImageFileWrapper> imageFileWrapperList = mediaDirectory.getImageFileWrapperList();

        Date imageFileCreationTime = FileUtil.getImageFileCreationTime(file);
        if (imageFileWrapperList.isEmpty()) {
            ImageFileWrapper newImageFileWrapper = new ImageFileWrapper();
            newImageFileWrapper.setFileCreationDate(imageFileCreationTime);
            newImageFileWrapper.getImageFiles().add(file);
            imageFileWrapperList.add(newImageFileWrapper);
        } else {
            FileWrapper imageFileWrapper =
                getFileWrapperWithSameDayIfExists(imageFileWrapperList, imageFileCreationTime);

            if (imageFileWrapper == null) {
                ImageFileWrapper newImageFileWrapper = new ImageFileWrapper();
                newImageFileWrapper.setFileCreationDate(imageFileCreationTime);
                newImageFileWrapper.getImageFiles().add(file);
                imageFileWrapperList.add(newImageFileWrapper);
            } else {
                imageFileWrapper.getImageFiles().add(file);
            }
        }
    }

    private static void addFileToVideoWrapperList(MediaDirectory mediaDirectory, File file) {
        ArrayList<VideoFileWrapper> videoFileWrapperList = mediaDirectory.getVideoFileWrapperList();

        Date videoFileCreationTime = FileUtil.getVideoFileCreationTime(file);
        if (videoFileWrapperList.isEmpty()) {
            VideoFileWrapper newVideoFileWrapper = new VideoFileWrapper();
            newVideoFileWrapper.setFileCreationDate(videoFileCreationTime);
            newVideoFileWrapper.getVideoFiles().add(file);
            videoFileWrapperList.add(newVideoFileWrapper);
        } else {
            FileWrapper videoFileWrapper =
                getFileWrapperWithSameDayIfExists(videoFileWrapperList, videoFileCreationTime);

            if (videoFileWrapper == null) {
                VideoFileWrapper newVideoFileWrapper = new VideoFileWrapper();
                newVideoFileWrapper.setFileCreationDate(videoFileCreationTime);
                newVideoFileWrapper.getVideoFiles().add(file);
                videoFileWrapperList.add(newVideoFileWrapper);
            } else {
                videoFileWrapper.getVideoFiles().add(file);
            }
        }
    }

    private static <T extends FileWrapper> FileWrapper getFileWrapperWithSameDayIfExists(
        ArrayList<T> fileWrapperList,
        Date fileCreationTime
    ) {
        return fileWrapperList
            .stream()
            .filter(fileWrapper -> DateUtils.isSameDay(
                fileCreationTime,
                fileWrapper.getFileCreationDate()
            ))
            .findAny()
            .orElse(null);
    }

    private static MediaDirectory getMediaDirectoryFromMediaWrapperIfExists(File directory) {
        return mediaWrapper.getMediaDirectoryList()
            .stream()
            .filter(mediaDirectory -> directory.equals(mediaDirectory.getDirectoryFile()))
            .findAny()
            .orElse(null);
    }

    private static String getLatestCreationDate(MediaDirectory mediaDirectory) {
        Date latestCreationDate = null;

        ArrayList<ImageFileWrapper> imageFileWrapperList = mediaDirectory.getImageFileWrapperList();
        if (!imageFileWrapperList.isEmpty()) {
            for (ImageFileWrapper imageFileWrapper : mediaDirectory.getImageFileWrapperList()) {
                Date fileCreationDate = imageFileWrapper.getFileCreationDate();
                if (latestCreationDate == null) {
                    latestCreationDate = fileCreationDate;
                } else if (fileCreationDate.after(latestCreationDate)) {
                    latestCreationDate = fileCreationDate;
                }
            }
        }

        ArrayList<VideoFileWrapper> videoFileWrapperList = mediaDirectory.getVideoFileWrapperList();
        if (!videoFileWrapperList.isEmpty()) {
            for (VideoFileWrapper videoFileWrapper : mediaDirectory.getVideoFileWrapperList()) {
                Date fileCreationDate = videoFileWrapper.getFileCreationDate();
                if (latestCreationDate == null) {
                    latestCreationDate = fileCreationDate;
                } else if (fileCreationDate.after(latestCreationDate)) {
                    latestCreationDate = fileCreationDate;
                }
            }
        }

        String latestCreationDateString = null;
        if (latestCreationDate != null) {
            latestCreationDateString = getFormattedDateString(latestCreationDate);
        }

        return latestCreationDateString;
    }

    private static String getFormattedDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

}
