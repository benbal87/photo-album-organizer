package hu.ben.photoalbumorganizer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class RenameUtil {

    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";

    private RenameUtil() {
    }

    public static String getFormattedDateString(Date date) {
        return getFormattedDateString(date, RenameUtil.ISO_8601_DATE_FORMAT);
    }

    public static String getFormattedDateString(Date date, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);
    }

}
