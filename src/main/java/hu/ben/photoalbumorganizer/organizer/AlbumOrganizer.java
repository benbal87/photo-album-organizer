package hu.ben.photoalbumorganizer.organizer;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.organizer.rename.Rename;
import hu.ben.photoalbumorganizer.organizer.rename.RenameForIphone;
import hu.ben.photoalbumorganizer.util.FileDateCorrectorUtil;
import hu.ben.photoalbumorganizer.util.HandBrakeUtil;
import hu.ben.photoalbumorganizer.util.LogUtil;
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
        new Rename().renameAlbumFiles(workDir);
        RenameForIphone.renameFiles(workDir);
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
