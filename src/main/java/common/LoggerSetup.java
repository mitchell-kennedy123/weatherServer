package common;

import java.io.IOException;
import java.util.logging.*;

public class LoggerSetup {

    static Level toConsoleLogLevel = Level.ALL;

    public static void setupLogger(Logger logger, String logFilePath) {
        try {
            // File handler for logging into a file
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Console handler for logging to console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(toConsoleLogLevel);


            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            logger.log(Level.ALL, "Error setting up logger", e);
        }
    }
}