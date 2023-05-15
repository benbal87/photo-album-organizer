package hu.ben.photoalbumorganizer.util.datecorrection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import hu.ben.photoalbumorganizer.constant.Constants;
import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.rename.RenamingUtil;
import hu.ben.photoalbumorganizer.validator.DateValidator;
import hu.ben.photoalbumorganizer.validator.DateValidatorUsingDateFormat;

public final class FileDateCorrectorUtil {

    public static final String EXIFTOOL_DATE_CORRECTION_CMD =
        "exiftool -charset filename=UTF8 -overwrite_original {0} \"{1}\"";

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
    public static void correctDatesOfConvertedVideoFiles(String containerDirAbsPath) {
        File containerDir = new File(containerDirAbsPath);
        Collection<File> containerDirFiles = FileUtil.getVideoFiles(containerDir.getAbsolutePath());
        if (containerDirFiles.size() > 0) {
            List<File> handbrakedVideoFiles = getHandbrakedVideoFiles(containerDirFiles);
            for (File handbrakedVideoFile : handbrakedVideoFiles) {
                System.out.println(handbrakedVideoFile.getAbsolutePath());
                modifyConvertedVideoFileDates(handbrakedVideoFile, null);
            }
        }
    }

    public static void setFileDatesBasedOnFileName(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            Collection<File> files = FileUtil.getImageAndVideoFiles(dir);
            if (!files.isEmpty()) {
                for (File file : files) {
                    String date = FileUtil.getDateStringFromFileName(file);
                    setFileDates(file, date);
                }
            }
        }
    }

    private static List<File> getHandbrakedVideoFiles(Collection<File> containerDirFiles) {
        List<File> result = new ArrayList<>();
        for (File file : containerDirFiles) {
            if (isFileHandbraked(file)) {
                result.add(file);
            }
        }
        return result;
    }

    private static boolean isFileHandbraked(File file) {
        String fileNameWithoutExtension = FilenameUtils.removeExtension(file.getName());
        return fileNameWithoutExtension.endsWith(Constants.HANDBRAKE_CONVERT_NAME_CONCAT);
    }

    public static void modifyConvertedVideoFileDates(File convertedVideoFile, File originalVideoFile) {
        if (convertedVideoFile.exists()) {
            String date = getVideoFileCreationDateString(convertedVideoFile, originalVideoFile);
            setDatesInFileMetadata(convertedVideoFile, date);
        }
    }

    public static void setFileDates(File file, String date) {
        setDatesInFileMetadata(file, date);
    }

    public static void setDatesInFileMetadata(File file, String date) {
        String ext = FilenameUtils.getExtension(file.getName());
        if (Constants.ALLOWED_VIDEO_FILES.contains(ext) || Constants.ALLOWED_IMAGE_FILES.contains(ext)) {
            // WORKAROUND
            // It is necessary because unfortunately the exiftool would not handle special characters in
            // filenames so temporarily the file has to be moved back to root dir and renamed to a simplified name
            // until this issue has been solved
            File tmpFile = moveFileWithSimplifiedNameToRoot(file);
            if (tmpFile != null && tmpFile.exists()) {
                try {
                    String dateCorrected = date.replaceAll("-", ":");
                    String tmpFilePath = tmpFile.getAbsolutePath();
                    List<String> metadata = Arrays
                        .stream(METADATA).map(m -> MessageFormat.format(m, dateCorrected)).collect(Collectors.toList());
                    String metadataJoined = String.join(" ", metadata);
                    String cmd = MessageFormat.format(EXIFTOOL_DATE_CORRECTION_CMD, metadataJoined, tmpFilePath);

                    System.out.println("File to process: " + file.getAbsolutePath());
                    System.out.println("Date to set in file: " + dateCorrected);
                    System.out.println("Set dates in file with command: " + cmd);
                    CommandLine cmdLine = CommandLine.parse(cmd);
                    DefaultExecutor executor = new DefaultExecutor();
                    int exitValue = executor.execute(cmdLine);
                    System.out.println("Modification result: " + exitValue);
                    System.out.println("-----------------------------------------------------------------------------");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    // move file back to original path with original name
                    FileUtils.moveFile(tmpFile, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("File is not allowed to process. Extension not allowed. " + file.getAbsolutePath());
        }
    }

    private static File moveFileWithSimplifiedNameToRoot(File file) {
        File tempMovedFile = null;
        try {
            String extension = FilenameUtils.getExtension(file.getName());
            String rootDirPath = Paths.get(file.getAbsolutePath()).getRoot().toString();
            String tempFileAbsolutePath = MessageFormat.format("{0}temp-file.{1}", rootDirPath, extension);
            tempMovedFile = new File(tempFileAbsolutePath);
            FileUtils.moveFile(file, tempMovedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempMovedFile;
    }

    private static String getVideoFileCreationDateString(File convertedFile, File originalFile) {
        String result;
        if (originalFile != null) {
            Date fileCreationDate = FileUtil.getVideoFileCreationTime(originalFile);
            result = RenamingUtil.getFormattedDateString(fileCreationDate);
        } else {
            result = getFileCreationDateFromName(convertedFile);
        }
        return result;
    }

    private static String getFileCreationDateFromName(File file) {
        String result = null;
        String fileCreationDateRaw = FileUtil.getDateStringFromFileName(file);
        String dateRawFromParentDir = FileUtil.getDateStringFromFileParentDirName(file);
        DateValidator validator = new DateValidatorUsingDateFormat("yyyy-MM-dd");
        Date fileCreationDate = FileUtil.getVideoFileCreationTime(file);
        if (validator.isValid(fileCreationDateRaw)) {
            result = fileCreationDateRaw.replaceAll("-", ":");
        } else if (validator.isValid(dateRawFromParentDir)) {
            result = dateRawFromParentDir.replaceAll("-", ":");
        } else if (fileCreationDate != null) {
            result = RenamingUtil.getFormattedDateString(fileCreationDate);
        }
        return result;
    }

}
