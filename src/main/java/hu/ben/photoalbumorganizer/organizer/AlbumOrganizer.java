package hu.ben.photoalbumorganizer.organizer;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.util.LogUtil;
import hu.ben.photoalbumorganizer.util.datecorrection.FileDateCorrectorUtil;
import hu.ben.photoalbumorganizer.util.handbrake.HandBrakeUtil;
import hu.ben.photoalbumorganizer.util.rename.RenamingUtil;
import hu.ben.photoalbumorganizer.util.rename.RenamingUtilForIphoneFiles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AlbumOrganizer {

    private static final Logger logger = LogManager.getLogger(AlbumOrganizer.class);

    private String workDir;

    public void organizeAlbumFiles() {
        long startTime = System.currentTimeMillis();
        RenamingUtil.renameAlbumFiles(workDir);
        RenamingUtilForIphoneFiles.renameFilesFromIphone(workDir);
        HandBrakeUtil.convertVideoFiles(workDir);
        FileDateCorrectorUtil.setFileDatesBasedOnFileName(workDir);
        FileDateCorrectorUtil.setFileDatesBasedOnDateInParentDirName(workDir);
        logElapsedTime(startTime);
    }

    private static void logElapsedTime(long startTime) {
        long endTime = System.currentTimeMillis();
        double elapsedTime = (double) (endTime - startTime) / (1000);
        String msg = MessageFormat.format("{0}Elapsed time is {1} seconds.", LogUtil.getSeparator(), elapsedTime);
        logger.info(msg);
    }

}
