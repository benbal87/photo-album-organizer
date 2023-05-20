package hu.ben.photoalbumorganizer.organizer.rename;

import java.io.File;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.constant.Constants;
import hu.ben.photoalbumorganizer.exception.FileRenamingException;
import hu.ben.photoalbumorganizer.model.FileWrapper;
import hu.ben.photoalbumorganizer.model.ImageFileWrapper;
import hu.ben.photoalbumorganizer.model.MediaDirectory;
import hu.ben.photoalbumorganizer.model.MediaWrapper;
import hu.ben.photoalbumorganizer.model.VideoFileWrapper;
import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.LogUtil;
import hu.ben.photoalbumorganizer.util.RenameUtil;

public class Rename {

    private static final Logger logger = LogManager.getLogger(Rename.class);

    private static final String IMAGE_FILE_NAME_FORMAT_PATTERN = "{0} {1} {2}.{3}";

    private static final String VIDEO_FILE_NAME_FORMAT_PATTERN = "{0} {1} {2} {3}.{4}";

    private static final String VIDEO = "video";

    public static final String SEQUENTIAL_NUMBER_FORMAT_PATTERN = "%0{0}d";

    private final MediaWrapper mediaWrapper;

    public Rename() {
        this.mediaWrapper = new MediaWrapper();
    }

    public Rename(MediaWrapper mediaWrapper) {
        this.mediaWrapper = mediaWrapper;
    }

    public MediaWrapper getMediaWrapper() {
        return mediaWrapper;
    }

    public void renameAlbumFiles(String workDir) {
        logger.info(LogUtil.getSeparator(3) + "Starting to rename album files in directory: " + workDir);
        collectFiles(workDir, this.mediaWrapper);
        setLatestCreationDates(this.mediaWrapper);
        renameFiles(this.mediaWrapper);
        logger.info(LogUtil.getSeparator() + "Renaming of album files finished!");
    }

    public MediaWrapper collectFiles(String workDir, MediaWrapper mw) {
        logger.info(LogUtil.getSeparator() + "Started to collecting files in directory: " + workDir);
        getFiles(workDir, mw);
        logger.info("Collecting files finished in directory: " + workDir);
        logger.info("Number of image and video files collected: " + mw.getNumberOfFiles());
        return mw;
    }

    private static void getFiles(String workDir, MediaWrapper mw) {
        File[] filesInWorkingDirectory = new File(workDir).listFiles();
        if (filesInWorkingDirectory != null) {
            for (File file : filesInWorkingDirectory) {
                if (!file.isDirectory()) {
                    addFileToWrapperList(file, mw);
                } else if (file.isDirectory()) {
                    logger.info("Starting to collect files in directory: " + file.getAbsolutePath());
                    getFiles(file.getAbsolutePath(), mw);
                }
            }
        }
    }

    public MediaWrapper setLatestCreationDates(MediaWrapper mw) {
        if (!mw.getMediaDirectoryList().isEmpty()) {
            for (MediaDirectory mediaDirectory : mw.getMediaDirectoryList()) {
                String latestCreationDate = getLatestCreationDate(mediaDirectory);
                mediaDirectory.setLatestCreationDate(latestCreationDate);
            }
        }
        return mw;
    }

    public MediaWrapper renameFiles(MediaWrapper mw) {
        ArrayList<MediaDirectory> mediaDirectoryList = mw.getMediaDirectoryList();
        if (!mediaDirectoryList.isEmpty()) {
            for (MediaDirectory mediaDirectory : mediaDirectoryList) {
                renameImageFilesInMediaDirectory(mediaDirectory);
                renameVideoFilesInMediaDirectory(mediaDirectory);
                renameMediaDirectory(mediaDirectory);
            }
        }
        return mw;
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
        String creationDate = RenameUtil.getFormattedDateString(fileWrapper.getFileCreationDate());
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
            renameFile(file, newFileName);
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
        String newFileName =
            MessageFormat.format("{0} {1}", mediaDirectory.getLatestCreationDate(), directoryFile.getName());
        renameFile(directoryFile, newFileName);
    }

    public static void renameFile(File originalFile, String newFileName) {
        String absoluteFilePath = originalFile.getAbsolutePath();
        String newFileAbsPath = FilenameUtils.getFullPath(absoluteFilePath) + newFileName;
        logger.info(
            LogUtil.getSeparator()
            + "Attempting to rename file: " + absoluteFilePath + "\n"
            + "New file: " + newFileAbsPath
        );
        File newFile = FileUtils.getFile(newFileAbsPath);
        boolean result = originalFile.renameTo(newFile);
        if (result) {
            logger.info("File renaming was successful!");
        } else {
            logger.error(
                MessageFormat.format(
                    "Moving file failed!\n From file: {0}\n To file: {1}",
                    originalFile.getAbsolutePath(),
                    newFile.getAbsolutePath()
                )
            );
            throw new FileRenamingException("Renaming was unsuccessful. " + absoluteFilePath);
        }
    }

