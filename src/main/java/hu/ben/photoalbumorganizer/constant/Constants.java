package hu.ben.photoalbumorganizer.constant;

import java.util.List;

public final class Constants {

    public static final List<String> ALLOWED_VIDEO_FILES = List.of("mp4", "avi", "mov", "mpg");

    public static final List<String> ALLOWED_IMAGE_FILES = List.of("jpg", "jpeg", "gif");

    public static final String HANDBRAKE_CONVERT_NAME_CONCAT = "__HB";

    private Constants() {
    }

}
