package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.text.MessageFormat;

import hu.ben.photoalbumorganizer.exception.InvalidArgumentsException;

public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    public static void validateArguments(String containerDirAbsPath) {
        validateIfArgumentIsValidPath(containerDirAbsPath);
        validateIfArgumentIsDirectory(containerDirAbsPath);
    }

    private static void validateIfArgumentIsValidPath(String containerDirAbsPath) {
        if (!isArgumentHasValidPath(containerDirAbsPath)) {
            throw new InvalidArgumentsException(MessageFormat.format(
                "Given path [{0}] does not exists!",
                containerDirAbsPath
            ));
        }
    }

    private static void validateIfArgumentIsDirectory(String containerDirAbsPath) {
        if (!new File(containerDirAbsPath).isDirectory()) {
            throw new InvalidArgumentsException(
                MessageFormat.format(
                    "Given path [{0}] is not a directory!",
                    containerDirAbsPath
                ));
        }
    }

    public static boolean isArgumentHasValidPath(String arg) {
        return new File(arg).exists();
    }

}
