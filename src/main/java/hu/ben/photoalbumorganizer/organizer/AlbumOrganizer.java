package hu.ben.photoalbumorganizer.organizer;

import java.text.MessageFormat;

import hu.ben.photoalbumorganizer.util.datecorrection.VideoFileDateCorrectorUtil;
import hu.ben.photoalbumorganizer.util.handbrake.HandBrakeUtil;
import hu.ben.photoalbumorganizer.util.rename.RenamingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AlbumOrganizer {

    private String containerDirAbsPath;

    public void organizeAlbumFiles() {
        long startTime = System.currentTimeMillis();

        RenamingUtil.renameAlbumFiles(containerDirAbsPath);
        HandBrakeUtil.convertVideoFiles(containerDirAbsPath);
        VideoFileDateCorrectorUtil.correctDates(containerDirAbsPath);

        long endTime = System.currentTimeMillis();
        double elapsedTime = (double) (endTime - startTime) / (1000);
        String logMessage = MessageFormat.format("Elapsed time is {0} seconds.", elapsedTime);
        System.out.println(logMessage);
    }

}
