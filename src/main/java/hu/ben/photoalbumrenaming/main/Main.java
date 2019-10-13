package hu.ben.photoalbumrenaming.main;

import java.text.MessageFormat;

import hu.ben.photoalbumrenaming.rename.PhotoAlbumRenaming;
import hu.ben.photoalbumrenaming.util.ValidatorUtil;

public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        ValidatorUtil.validateArguments(args);

        PhotoAlbumRenaming photoAlbumRenaming = new PhotoAlbumRenaming();

        photoAlbumRenaming.renameAlbumFiles(args[0]);

        long endTime = System.currentTimeMillis();
        double elapsedTime = (double) (endTime - startTime) / (1000);
        String logMessage = MessageFormat.format("Elapsed time is {0} seconds.", elapsedTime);
        System.out.println(logMessage);
    }

}
