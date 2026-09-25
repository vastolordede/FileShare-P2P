package vn.edu.p2p.tracker.logging;

import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Small java.util.logging bootstrap for the Tracker process.
 * No external logging framework is required for the course project.
 */
public final class TrackerLogging {
    private static final String FORMAT =
            "%1$tF %1$tT [%4$-7s] [%3$s] %5$s%6$s%n";

    private TrackerLogging() {
    }

    public static void configure(String rawLevel) {
        Level level = parseLevel(rawLevel);
        System.setProperty("java.util.logging.SimpleFormatter.format", FORMAT);

        Logger root = Logger.getLogger("");
        root.setLevel(level);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(level);
        }

        Logger.getLogger("vn.edu.p2p.tracker").setLevel(level);
    }

    static Level parseLevel(String rawLevel) {
        if (rawLevel == null || rawLevel.isBlank()) {
            return Level.INFO;
        }
        try {
            return Level.parse(rawLevel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Level.INFO;
        }
    }
}
