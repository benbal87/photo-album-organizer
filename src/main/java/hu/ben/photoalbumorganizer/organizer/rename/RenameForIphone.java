package hu.ben.photoalbumorganizer.organizer.rename;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.LogUtil;
import hu.ben.photoalbumorganizer.util.RenameUtil;
import hu.ben.photoalbumorganizer.validator.DateValidatorUsingDateFormat;

public final class RenameForIphone {

    private static final Logger logger = LogManager.getLogger(RenameForIphone.class);

    public static final List<String> IPHONE_FILE_LEADING_CHARS = List.of("Photo", "Video");

    private RenameForIphone() {
    }

    public static boolean isFileFromIphone(File file) {
        String fileName = file.getName();
        return IPHONE_FILE_LEADING_CHARS.stream().anyMatch(fileName::startsWith);
    }

    public static void renameFiles(String workDir) {
        logger.info(LogUtil.getSeparator(3)
                    + "Starting to rename video and image files from iPhone in directory: "
                    + workDir);
        Collection<File> imageAndVideoFiles = FileUtil.getImageAndVideoFiles(workDir);
        Set<String> processedAlbumDirs = new HashSet<>();
        for (File file : imageAndVideoFiles) {
            File albumDir = file.getParentFile();
            String albumDirPath = albumDir.getAbsolutePath();
            if (!processedAlbumDirs.contains(albumDirPath)) {
                processedAlbumDirs.add(albumDirPath);
                String albumInfoStr = getAlbumInfoStringFromParentDir(albumDir);
                if (albumInfoStr != null) {
                    renameFiles(albumDirPath, albumInfoStr);
                }
            }
        }
    }

    private static String getAlbumInfoStringFromParentDir(File albumDir) {
        logger.info("Get album info string from album directory name: " + albumDir);
        if (albumDir.isDirectory()) {
            String folderName = albumDir.getName();
            String dateInFolder = folderName.substring(0, 10);
            String result =
                new DateValidatorUsingDateFormat().isValid(dateInFolder) ? folderName.substring(11) : folderName;
            logger.info("Album info string is: " + result);
            return result;
        }
        {
            logger.error("Can not get album info string from file object. Object is not directory."
                         + albumDir.getAbsolutePath());
        }
        return null;
    }

    private static void renameFiles(String albumDir, String albumInfoStr) {
        Collection<File> imageAndVideoFiles = FileUtil.getImageAndVideoFiles(albumDir);
        for (File file : imageAndVideoFiles) {
            if (isFileFromIphone(file)) {
                logger.info(LogUtil.getSeparator()
                            + "Attempting to rename file from iphone: "
                            + file.getAbsolutePath());
                String dateStr = RenameUtil.getDateStringFromFileName(file);
                if (dateStr != null) {
                    logger.debug("Date string from iphone file name: " + dateStr);

                    String videoStr = FileUtil.isFileVideo(file) ? "video " : "";
                    String newFileNameWithoutNumber =
                        MessageFormat.format("{0} {1} {2}", dateStr, albumInfoStr, videoStr);
                    logger.debug("New file name without number: " + newFileNameWithoutNumber);

                    String fileExt = FilenameUtils.getExtension(file.getName());
                    String newFileName = getNewFileNameWithNumber(albumDir, newFileNameWithoutNumber, fileExt);

                    Rename.renameFile(file, newFileName);
                } else {
                    logger.error("Excluding file. Can not extract date from file name: " + file.getAbsolutePath());
                }
            } else {
                logger.debug("Excluding file. File is not from iphone: " + file.getAbsolutePath());
            }
        }
    }

    private static String getNewFileNameWithNumber(
        String albumDir,
        String newFileNameWithoutNumber,
        String fileExtension
    ) {
        int n = getFileNumber(albumDir, newFileNameWithoutNumber);
        int numberOfDigits = n < 1000 ? 3 : String.valueOf(n).length();
        String pattern = MessageFormat.format(Rename.SEQUENTIAL_NUMBER_FORMAT_PATTERN, numberOfDigits);
        String countString = String.format(pattern, n);

        return MessageFormat.format("{0}{1}.{2}", newFileNameWithoutNumber, countString, fileExtension);
    }

    private static int getFileNumber(String albumDir, String newFileNameWithoutNumber) {
        List<File> imageAndVideoFiles = new ArrayList<>(FileUtil.getImageAndVideoFiles(albumDir));
        int n = 1;
        for (File file : imageAndVideoFiles) {
            String fileName = FilenameUtils.removeExtension(file.getName());
            if (fileName.contains(newFileNameWithoutNumber)) {
                String countInName = fileName.substring(fileName.length() - 4).trim();
                int currentCount = Integer.parseInt(countInName);
                if (currentCount >= n) {
                    n = currentCount + 1;
                }
            }
        }
        return n;
    }

}
