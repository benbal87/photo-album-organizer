package hu.ben.photoalbumorganizer.util;

import org.apache.commons.lang3.StringUtils;

public final class LogUtil {

    public static final String LOG_SEPARATOR = StringUtils.repeat('-', 120) + "\n";

    private LogUtil() {
    }

    public static String getSeparator() {
        return provideSeparator(1);
    }

    public static String getSeparator(int numberOfLines) {
        return provideSeparator(numberOfLines);
    }

    private static String provideSeparator(int numberOfLines) {
        int noLinesCorrected = numberOfLines < 0 ? 1 : numberOfLines;
        String dividers = StringUtils.repeat(LOG_SEPARATOR, noLinesCorrected);
        return "\n" + dividers;
    }

}
