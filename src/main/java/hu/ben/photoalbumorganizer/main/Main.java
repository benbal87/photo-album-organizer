package hu.ben.photoalbumorganizer.main;

import hu.ben.photoalbumorganizer.organizer.AlbumOrganizer;
import hu.ben.photoalbumorganizer.util.ValidatorUtil;

public class Main {

    public static void main(String[] args) {
        String containerDirAbsPath = String.join(" ", args);
        ValidatorUtil.validateArguments(containerDirAbsPath);
        new AlbumOrganizer(containerDirAbsPath).organizeAlbumFiles();
    }

}
