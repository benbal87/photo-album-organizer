package hu.ben.photoalbumorganizer.util.datecorrection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import hu.ben.photoalbumorganizer.constant.Constants;
import hu.ben.photoalbumorganizer.util.FileUtil;

public final class VideoFileDateCorrectorUtil {

    private VideoFileDateCorrectorUtil() {
    }

    public static void correctDates(String containerDirAbsPath) {
        File containerDir = new File(containerDirAbsPath);

        Collection<File> containerDirFiles = FileUtil.getVideoFiles(containerDir.getAbsolutePath());
        if (containerDirFiles.size() > 0) {
            List<File> handbrakedVideoFiles = getHandbrakedVideoFiles(containerDirFiles);
            for (File handbrakedVideoFile : handbrakedVideoFiles) {
                System.out.println(handbrakedVideoFile.getAbsolutePath());
                modifyFileDates(handbrakedVideoFile);
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

    private static void modifyFileDates(File file) {
        if (file.exists()) {
            // WORKAROUND
            // it is necessary because unfortunately the the exiftool would not handle special characters in
            // filenames so temporarily the file has to be moved back to root dir and renamed to a simplified name
            // until this issue has been solved
            File tempMovedFile = moveFileWithSimplifiedNameToRoot(file);

            if (tempMovedFile != null && tempMovedFile.exists()) {
                try {
                    String date = getFileCreationDateFromName(file);
                    String fileCreateDate = MessageFormat.format("-FileCreateDate=\"{0} 12:00:00\"", date);
                    String fileModifyDate = MessageFormat.format("-FileModifyDate=\"{0} 12:00:00\"", date);
                    String quickTimeCreateDate = MessageFormat.format("-Quicktime:CreateDate=\"{0} 12:00:00\"", date);
                    String line = MessageFormat.format(
                        "exiftool -charset filename=UTF8 -overwrite_original {0} {1} {2} \"{3}\"",
                        fileCreateDate,
                        fileModifyDate,
                        quickTimeCreateDate,
                        tempMovedFile.getAbsolutePath()
                    );
                    System.out.println("Modifying video file date. Running command: " + line);
                    CommandLine cmdLine = CommandLine.parse(line);
                    DefaultExecutor executor = new DefaultExecutor();
                    int exitValue = executor.execute(cmdLine);
                    System.out.println("Modification result: " + exitValue);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    // move file back to original path with original name
                    FileUtils.moveFile(tempMovedFile, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    private static String getFileCreationDateFromName(File file) {
        String fileCreationDate = file.getName().substring(0, 11);
        return fileCreationDate.replaceAll("-", ":");
    }

}
