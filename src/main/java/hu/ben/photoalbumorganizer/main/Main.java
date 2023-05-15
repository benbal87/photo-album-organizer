package hu.ben.photoalbumorganizer.main;

import hu.ben.photoalbumorganizer.organizer.AlbumOrganizer;
import hu.ben.photoalbumorganizer.util.ValidatorUtil;

public class Main {

    public static void main(String[] args) {
        String workdir = System.getProperty("user.dir");
        System.out.println("Working Directory: " + workdir);
        ValidatorUtil.validateArguments(workdir);
        new AlbumOrganizer(workdir).organizeAlbumFiles();
    }

}