    private static void addFileToWrapperList(File file, MediaWrapper mw) {
        boolean isFileImage = FileUtil.isFileImage(file);
        boolean isFileVideo = FileUtil.isFileVideo(file);

        if ((isFileImage || isFileVideo) && !RenameForIphone.isFileFromIphone(file)) {
            File parentFile = file.getParentFile();
            MediaDirectory mediaDirectory = getMediaDirectoryFromMediaWrapperIfExists(parentFile, mw);
            if (mediaDirectory == null) {
                MediaDirectory newMediaDirectory = new MediaDirectory();
                newMediaDirectory.setDirectoryFile(parentFile);
                addFileToMediaDirectory(file, newMediaDirectory);
                mw.getMediaDirectoryList().add(newMediaDirectory);
            } else {
                addFileToMediaDirectory(file, mediaDirectory);
            }
        } else {
            logger.info("File is not considered to be video or image. Excluding file from collection: "
                        + file.getAbsolutePath());
        }
    }

    private static void addFileToMediaDirectory(File file, MediaDirectory md) {
        boolean isFileImage = FileUtil.isFileImage(file);
        boolean isFileVideo = FileUtil.isFileVideo(file);
        if (isFileVideo) {
            addFileToVideoWrapperList(md, file);
        } else if (isFileImage) {
            addFileToImageWrapperList(md, file);
        } else {
            logger.error("File can not be added to media directory! File is not considered to be video or image. "
                         + file.getAbsolutePath()
                         + Constants.ALLOWED_FILE_EXTENSIONS_INFO + " ");
        }
    }

    private static void addFileToImageWrapperList(MediaDirectory mediaDirectory, File file) {
        logger.info("Adding file to image file wrapper list: " + file.getAbsolutePath());
        ArrayList<ImageFileWrapper> imageFileWrapperList = mediaDirectory.getImageFileWrapperList();

        ZonedDateTime imageFileCreationTime = FileUtil.getImageFileCreationTime(file);
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
        logger.info("Adding file to video file wrapper list: " + file.getAbsolutePath());
        ArrayList<VideoFileWrapper> videoFileWrapperList = mediaDirectory.getVideoFileWrapperList();

        ZonedDateTime videoFileCreationTime = FileUtil.getVideoFileCreationTime(file);
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
        ZonedDateTime fileCreationTime
    ) {
        return fileWrapperList
            .stream()
            .filter(fileWrapper -> fileCreationTime.isEqual(fileWrapper.getFileCreationDate()))
            .findAny()
            .orElse(null);
    }

    private static MediaDirectory getMediaDirectoryFromMediaWrapperIfExists(File directory, MediaWrapper mw) {
        return mw.getMediaDirectoryList()
            .stream()
            .filter(mediaDirectory -> directory.equals(mediaDirectory.getDirectoryFile()))
            .findAny()
            .orElse(null);
    }

    private static String getLatestCreationDate(MediaDirectory mediaDirectory) {
        ZonedDateTime latestCreationDate = null;

        ArrayList<ImageFileWrapper> imageFileWrapperList = mediaDirectory.getImageFileWrapperList();
        if (!imageFileWrapperList.isEmpty()) {
            for (ImageFileWrapper imageFileWrapper : mediaDirectory.getImageFileWrapperList()) {
                ZonedDateTime fileCreationDate = imageFileWrapper.getFileCreationDate();
                if (latestCreationDate == null) {
                    latestCreationDate = fileCreationDate;
                } else if (fileCreationDate.isAfter(latestCreationDate)
                           || fileCreationDate.isEqual(latestCreationDate)) {
                    latestCreationDate = fileCreationDate;
                }
            }
        }

        ArrayList<VideoFileWrapper> videoFileWrapperList = mediaDirectory.getVideoFileWrapperList();
        if (!videoFileWrapperList.isEmpty()) {
            for (VideoFileWrapper videoFileWrapper : mediaDirectory.getVideoFileWrapperList()) {
                ZonedDateTime fileCreationDate = videoFileWrapper.getFileCreationDate();
                if (latestCreationDate == null) {
                    latestCreationDate = fileCreationDate;
                } else if (fileCreationDate.isAfter(latestCreationDate)
                           || fileCreationDate.isEqual(latestCreationDate)) {
                    latestCreationDate = fileCreationDate;
                }
            }
        }

        String latestCreationDateString = null;
        if (latestCreationDate != null) {
            latestCreationDateString = RenameUtil.getFormattedDateString(latestCreationDate);
        }

        return latestCreationDateString;
    }

}
