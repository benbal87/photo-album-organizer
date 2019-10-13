package hu.ben.photoalbumrenaming.rename;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;

import hu.ben.photoalbumrenaming.constant.Constants;
import hu.ben.photoalbumrenaming.model.ImageFileWrapper;
import hu.ben.photoalbumrenaming.model.MediaDirectory;
import hu.ben.photoalbumrenaming.model.MediaWrapper;
import hu.ben.photoalbumrenaming.model.VideoFileWrapper;
import hu.ben.photoalbumrenaming.util.PhotoAlbumRenamingUtil;

public class PhotoAlbumRenaming {

    private static int NUMBER_OF_FILES_TO_BE_PROCESSED = 0;

    private MediaWrapper mediaWrapper = new MediaWrapper();

    public void renameAlbumFiles(String location) {
        getFiles(location);
        setLatestCreationDates();

        System.out.println(MessageFormat.format("{0} files renamed.", NUMBER_OF_FILES_TO_BE_PROCESSED));
    }

    private void getFiles(String workingDirectory) {
        File[] filesInWorkingDirectory = Objects.requireNonNull(new File(workingDirectory).listFiles());

        for (File file : filesInWorkingDirectory) {
            if (!file.isDirectory()) {
                boolean isFileAnImage =
                    Constants.ALLOWED_IMAGE_FILES.contains(FilenameUtils.getExtension(file.getName()));
                boolean isFileAVideo =
                    Constants.ALLOWED_VIDEO_FILES.contains(FilenameUtils.getExtension(file.getName()));

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

    private void setLatestCreationDates() {
        if (!mediaWrapper.getMediaDirectoryList().isEmpty()) {
            for (MediaDirectory mediaDirectory : mediaWrapper.getMediaDirectoryList()) {
                String latestCreationDate = getLatestCreationDate(mediaDirectory);
                mediaDirectory.setLatestCreationDate(latestCreationDate);
            }
        }
    }

    private void addFileToImageWrapperList(MediaDirectory mediaDirectory, File file) {
        ArrayList<ImageFileWrapper> imageFileWrapperList = mediaDirectory.getImageFileWrapperList();

        Date imageFileCreationTime = PhotoAlbumRenamingUtil.getImageFileCreationTime(file);
        if (imageFileWrapperList.isEmpty()) {
            ImageFileWrapper newImageFileWrapper = new ImageFileWrapper();
            newImageFileWrapper.setFileCreationDate(imageFileCreationTime);
            newImageFileWrapper.getImageFiles().add(file);
        } else {
            ImageFileWrapper imageFileWrapper =
                getImageFileWrapperWithSameDayIfExists(imageFileWrapperList, imageFileCreationTime);

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

    private void addFileToVideoWrapperList(MediaDirectory mediaDirectory, File file) {
        ArrayList<VideoFileWrapper> videoFileWrapperList = mediaDirectory.getVideoFileWrapperList();

        Date videoFileCreationTime = PhotoAlbumRenamingUtil.getVideoFileCreationTime(file);
        if (videoFileWrapperList.isEmpty()) {
            VideoFileWrapper newVideoFileWrapper = new VideoFileWrapper();
            newVideoFileWrapper.setFileCreationDate(videoFileCreationTime);
            newVideoFileWrapper.getVideoFiles().add(file);
        } else {
            VideoFileWrapper videoFileWrapper =
                getVideoFileWrapperWithSameDayIfExists(videoFileWrapperList, videoFileCreationTime);

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

    private ImageFileWrapper getImageFileWrapperWithSameDayIfExists(
        ArrayList<ImageFileWrapper> imageFileWrapperList,
        Date imageFileCreationTime
    ) {
        return imageFileWrapperList
            .stream()
            .filter(imageFileWrapper -> DateUtils.isSameDay(
                imageFileCreationTime,
                imageFileWrapper.getFileCreationDate()
            ))
            .findAny()
            .orElse(null);
    }

    private VideoFileWrapper getVideoFileWrapperWithSameDayIfExists(
        ArrayList<VideoFileWrapper> videoFileWrapperList,
        Date videoFileCreationTime
    ) {
        return videoFileWrapperList
            .stream()
            .filter(videoFileWrapper -> DateUtils.isSameDay(
                videoFileCreationTime,
                videoFileWrapper.getFileCreationDate()
            ))
            .findAny()
            .orElse(null);
    }

    private MediaDirectory getMediaDirectoryFromMediaWrapperIfExists(File directory) {
        return mediaWrapper.getMediaDirectoryList()
            .stream()
            .filter(mediaDirectory -> directory.equals(mediaDirectory.getDirectoryFile()))
            .findAny()
            .orElse(null);
    }

    private String getLatestCreationDate(MediaDirectory mediaDirectory) {
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            latestCreationDateString = formatter.format(latestCreationDate);
        }

        return latestCreationDateString;
    }

}
