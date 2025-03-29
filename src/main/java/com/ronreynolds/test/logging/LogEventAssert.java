package com.ronreynolds.test.logging;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * AssertJ-style class to make assertions about LogEvents
 */
public class LogEventAssert extends AbstractAssert<LogEventAssert, LogEvent> {
    public static final InstanceOfAssertFactory<LogEvent, LogEventAssert> FACTORY =
        new InstanceOfAssertFactory<>(LogEvent.class, LogEventAssert::new);

    public static LogEventAssert assertThat(LogEvent logEvent) {
        return new LogEventAssert(logEvent);
    }

    private LogEventAssert(LogEvent actual) {
        super(actual, LogEventAssert.class);
    }

    public LogEventAssert isLevel(Level level) {
        isNotNull();
        Assertions.assertThat(actual.getLevel())
                  .withFailMessage("Level mismatch; actual %s is not %s", actual.getLevel(), level)
                  .isSameAs(level);
        return myself;
    }

    public LogEventAssert hasFormattedMessage(String msg) {
        isNotNull();
        Assertions.assertThat(actual.getFormattedMessage())
                  .withFailMessage("Formatted-message mismatch; actual %s is not %s", actual.getFormattedMessage(), msg)
                  .isEqualTo(msg);
        return myself;
    }

    public LogEventAssert hasThrown(Throwable throwable) {
        isNotNull();
        Assertions.assertThat(actual.getThrown())
                  .withFailMessage("Thrown mismatch; actual %s is not %s", actual.getThrown(), throwable)
                  .isEqualTo(throwable);
        return myself;
    }

    public LogEventAssert contextMapMatches(Predicate<? super Map<String, String>> predicate) {
        isNotNull();
        Assertions.assertThat(actual.getContextMap())
                  .withFailMessage("ContextMap mismatch; actual %s didn't pass predicate", actual.getContextMap())
                  .matches(predicate);
        return myself;
    }

    public LogEventAssert contextMapContains(String key, String value) {
        isNotNull();
        Assertions.assertThat(actual.getContextMap())
                  .withFailMessage("ContextMap is null")
                  .isNotNull();
        String mapVal = actual.getContextMap().get(key);
        Assertions.assertThat(mapVal)
                  .withFailMessage("ContextMap entry %s (%s) doesn't match %s", key, mapVal, value)
                  .isEqualTo(value);
        return myself;
    }

    public LogEventAssert contextMapContains(String key, Predicate<String> valueTest) {
        isNotNull();
        Assertions.assertThat(actual.getContextMap())
                  .withFailMessage("ContextMap is null")
                  .isNotNull();
        String mapVal = actual.getContextMap().get(key);
        Assertions.assertThat(mapVal)
                  .withFailMessage("ContextMap entry %s (%s) doesn't match predicate", key, mapVal)
                  .matches(valueTest);
        return myself;
    }

    public LogEventAssert contextMapContains(Map.Entry<String, String>... keyValues) {
        isNotNull();
        Assertions.assertThat(actual.getContextMap())
                  .withFailMessage("ContextMap mismatch; actual %s doesn't contain %s", actual.getContextMap(), Arrays.toString(keyValues))
                  .contains(keyValues);
        return myself;
    }

    public LogEventAssert contextMapDoesNotContain(String key) {
        isNotNull();
        // a null ContextMap, by definition, doesn't contain the specified key
        if (actual.getContextMap() != null) {
            Assertions.assertThat(actual.getContextMap().get(key))
                      .withFailMessage("ContextMap contains entry %s", key)
                      .isNull();
        }
        return myself;
    }

    public LogEventAssert hasLoggerName(String loggerName) {
        isNotNull();
        Assertions.assertThat(actual.getLoggerName())
                  .withFailMessage("Logger mismatch; actual %s is not %s", actual.getLoggerName(), loggerName)
                  .isEqualTo(loggerName);
        return myself;
    }

    public LogEventAssert hasMarker(Marker marker) {
        isNotNull();
        Assertions.assertThat(actual.getMarker())
                  .withFailMessage("Marker mismatch; actual %s is not %s", actual.getMarker(), marker)
                  .isEqualTo(marker);
        return myself;
    }

    public LogEventAssert hasMessageTemplate(String message) {
        isNotNull();
        Assertions.assertThat(actual.getMessage())
                  .withFailMessage("Message mismatch; actual %s is not %s", actual.getMessage(), message)
                  .isEqualTo(message);
        return myself;
    }

    public LogEventAssert messageArgsMatch(Predicate<Object[]> predicate) {
        isNotNull();
        Assertions.assertThat(actual.getMessageArgs())
                  .withFailMessage("Message-args mismatch; %s didn't pass predicate", Arrays.toString(actual.getMessageArgs()))
                  .matches(predicate);
        return myself;
    }

    public LogEventAssert containsMessageArgs(Object... args) {
        isNotNull();
        Assertions.assertThat(actual.getMessageArgs())
                  .withFailMessage("Message-args mismatch; %s didn't contain %s",
                                   Arrays.toString(actual.getMessageArgs()), Arrays.toString(args))
                  .contains(args);
        return myself;
    }

    public LogEventAssert hasThreadName(String name) {
        isNotNull();
        Assertions.assertThat(actual.getThreadName())
                  .withFailMessage("Thread name mismatch; actual %s is not %s", actual.getThreadName(), name)
                  .isEqualTo(name);
        return myself;
    }
}