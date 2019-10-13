package hu.ben.photoalbumrenaming.util;

import java.io.File;
import java.text.MessageFormat;

import hu.ben.photoalbumrenaming.exception.ArgumentsNotValidException;

public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    public static void validateArguments(String[] arguments) {
        validateNumberOfArguments(arguments);
        validateIfArgumentIsValidPath(arguments);
        validateIfArgumentIsDirectory(arguments);
    }

    private static void validateNumberOfArguments(String[] arguments) {
        if (arguments.length != 1) {
            throw new ArgumentsNotValidException(
                MessageFormat.format(
                    "Number of arguments must be one. There were {0} number of of them.",
                    arguments.length
                ));
        }
    }

    private static void validateIfArgumentIsValidPath(String[] arguments) {
        if (!new File(arguments[0]).exists()) {
            throw new ArgumentsNotValidException(MessageFormat.format(
                "Given path [{0}] does not exists!",
                arguments[0]
            ));
        }
    }

    private static void validateIfArgumentIsDirectory(String[] arguments) {
        if (!new File(arguments[0]).isDirectory()) {
            throw new ArgumentsNotValidException(
                MessageFormat.format(
                    "Given path [{0}] is not a directory!",
                    arguments[0]
                ));
        }
    }

}
