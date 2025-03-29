package com.ronreynolds.test.logging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

public class TestLogger extends AbstractLogger {
    // static fields
    //
    // cache of TestLogger instances by name
    private static final Map<String, TestLogger> testLoggerMap = new ConcurrentHashMap<>();
    private static final Consumer<LogEvent> BLACKHOLE = (t) -> {
    };
    // the callback used when no TestLogger-level callback is specified
    private static Consumer<LogEvent> defaultOnEventConsumer = BLACKHOLE;
    // the level used when no level is specified
    private static Level defaultLogLevel = Level.INFO;
    private static final List<LogEvent> EMPTY_LIST = List.of();
    private static final Comparator<LogEvent> EVENTS_BY_ID = (e1, e2) -> e1.getEventId() - e2.getEventId();

    // static methods
    //
    public static TestLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static TestLogger getLogger(String name) {
        return testLoggerMap.computeIfAbsent(Objects.requireNonNull(name, "logger name must not be null"), TestLogger::new);
    }

    /**
     * to change the logging level that loggers use when a level isn't specified
     */
    public static void setDefaultLoggerLevel(Level level) {
        defaultLogLevel = Objects.requireNonNull(level, "default log level must not be null");
    }

    /**
     * sets the default onEvent consumer (for newly-created loggers and those that don't have a onEvent consumer set)
     */
    public static void setOnAllEvents(Consumer<LogEvent> onEvent) {
        defaultOnEventConsumer = onEvent != null ? onEvent : BLACKHOLE;
    }

    /**
     * returns a list of all events of the specified level across ALL TestLogger instances
     */
    public static List<LogEvent> getAllEventsAtLevel(Level level) {
        return testLoggerMap.values().stream()
                            .flatMap((logger) -> logger._getEventsAtLevel(level).stream())
                            .collect(Collectors.toList());
    }

    /**
     * reset ALL TestLogger instances
     */
    public static void resetAll() {
        testLoggerMap.values().forEach(TestLogger::reset);
    }

    // instance-level fields
    //
    // lists of LogEvents by Level
    private final Map<Level, List<LogEvent>> logEventMap = new ConcurrentHashMap<>();
    // TestLogger-level callback to observe LogEvents
    private Consumer<LogEvent> onEventConsumer;
    // the log-level for this TestLogger
    private Level logLevel = null;

    // instance-level methods
    //
    public TestLogger(String name) {
        this.name = name;
    }

    /**
     * sets the log level for this TestLogger
     */
    public TestLogger setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public Level getLogLevel() {
        return logLevel != null ? logLevel : defaultLogLevel;
    }

    /**
     * sets the onEvent consumer for this logger instance
     */
    public TestLogger setOnEvent(Consumer<LogEvent> onEvent) {
        onEventConsumer = onEvent != null ? onEvent : defaultOnEventConsumer;
        return this;
    }

    /**
     * returns a copy of the logEventMap to avoid thread-safety issues caused by exposing internal data
     */
    public Map<Level, List<LogEvent>> getLogEventMap() {
        return Map.copyOf(logEventMap);
    }

    /**
     * returns a copy of the `List<LogEvent>` to avoid thread-safety issues caused by exposing internal data
     */
    public List<LogEvent> getEventsAtLevel(Level level) {
        List<LogEvent> eventList = logEventMap.get(level);
        return eventList != null ? List.copyOf(eventList) : List.of();
    }

    /**
     * returns a list of all LogEvents across all levels for this logger
     */
    public List<LogEvent> getAllEvents() {
        return logEventMap.values().stream()
                          .flatMap(List::stream)
                          .sorted(EVENTS_BY_ID) // so they're returned chronologically
                          .collect(Collectors.toList());
    }

    /**
     * removes the log event list of the specified level
     */
    public void clearEventsAtLevel(Level level) {
        List<LogEvent> eventList = logEventMap.get(Objects.requireNonNull(level, "null level not allowed"));
        if (eventList != null) {
            // to prevent concurrent mod of this list by both this method and addEvent(LogEvent)
            synchronized (eventList) {
                eventList.clear();
            }
        }
    }

    public void reset() {
        logEventMap.clear();
    }

    /**
     * used with TWR blocks to reset this logger when the TWR block is closed
     */
    public NoThrowAutoCloseable resetOnClose() {
        final Level startingLogLevel = this.logLevel;
        final Consumer<LogEvent> startingConsumer = this.onEventConsumer;
        return NoThrowAutoCloseable.of(() -> {
            this.logLevel = startingLogLevel;
            this.setOnEvent(startingConsumer);
            reset();
        });
    }

    private void addEvent(LogEvent event) {
        List<LogEvent> eventList = logEventMap.computeIfAbsent(event.getLevel(), ignore -> new ArrayList<>());
        // used to prevent concurrent-mod with clearEventsAtLevel()
        synchronized (eventList) {
            eventList.add(event);
        }
        if (onEventConsumer != null) {
            onEventConsumer.accept(event);
        } else {
            defaultOnEventConsumer.accept(event);
        }
    }

    private List<LogEvent> _getEventsAtLevel(Level level) {
        return logEventMap.getOrDefault(level, List.of());
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return "";
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String message, Object[] msgArgs, Throwable throwable) {
        addEvent(new LogEvent(level, getName(), marker, message, msgArgs, throwable));
    }

    @Override
    public boolean isTraceEnabled() {
        return getLogLevel().toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isDebugEnabled() {
        return getLogLevel().toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isInfoEnabled() {
        return getLogLevel().toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isWarnEnabled() {
        return getLogLevel().toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isErrorEnabled() {
        return getLogLevel().toInt() <= Level.ERROR.toInt();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }
}
