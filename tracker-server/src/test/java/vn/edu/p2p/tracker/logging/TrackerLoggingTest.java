package vn.edu.p2p.tracker.logging;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackerLoggingTest {
    @Test
    void shouldParseConfiguredLevelCaseInsensitively() {
        assertEquals(Level.FINE, TrackerLogging.parseLevel("fine"));
    }

    @Test
    void invalidLevelShouldFallBackToInfo() {
        assertEquals(Level.INFO, TrackerLogging.parseLevel("not-a-level"));
    }
}
