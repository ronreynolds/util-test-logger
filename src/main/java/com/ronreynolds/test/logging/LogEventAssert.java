package com.ronreynolds.test.logging;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

/**
 * AssertJ-style class to make assertions about LogEvents
 */
public class LogEventAssert extends AbstractAssert<LogEventAssert, LogEvent> {
    public static final InstanceOfAssertFactory<LogEvent, LogEventAssert> FACTORY =
            new InstanceOfAssertFactory<>(LogEvent.class, LogEventAssert::new);

    public static LogEventAssert assertThat(LogEvent logEvent) {
        return new LogEventAssert(logEvent);
    }

    public LogEventAssert(LogEvent actual) {
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
        Map<String, String> contextMap = actual.getContextMap();
        Assertions.assertThat(contextMap)
                .withFailMessage("ContextMap is null")
                .isNotNull();
        String mapVal = contextMap.get(key);
        Assertions.assertThat(mapVal)
                .withFailMessage("ContextMap entry for key %s (%s) doesn't match value %s", key, mapVal, value)
                .isEqualTo(value);
        return myself;
    }

    public LogEventAssert contextMapContainsOnly(String key, String value) {
        isNotNull();
        Map<String, String> contextMap = actual.getContextMap();
        Assertions.assertThat(contextMap)
                .isNotNull()
                .containsOnly(Map.entry(key, value));
        return myself;
    }

    public LogEventAssert contextMapContains(String key, Predicate<String> valueTest) {
        isNotNull();
        Map<String, String> contextMap = actual.getContextMap();
        Assertions.assertThat(contextMap)
                .withFailMessage("ContextMap is null")
                .isNotNull();
        String mapVal = contextMap.get(key);
        Assertions.assertThat(mapVal)
                .withFailMessage("ContextMap entry %s (%s) doesn't match predicate", key, mapVal)
                .matches(valueTest);
        return myself;
    }

    public LogEventAssert contextMapContains(Map.Entry<String, String>... keyValues) {
        isNotNull();
        Map<String, String> contextMap = actual.getContextMap();
        Assertions.assertThat(contextMap)
                .withFailMessage(() -> String.format("ContextMap mismatch; actual %s doesn't contain %s", contextMap,
                        Arrays.toString(keyValues)))
                .contains(keyValues);
        return myself;
    }

    public LogEventAssert contextMapContainsOnly(Map.Entry<String, String>... keyValues) {
        isNotNull();
        Assertions.assertThat(actual.getContextMap()).containsOnly(keyValues);
        return myself;
    }

    public LogEventAssert contextMapDoesNotContain(String key) {
        isNotNull();
        Map<String, String> contextMap = actual.getContextMap();
        // a null ContextMap, by definition, doesn't contain the specified key
        if (contextMap != null) {
            Assertions.assertThat(contextMap).doesNotContainKey(key);
        }
        return myself;
    }

    public LogEventAssert hasLoggerName(String loggerName) {
        isNotNull();
        String actualLoggerName = actual.getLoggerName();
        Assertions.assertThat(actualLoggerName)
                .withFailMessage("Logger name mismatch; actual '%s' is not '%s'", actualLoggerName, loggerName)
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
                .as("checking that message-args contains specific args")
                .withFailMessage("Message-args mismatch; %s didn't contain %s",
                        Arrays.toString(actual.getMessageArgs()), Arrays.toString(args))
                .contains(args);
        return myself;
    }

    public LogEventAssert messageArgsHasSize(int size) {
        isNotNull();
        Assertions.assertThat(actual.getMessageArgs())
                .as("checking message arg size")
                .hasSize(size);
        return myself;
    }

    public LogEventAssert messageArgIsEqualTo(int index, Object arg) {
        isNotNull();
        Object[] args = actual.getMessageArgs();
        Assertions.assertThat(args).as("checking msg-args size").hasSizeGreaterThanOrEqualTo(index - 1);
        // unfortunately AbstractObjectArrayAssert doesn't have a method to get an ObjectAssert on an indexed element :-/
        Assertions.assertThat(args[index]).as("checking arg equality").isEqualTo(arg);
        return myself;
    }

        public LogEventAssert hasThreadName (String name){
            isNotNull();
            String threadName = actual.getThreadName();
            Assertions.assertThat(threadName)
                    .withFailMessage("Thread name mismatch; actual '%s' is not '%s'", threadName, name)
                    .isEqualTo(name);
            return myself;
        }
    }