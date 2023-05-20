package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import hu.ben.photoalbumorganizer.validator.DateValidatorUsingDateFormat;

public final class RenameUtil {

    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";

    public static final String DAY_MONTH_YEAR_DATE_FORMAT = "yyyy-MM-dd";

    private RenameUtil() {
    }

    public static String getFormattedDateString(Date date) {
        return getFormattedDateString(date, RenameUtil.ISO_8601_DATE_FORMAT);
    }

    public static String getFormattedDateString(Date date, String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(date);
    }

    public static String getFormattedDateString(ZonedDateTime date) {
        return getFormattedDateString(date, ISO_8601_DATE_FORMAT);
    }

    public static String getFormattedDateString(ZonedDateTime date, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return date.format(formatter);
    }

    public static ZonedDateTime getDateFromFileName(File file) {
        String dateString = getDateStringFromFileName(file);
        return getDateFromDateString(dateString);
    }

    public static String getDateStringFromFileName(File file) {
        if (file.exists()) {
            String fileName = file.getName();
            return getDateStringFromFileName(fileName);
        }
        return null;
    }

    public static String getDateStringFromFileName(String fileName) {
        if (fileName.length() > 9) {
            String possibleDateStr = fileName.substring(0, 10);
            if (new DateValidatorUsingDateFormat().isValid(possibleDateStr)) {
                ZonedDateTime zd = getDateFromDateString(possibleDateStr);
                return getFormattedDateString(zd);
            }

            // We are trying to get the date from a file name like this "Photo 1-12-2022, 18 15 33.jpg"
            // or like this "Video 18-12-2022, 22 33 31.mov"
            try {
                String[] parts = fileName.split(" ");
                String partOne = parts[1];
                String dateStr = partOne.substring(0, partOne.length() - 1);
                if (new DateValidatorUsingDateFormat(DAY_MONTH_YEAR_DATE_FORMAT).isValid(dateStr)) {
                    ZonedDateTime zd = getDateFromDateString(dateStr);
                    return getFormattedDateString(zd, DAY_MONTH_YEAR_DATE_FORMAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static ZonedDateTime getDateFromDateString(String date) {
        return getDateFromDateString(date, ISO_8601_DATE_FORMAT);
    }

    public static ZonedDateTime getDateFromDateString(String date, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        ZonedDateTime d = ZonedDateTime.parse(date, formatter);
        return d.withZoneSameInstant(ZoneId.systemDefault());
    }

}
