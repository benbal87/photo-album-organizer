package hu.ben.photoalbumorganizer.organizer.rename;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.RenameUtil;
import hu.ben.photoalbumorganizer.validator.DateValidatorUsingDateFormat;

public final class RenamingUtilForIphoneFiles {

    private RenamingUtilForIphoneFiles() {
    }

    public static void renameFilesFromIphone(String workDir) {
        Collection<File> imageAndVideoFiles = FileUtil.getImageAndVideoFiles(workDir);
        List<String> processedAlbumDirs = new ArrayList<>();
        for (File file : imageAndVideoFiles) {
            File albumDir = file.getParentFile();
            String albumDirPath = albumDir.getAbsolutePath();
            if (!processedAlbumDirs.contains(albumDirPath)) {
                processedAlbumDirs.add(albumDirPath);
                String folderNameTopic = getFolderNameTopicFromParentDir(albumDir);
                if (folderNameTopic != null) {
                    renameFilesFromIphone(albumDirPath, folderNameTopic);
                }
            }
        }
    }

    public static String getFolderNameTopicFromParentDir(File albumDir) {
        String result = null;
        if (albumDir.isDirectory()) {
            String folderName = albumDir.getName();
            String dateInFolder = folderName.substring(0, 10);
            result = new DateValidatorUsingDateFormat().isValid(dateInFolder) ? folderName.substring(11) : folderName;
        }
        return result;
    }

    private static void renameFilesFromIphone(String albumDir, String folderNameTopic) {
        Collection<File> imageAndVideoFiles = FileUtil.getImageAndVideoFiles(albumDir);
        for (File file : imageAndVideoFiles) {
            if (file.getName().startsWith("Photo") || file.getName().startsWith("Video")) {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(file.getAbsolutePath());
                String dateStr = RenameUtil.getDateStringFromFileName(file);
                System.out.println("Date String: " + dateStr);
                String fileExt = FilenameUtils.getExtension(file.getName());
                System.out.println("File extension: " + fileExt);
                String videoStr = FileUtil.isFileVideo(file) ? "video " : "";
                String newFileNameWithoutNumber =
                    MessageFormat.format("{0} {1} {2}", dateStr, folderNameTopic, videoStr);
                System.out.println("New file name without number: " + newFileNameWithoutNumber);
                String newFileName = getNewFileNameWithCount(albumDir, newFileNameWithoutNumber, fileExt);
                System.out.println("New file name: " + newFileName);
                String newFileAbsPath = albumDir + newFileName;
                System.out.println("newFileAbsPath: " + newFileAbsPath);
                File newFile = new File(newFileAbsPath);
                try {
                    FileUtils.moveFile(file, newFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String getNewFileNameWithCount(
        String albumDir,
        String newFileNameWithoutNumber,
        String fileExtension
    ) {
//        String result = newFileNameWithoutNumber + "001";
        String result;
        List<File> imageAndVideoFiles = new ArrayList<>(FileUtil.getImageAndVideoFiles(albumDir));
        int count = 1;
        for (File file : imageAndVideoFiles) {
            String fileName = FilenameUtils.removeExtension(file.getName());
            if (fileName.contains(newFileNameWithoutNumber)) {
                String countInName = fileName.substring(fileName.length() - 4).trim();
                int currentCount = Integer.parseInt(countInName);
                System.out.println("currentCount: " + currentCount + " | count: " + count);
                if (currentCount >= count) {
                    count = currentCount + 1;
                }
            }
        }

        String SEQUENTIAL_NUMBER_FORMAT_PATTERN = "%0{0}d";
        int numberOfDigits = count < 1000 ? 3 : String.valueOf(count).length();
        String pattern = MessageFormat.format(SEQUENTIAL_NUMBER_FORMAT_PATTERN, numberOfDigits);
        String countString = String.format(pattern, count);
        result = newFileNameWithoutNumber + countString + "." + fileExtension;

        return result;
    }

}
