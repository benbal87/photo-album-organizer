package hu.ben.photoalbumorganizer.constant;

import java.util.List;

public final class Constants {

    public static final List<String> ALLOWED_VIDEO_FILES = List.of("mp4", "avi", "mov", "mpg");

    public static final List<String> ALLOWED_IMAGE_FILES = List.of("jpg", "jpeg", "gif");

    public static final String ALLOWED_IMAGE_EXTENSIONS = "[" + String.join(", ", ALLOWED_IMAGE_FILES) + "]";

    public static final String ALLOWED_VIDEO_EXTENSIONS = "[" + String.join(", ", ALLOWED_VIDEO_FILES) + "]";

    public static final String ALLOWED_FILE_EXTENSIONS_INFO =
        "Allowed image file extensions: " + Constants.ALLOWED_IMAGE_EXTENSIONS + " | "
        + "Allowed video file extensions: " + Constants.ALLOWED_VIDEO_EXTENSIONS;

    public static final String VIDEO_CONVERSION_SUFFIX = "__conv";

    private Constants() {
    }

}
