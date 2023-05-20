package hu.ben.photoalbumorganizer.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.organizer.AlbumOrganizer;
import hu.ben.photoalbumorganizer.util.LogUtil;
import hu.ben.photoalbumorganizer.util.ValidatorUtil;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        String workdir = System.getProperty("user.dir");
        logger.info(LogUtil.getSeparator(6) + "Working directory: " + workdir);
        ValidatorUtil.validateArguments(workdir);
        new AlbumOrganizer(workdir).organizeAlbumFiles();
    }

}
