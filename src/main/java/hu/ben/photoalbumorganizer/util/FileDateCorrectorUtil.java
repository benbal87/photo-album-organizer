package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.constant.Constants;

public final class FileDateCorrectorUtil {

    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    public static final String EXIFTOOL_DATE_CORRECTION_CMD_WITH_UTF8_CHARSET =
        "exiftool -charset FileName=UTF8 -overwrite_original {0} \"{1}\"";

    public static final String EXIFTOOL_DATE_CORRECTION_CMD_WITH_LATIN_CHARSET =
        "exiftool -charset FileName=Latin -overwrite_original {0} \"{1}\"";

    public static final String[] METADATA = new String[] {
        "-FileCreateDate=\"{0} 12:00:00\"",
        "-CreateDate=\"{0} 12:00:00\"",
        "-FileModifyDate=\"{0} 12:00:00\"",
        "-ModifyDate=\"{0} 12:00:00\"",
        "-xmp:CreateDate=\"{0}\"",
        "-xmp:MetadataDate=\"{0}\"",
        "-xmp:ModifyDate=\"{0}\"",
        "-xmp:HistoryWhen=\"{0} 12:00:00\"",
        "-exif:DateTimeOriginal=\"{0} 12:00:00\"",
        "-exif:CreateDate=\"{0} 12:00:00\""
    };

    private FileDateCorrectorUtil() {
    }

    // TODO: decide if method should be deleted of provide a usage
    public static void correctDatesOfConvertedVideoFiles(String workDir) {
        logger.info("Starting to correct converted video file dates in directory: " + workDir);
        Collection<File> convertedVideoFiles = FileUtil.getConvertedVideoFiles(workDir);
        for (File file : convertedVideoFiles) {
            modifyConvertedVideoFileDates(file, null);
        }
    }

    public static void modifyConvertedVideoFileDates(File convertedVideoFile, File originalVideoFile) {
        if (convertedVideoFile.exists()) {
            String date = getVideoFileCreationDateString(convertedVideoFile, originalVideoFile);
            setFileDates(convertedVideoFile, date);
        } else {
            logger.warn("Can not correct date in video file. Converted video file doesn't exist! "
                        + convertedVideoFile.getAbsolutePath());
        }
    }

    public static void setFileDatesBasedOnFileName(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            Collection<File> files = FileUtil.getImageAndVideoFiles(dir);
            if (!files.isEmpty()) {
                for (File file : files) {
                    String date = RenameUtil.getDateStringFromFileName(file);
                    setFileDates(file, date);
                }
            }
        }
    }

    public static void setFileDatesBasedOnDateInParentDirName(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            Collection<File> files = FileUtil.getImageAndVideoFiles(dir);
            if (!files.isEmpty()) {
                for (File file : files) {
                    String date = RenameUtil.getDateStringFromFileParentDirName(file);
                    setFileDates(file, date);
                }
            }
        }
    }

    public static void setFileDates(File file, String date) {
        logger.info(LogUtil.getSeparator() + "Attempting to change dates in file metadata: " + file.getAbsolutePath());
        boolean result = handleDateChangingInFileMetadata(file, date);
        if (result) {
            logger.info("Metadata modification was successful in file: " + file.getAbsolutePath());
        } else {
            logger.info("Metadata modification was failed in file: " + file.getAbsolutePath());
        }
    }

    public static boolean handleDateChangingInFileMetadata(File file, String date) {
        boolean result = true;
        String ext = FilenameUtils.getExtension(file.getName());
        if (Constants.ALLOWED_VIDEO_FILES.contains(ext) || Constants.ALLOWED_IMAGE_FILES.contains(ext)) {
            result = setDatesInFileMetadata(file, date, EXIFTOOL_DATE_CORRECTION_CMD_WITH_LATIN_CHARSET);
            if (!result) {
                logger.warn("Date set with latin charset failed in file: "
                            + file.getAbsolutePath()
                            + " -> Trying it with workaround now...");
                result = setDatesInFileMetadataWithFileMovingWorkaround(file, date);
            }
        } else {
            logger.warn("File is not allowed to be processed. Extension not allowed. File: "
                        + file.getAbsolutePath() + " | "
                        + Constants.ALLOWED_FILE_EXTENSIONS_INFO);
        }
        return result;
    }

    public static boolean setDatesInFileMetadataWithFileMovingWorkaround(File file, String date) {
        boolean result;
        // WORKAROUND
        // It is necessary because unfortunately the exiftool would not handle special characters in
        // filenames so temporarily the file has to be moved back to root dir and renamed to a simplified name
        // until this issue has been solved
        File tmpFile = moveFileWithSimplifiedNameToRoot(file);
        if (tmpFile != null && tmpFile.exists()) {
            result = setDatesInFileMetadata(tmpFile, date, EXIFTOOL_DATE_CORRECTION_CMD_WITH_UTF8_CHARSET);
            if (!result) {
                logger.error("Not able to set metadata for file: " + file.getAbsolutePath());
            }
        } else {
            logger.error("Not able to create temporary file to change metadata.");
            result = false;
        }
        moveFileBackToItsOriginalLocation(tmpFile, file);
        return result;
    }

    public static boolean setDatesInFileMetadata(File file, String date, String exiftoolCmd) {
        try {
            String dateCorrected = date.replaceAll("-", ":");
            String tmpFilePath = file.getAbsolutePath();
            List<String> metadata = Arrays
                .stream(METADATA).map(m -> MessageFormat.format(m, dateCorrected)).collect(Collectors.toList());
            String metadataJoined = String.join(" ", metadata);
            String cmd = MessageFormat.format(exiftoolCmd, metadataJoined, tmpFilePath);
            logger.info("Attempting to run command: " + cmd);
            CommandLine cmdLine = CommandLine.parse(cmd);
            DefaultExecutor executor = new DefaultExecutor();
            int execute = executor.execute(cmdLine);
            logger.info("Command execution result: " + execute);
            return true;
        } catch (IOException e) {
            logger.error("Failed to run command.");
            logger.error(e.toString());
        }
        return false;
    }

    private static void moveFileBackToItsOriginalLocation(File tmpFile, File originalFile) {
        try {
            FileUtils.moveFile(tmpFile, originalFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Moving file to its original location was unsuccessful. File: "
                                       + originalFile.getAbsolutePath());
        }
    }

    private static File moveFileWithSimplifiedNameToRoot(File file) {
        File tempMovedFile = null;
        try {
            String extension = FilenameUtils.getExtension(file.getName());
            String rootDirPath = Paths.get(file.getAbsolutePath()).getRoot().toString();
            String tempFileAbsolutePath = MessageFormat.format("{0}temp-file.{1}", rootDirPath, extension);
            tempMovedFile = new File(tempFileAbsolutePath);
            if (tempMovedFile.exists()) {
                boolean delete = tempMovedFile.delete();
                if (delete) {
                    logger.info("Previously created temp moved file detected and deleted.");
                } else {
                    throw new RuntimeException("Previously created temp moved file detected BUT can not be deleted.");
                }
            }
            FileUtils.moveFile(file, tempMovedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempMovedFile;
    }

    private static String getVideoFileCreationDateString(File convertedFile, File originalFile) {
        if (FileUtil.isFileVideo(originalFile)) {
            ZonedDateTime fileCreationDate = FileUtil.getVideoFileCreationTime(originalFile);
            return RenameUtil.getFormattedDateString(fileCreationDate);
        }
        return RenameUtil.getDateStringFromFileName(convertedFile);
    }

}
