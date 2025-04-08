package com.ronreynolds.test.logging;

import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.slf4j.event.Level;

import java.util.List;

/**
 * Assert-J style class for making assertions about {@code List<LogEvent>}
 *
 * USAGE:
 @formatter:off
 <pre>
 TestLogger logger = TestLogger.getLogger("fubar");
 logger.info("foo:{}", 123);
 LogEventListAssert.assertThat(logger, Level.INFO)
    .isNotEmpty()
    .first()
    .hasMessageTemplate("foo:{}")
    .containsMessageArgs(123);
</pre>
 @formatter:on
 */
public class LogEventListAssert
        extends FactoryBasedNavigableListAssert<LogEventListAssert, List<? extends LogEvent>, LogEvent, LogEventAssert> {
    private LogEventListAssert(List<LogEvent> actual) {
        super(actual, LogEventListAssert.class, LogEventAssert::new);
    }

    /**
     * create a {@code LogEventListAssert} for the provided {@code List<LogEvent>}.
     */
    public static LogEventListAssert assertThat(List<LogEvent> logEventList) {
        return new LogEventListAssert(logEventList);
    }

    /**
     * create a {@code LogEventListAssert} for the {@code List<LogEvent>} of events at {@code level} in {@code log}
     * @param log the {@code TestLogger} from which we want the list of {@code LogEvent}s
     * @param level the level of the {@code LogEvent}s upon which we want to assert
     * @return new {@code LogEventListAssert} for the {@code LogEvent}s at the specified level in the provided {@code TestLogger}
     */
    public static LogEventListAssert assertThat(TestLogger log, Level level) {
        return assertThat(log.getEventsAtLevel(level));
    }
}