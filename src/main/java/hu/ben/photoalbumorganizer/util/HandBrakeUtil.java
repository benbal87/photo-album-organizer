package hu.ben.photoalbumorganizer.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.ben.photoalbumorganizer.constant.Constants;

public final class HandBrakeUtil {

    private static final Logger logger = LogManager.getLogger(HandBrakeUtil.class);

    private HandBrakeUtil() {
    }

    private static final String HANDBRAKE_CLI_CMD_TEMPLATE =
        "HandBrakeCLI --verbose --quality 28.0 --vfr --input \"{0}\" --output \"{1}\"";

    public static void convertVideoFiles(String workDir) {
        logger.info(LogUtil.getSeparator(3) + "Starting to convert video files in directory: " + workDir);
        Collection<File> videoFiles = FileUtil.getVideoFiles(workDir);
        for (File originalVideoFile : videoFiles) {
            File convertedFile = convertFile(originalVideoFile);
            if (convertedFile != null && convertedFile.exists()) {
                FileDateCorrectorUtil.modifyConvertedVideoFileDates(convertedFile, originalVideoFile);
            } else {
                logger.error(
                    "Can not modify converted video file dates. Converted file does not exists. Original file: "
                    + originalVideoFile.getAbsolutePath());
            }
        }
    }

    private static File convertFile(File file) {
        String inputFilePath = file.getAbsolutePath();
        logger.info("Attempting to convert video file: " + inputFilePath);
        try {
            String outputFilePath = getOutputVideoFileAbsolutePath(file);
            logger.info("Output file path: " + outputFilePath);
            String cmd = MessageFormat.format(HANDBRAKE_CLI_CMD_TEMPLATE, inputFilePath, outputFilePath);
            logger.info("Executing command: " + cmd);

            CommandLine cmdLine = CommandLine.parse(cmd);
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute(cmdLine);
            logger.info("Command execution result: " + exitValue);

            return new File(outputFilePath);
        } catch (IOException e) {
            logger.error("Error occurred while trying to convert video file: " + inputFilePath);
            logger.error(e.toString());
        }
        return null;
    }

    private static String getOutputVideoFileAbsolutePath(File file) {
        String fileName = file.getName();
        String fileNameWithoutExt = FilenameUtils.removeExtension(fileName);
        String ext = FilenameUtils.getExtension(fileName);
        String fileNameWithoutExtRenamed = fileNameWithoutExt + Constants.VIDEO_CONVERSION_SUFFIX;
        String path = FilenameUtils.getFullPath(file.getAbsolutePath());
        return MessageFormat.format("{0}{1}.{2}", path, fileNameWithoutExtRenamed, ext);
    }

}
