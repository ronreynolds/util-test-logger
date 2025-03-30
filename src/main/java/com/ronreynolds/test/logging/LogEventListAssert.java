package com.ronreynolds.test.logging;

import java.util.List;
import org.assertj.core.api.ListAssert;
import org.slf4j.event.Level;

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
    .firstLogEvent()
    .hasMessageTemplate("foo:{}")
    .containsMessageArgs(123);
</pre>
 @formatter:on
 */
public class LogEventListAssert extends ListAssert<LogEvent> {
    private LogEventListAssert(List<LogEvent> actual) {
        super(actual);
    }

    /**
     * create a {@code LogEventListAssert} for the provided {@code List<LogEvent>}.
     */
    public static LogEventListAssert assertThat(List<LogEvent> logEventList) {
        return new LogEventListAssert(logEventList);
    }

    public static LogEventListAssert assertThat(TestLogger log, Level level) {
        return new LogEventListAssert(log.getEventsAtLevel(level));
    }

    /**
     * Allows for performing asserts on the first log message in this list of log events.
     * Can't be called {@code first()} because return-type conflicts with super-class version.
     */
    public LogEventAssert firstLogEvent() {
        return super.first(LogEventAssert.FACTORY);
    }

    /**
     * Allows for performing asserts on the last log message in this list of log events.
     * Can't be called {@code last()} because return-type conflicts with super-class version.
     */
    public LogEventAssert lastLogEvent() {
        return super.last(LogEventAssert.FACTORY);
    }

    /**
     * Allows for performing asserts on the message with the specified index (0-based) in this list of log events.
     * Can't be called {@code element()} because return-type conflicts with super-class version.
     */
    public LogEventAssert logElement(int i) {
        return super.element(i, LogEventAssert.FACTORY);
    }

    /*
     * unfortunately despite the awesome that is AssertJ we need to overload some methods to keep the return type from
     * reverting to {@code ListAssert<LogEvent>} which doesn't have methods like firstLogEvent(). :-/
     */
    @Override
    public LogEventListAssert hasSize(int expected) {
        return (LogEventListAssert) super.hasSize(expected);
    }

    @Override
    public LogEventListAssert isNotEmpty() {
        return (LogEventListAssert) super.isNotEmpty();
    }
}