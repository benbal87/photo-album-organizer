package hu.ben.photoalbumrenaming.rename;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

import hu.ben.photoalbumrenaming.comparator.ImageCreationTimeComparator;
import hu.ben.photoalbumrenaming.comparator.VideoCreationTimeComparator;
import hu.ben.photoalbumrenaming.constant.Constants;

public class PhotoAlbumRenaming {

    private static int NUMBER_OF_FILES_TO_BE_PROCESSED = 0;

    private static HashMap<String, List<File>> fileMap = new HashMap<>();

    public void renameAlbumFiles(String location) {
        getFiles(location);
        sortFiles();

        System.out.println(MessageFormat.format("{0} files renamed.", NUMBER_OF_FILES_TO_BE_PROCESSED));
    }

    private void getFiles(String workingDirectory) {
        File[] filesInWorkingDirectory = Objects.requireNonNull(new File(workingDirectory).listFiles());
        List<File> fileList = new ArrayList<>();

        for (File file : filesInWorkingDirectory) {
            boolean isFileAnImage = Constants.ALLOWED_IMAGE_FILES.contains(FilenameUtils.getExtension(file.getName()));
            boolean isFileAVideo = Constants.ALLOWED_VIDEO_FILES.contains(FilenameUtils.getExtension(file.getName()));

            if (!file.isDirectory() && (isFileAnImage || isFileAVideo)) {
                String fileParentDirectoryName = file.getParentFile().getName();

                if (isFileAVideo) {
                    fileParentDirectoryName += " video";
                }

                List<File> fileMapEntry = fileMap.get(fileParentDirectoryName);

                if (fileMapEntry != null) {
                    fileMapEntry.add(file);
                } else {
                    fileList.add(file);
                    fileMap.put(fileParentDirectoryName, fileList);
                }

                NUMBER_OF_FILES_TO_BE_PROCESSED++;
            } else {
                getFiles(file.getAbsolutePath());
            }
        }
    }

    private void sortFiles() {
        for (String key : fileMap.keySet()) {
            if (key.contains(Constants.VIDEO)) {
                fileMap.get(key).sort(new VideoCreationTimeComparator());
            } else {
                fileMap.get(key).sort(new ImageCreationTimeComparator());
            }
        }
    }

}
