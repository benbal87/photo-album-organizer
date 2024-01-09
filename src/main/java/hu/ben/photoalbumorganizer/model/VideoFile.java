package hu.ben.photoalbumorganizer.model;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import hu.ben.photoalbumorganizer.exception.FileNotExistsException;
import hu.ben.photoalbumorganizer.exception.InvalidArgumentsException;
import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.RenameUtil;

public class VideoFile {

    private final File videoFile;

    public VideoFile(File videoFile) {
        VideoFile.validateVideoFile(videoFile);
        this.videoFile = videoFile;
    }

    private static void validateVideoFile(File videoFile) {
        if (!videoFile.exists()) {
            throw new FileNotExistsException(
                "VideoFile class instantiation failed! Video file does not exists!"
            );
        }
        if (!FileUtil.isFileVideo(videoFile)) {
            throw new InvalidArgumentsException("Provided file is not a video file!");
        }
    }

    public File getVideoFile() {
        return videoFile;
    }

    public ZonedDateTime getVideoFileCreationZonedDateTime() {
        return FileUtil.getVideoFileCreationTime(this.videoFile);
    }

    public String getVideoFileCreationDate() {
        ZonedDateTime zd = this.getVideoFileCreationZonedDateTime();
        return RenameUtil.getFormattedDateString(zd, RenameUtil.ISO_8601_DATE_FORMAT);
    }

    public String getVideoFileCreationTime() {
        ZonedDateTime zd = this.getVideoFileCreationZonedDateTime();
        return RenameUtil.getFormattedDateString(zd, RenameUtil.ISO_8601_TIME_FORMAT);
    }

    public String getVideoFileCreationDateTime() {
        ZonedDateTime zd = this.getVideoFileCreationZonedDateTime();
        return RenameUtil.getFormattedDateString(zd, RenameUtil.ISO_8601_DATE_TIME_FORMAT);
    }

    public ZoneId getVideoFileCreationTimeZone() {
        return this.getVideoFileCreationZonedDateTime().getZone();
    }

    public String getVideoFileTimeZoneId() {
        /* Returns like "Asia/Bangkok" */
        return this.getVideoFileCreationZonedDateTime().getZone().getId();
    }

    public String getVideoFileTimeZoneOffset() {
        /* Returns like "+07:00" */
        ZonedDateTime zd = this.getVideoFileCreationZonedDateTime();
        return zd.getZone().getRules().getOffset(zd.toInstant()).getId();
    }

}
