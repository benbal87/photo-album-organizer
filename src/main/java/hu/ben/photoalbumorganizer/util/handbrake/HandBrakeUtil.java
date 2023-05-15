package hu.ben.photoalbumorganizer.util.handbrake;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;

import hu.ben.photoalbumorganizer.constant.Constants;
import hu.ben.photoalbumorganizer.util.FileUtil;
import hu.ben.photoalbumorganizer.util.datecorrection.FileDateCorrectorUtil;

public final class HandBrakeUtil {

    private HandBrakeUtil() {
    }

    private static final String handbrakeCommandTemplate =
        "HandBrakeCLI --verbose --quality 28.0 --vfr --input \"{0}\" --output \"{1}\"";

    public static void convertVideoFiles(String containerDirAbsPath) {
        Collection<File> videoFiles = FileUtil.getVideoFiles(containerDirAbsPath);
        for (File originalVideoFile : videoFiles) {
            File convertedFile = convertFile(originalVideoFile);
            if (convertedFile != null && convertedFile.exists()) {
                FileDateCorrectorUtil.modifyConvertedVideoFileDates(convertedFile, originalVideoFile);
            }
        }
    }

    private static File convertFile(File file) {
        File outputFile = null;
        try {
            String input = file.getAbsolutePath();
            String fileNameWithoutExtension = FilenameUtils.removeExtension(file.getName());
            String fileNameWithoutExtensionRenamed = fileNameWithoutExtension + Constants.HANDBRAKE_CONVERT_NAME_CONCAT;
            String output = replaceLast(input, fileNameWithoutExtension, fileNameWithoutExtensionRenamed);
            System.out.println(output);
            String command = MessageFormat.format(handbrakeCommandTemplate, input, output);
            CommandLine cmdLine = CommandLine.parse(command);
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute(cmdLine);
            outputFile = new File(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    private static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index) + replacement
               + string.substring(index + substring.length());
    }

}
