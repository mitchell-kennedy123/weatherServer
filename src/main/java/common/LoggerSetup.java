package common;

import java.io.IOException;
import java.util.logging.*;

public class LoggerSetup {

    // Static method to set up the logger configuration
    public static void setupLogger(Logger logger, String logFilePath) {
        try {
            // File handler for logging into a file
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Console handler for logging to console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);

            // Add handlers to the logger
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);  // Log everything from ALL to SEVERE
            logger.setUseParentHandlers(false);  // Disable default console logging

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up logger", e);
        }
    }
}