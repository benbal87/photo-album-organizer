package hu.ben.photoalbumorganizer.validator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import hu.ben.photoalbumorganizer.util.RenameUtil;

public class DateValidatorUsingDateFormat implements DateValidator {

    private final String dateFormat;

    public DateValidatorUsingDateFormat() {
        this.dateFormat = RenameUtil.ISO_8601_DATE_FORMAT;
    }

    public DateValidatorUsingDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public boolean isValid(String dateStr) {
        DateFormat sdf = new SimpleDateFormat(this.dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}
