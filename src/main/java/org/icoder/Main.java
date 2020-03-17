package org.icoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icoder.services.LogConsumer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;


public class Main {

    private static Logger logger = LogManager.getLogger(Main.class);

    /**
     * Main cmd initializer
     *
     * @param cmdArgs
     */
    public static void main(String[] cmdArgs) {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        List<String> args = Arrays.asList(cmdArgs);
        logger.debug("Log processor args {}", args);
        File inputFile = null;

        try {
            inputFile = new File(args.get(0));
        } catch (Exception e) {
            logger.error("File name argument is required", e);
            return;
        }

        File outputFile = args.size() == 2 ? new File(args.get(1)) : null;

        try {
            LogConsumer logConsumer = new LogConsumer();

            logConsumer.process(inputFile, outputFile, true);
        } catch (Exception e) {
            logger.error("LogConsumer", e);
        }
    }
}
