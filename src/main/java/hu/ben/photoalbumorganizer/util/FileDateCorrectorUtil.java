package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.MessageFormat;
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
import hu.ben.photoalbumorganizer.model.VideoFile;

public final class FileDateCorrectorUtil {

    private static final Logger logger = LogManager.getLogger(FileDateCorrectorUtil.class);

    public static final String EXIFTOOL_DATE_CORRECTION_CMD_WITH_UTF8_CHARSET =
        "exiftool -m -charset FileName=UTF8 -overwrite_original {0} \"{1}\"";

    public static final String EXIFTOOL_DATE_CORRECTION_CMD_WITH_LATIN_CHARSET =
        "exiftool -m -charset FileName=Latin -overwrite_original {0} \"{1}\"";

    public static final String[] METADATA = new String[] {
        "-FileCreateDate=\"{0} {1}\"",
        "-CreateDate=\"{0} {1}\"",
        "-FileModifyDate=\"{0} {1}\"",
        "-ModifyDate=\"{0} {1}\"",
        "-xmp:CreateDate=\"{0}\"",
        "-xmp:MetadataDate=\"{0}\"",
        "-xmp:ModifyDate=\"{0}\"",
        "-xmp:HistoryWhen=\"{0} {1}\"",
        "-exif:DateTimeOriginal=\"{0} {1}\"",
        "-exif:CreateDate=\"{0} {1}\""
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
            VideoFile vf = new VideoFile(originalVideoFile);
            String date = vf.getVideoFileCreationDateTime();
            String time = vf.getVideoFileCreationTime();
            setFileDates(convertedVideoFile, date, time);
        } else {
            logger.warn("Can not correct date in video file. Converted video file doesn't exist! "
                        + convertedVideoFile.getAbsolutePath());
        }
    }

    public static void setFileDatesBasedOnFileName(String dir) {
        logger.info(LogUtil.getSeparator(3) + "Starting to set file dates based on file names in directory: " + dir);
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            Collection<File> files = FileUtil.getImageAndVideoFiles(dir);
            int numberOfFiles = files.size();
            logger.info("Number of files to be processed: " + numberOfFiles);
            if (!files.isEmpty()) {
                int processedFiles = 0;
                for (File file : files) {
                    String date = RenameUtil.getDateStringFromFileName(file);
                    setFileDates(file, date, "");
                    processedFiles++;
                    String progress = new DecimalFormat("0.00").format(((double) processedFiles / numberOfFiles) * 100);
                    logger.info(MessageFormat.format(
                        "Metadata correction progress: {0}/{1} | {2}%",
                        processedFiles,
                        numberOfFiles,
                        progress
                    ));
                }
            }
        }
    }

    private static void setFileDatesBasedOnDateInParentDirName(String dir) {
        logger.info(LogUtil.getSeparator(3)
                    + "Starting to set file dates based on album names in directory: "
                    + dir);
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            Collection<File> files = FileUtil.getImageAndVideoFiles(dir);
            if (!files.isEmpty()) {
                for (File file : files) {
                    String date = RenameUtil.getDateStringFromFileParentDirName(file);
                    setFileDates(file, date, "");
                }
            }
        }
    }

    private static void setFileDates(File file, String date, String time) {
        logger.info(LogUtil.getSeparator() + "Attempting to change dates in file metadata: " + file.getAbsolutePath());
        boolean result = date != null
                         && time != null
                         && (FileUtil.isFileVideo(file) || FileUtil.isFileImage(file))
                         && handleDateChangingInFileMetadata(file, date, time);
        if (result) {
            logger.info("Metadata modification was successful in file: " + file.getAbsolutePath());
        } else {
            logger.error("Metadata modification failed in file: " + file.getAbsolutePath());
        }
    }

    private static boolean handleDateChangingInFileMetadata(File file, String date, String time) {
        boolean result = true;
        String ext = FilenameUtils.getExtension(file.getName());
        if (Constants.ALLOWED_VIDEO_FILES.contains(ext) || Constants.ALLOWED_IMAGE_FILES.contains(ext)) {
            result = setDatesInFileMetadata(file, date, time, EXIFTOOL_DATE_CORRECTION_CMD_WITH_LATIN_CHARSET);
            if (!result) {
                logger.warn("Date set with latin charset failed in file: "
                            + file.getAbsolutePath()
                            + " -> Trying it with workaround now...");
                result = setDatesInFileMetadataWithFileMovingWorkaround(file, date, time);
            }
        } else {
            logger.warn("File is not allowed to be processed. Extension not allowed. File: "
                        + file.getAbsolutePath() + " | "
                        + Constants.ALLOWED_FILE_EXTENSIONS_INFO);
        }
        return result;
    }

    private static boolean setDatesInFileMetadataWithFileMovingWorkaround(File file, String date, String time) {
        boolean result;
        // WORKAROUND
        // It is necessary because unfortunately the exiftool would not handle special characters in
        // filenames so temporarily the file has to be moved back to root dir and renamed to a simplified name
        // until this issue has been solved
        File tmpFile = moveFileWithSimplifiedNameToRoot(file);
        if (tmpFile != null && tmpFile.exists()) {
            result = setDatesInFileMetadata(tmpFile, date, time, EXIFTOOL_DATE_CORRECTION_CMD_WITH_UTF8_CHARSET);
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

    private static boolean setDatesInFileMetadata(File file, String date, String time, String exiftoolCmd) {
        try {
            String dateCorrected = date.replaceAll("-", ":");
            String tmpFilePath = file.getAbsolutePath();
            List<String> metadata = Arrays
                .stream(METADATA)
                .map(m -> MessageFormat.format(m, dateCorrected, time))
                .collect(Collectors.toList());
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
            logger.error(e);
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
            logger.error(e);
        }
        return tempMovedFile;
    }

}
