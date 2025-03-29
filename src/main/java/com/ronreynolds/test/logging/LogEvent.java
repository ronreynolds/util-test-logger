package com.ronreynolds.test.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

/**
 * based on log4j2's LogEvent
 */
public class LogEvent {
    private static final AtomicInteger idCount = new AtomicInteger();

    private final int eventId = idCount.incrementAndGet(); // for sorting LogEvent by occurrence order
    private final Map<String, String> contextMap;
    private final Level level;
    private final String loggerName;
    private Marker marker;
    private final String message;
    private Object[] messageArgs;
    private final long timeMillis;
    private final StackTraceElement source;
    private final String threadName;
    private Throwable thrown;

    LogEvent(Level level, String loggerName, String message) {
        this(level, loggerName, null, message, null, null);
    }

    LogEvent(Level level, String loggerName, Marker marker, String message, Object[] msgArgs, Throwable thrown) {
        Map<String,String> mdc = MDC.getCopyOfContextMap(); // some implementations return a null Map if there's no MDC data
        this.contextMap = mdc != null ? mdc : Map.of();
        this.level = level;
        this.loggerName = loggerName;
        this.marker = marker;
        this.message = message;
        this.timeMillis = System.currentTimeMillis();
        this.source = getStackTopElement(thrown).orElse(null);
        this.threadName = Thread.currentThread().getName();
        if (thrown != null) {
            this.thrown = thrown;
            this.messageArgs = msgArgs;
        } else {
            this.thrown = extractThrownLastArg(msgArgs);
            if (this.thrown == null) {
                this.messageArgs = msgArgs;
            } else {
                if (msgArgs != null && msgArgs.length > 1) {
                    this.messageArgs = Arrays.copyOf(msgArgs, msgArgs.length - 1);
                } else {
                    // msgArgs only contained 1 arg which was the throwable
                    this.messageArgs = null;
                }
            }
        }
    }

    LogEvent withMsgArgs(Object arg) {
        this.messageArgs = new Object[]{arg};
        return this;
    }

    LogEvent withMsgArgs(Object arg1, Object arg2) {
        this.messageArgs = new Object[]{arg1, arg2};
        return this;
    }

    LogEvent withMsgArgs(Object[] args) {
        this.messageArgs = args;
        return this;
    }

    LogEvent withThrown(Throwable t) {
        this.thrown = t;
        return this;
    }

    LogEvent withMarker(Marker marker) {
        this.marker = marker;
        return this;
    }

    public Map<String, String> getContextMap() {
        return contextMap;
    }

    public Level getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Marker getMarker() {
        return marker;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage() {
        return MessageFormatter.arrayFormat(message, messageArgs).getMessage();
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    private String getTimeString() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.of("UTC")));
    }

    public StackTraceElement getSource() {
        return source;
    }

    public String getThreadName() {
        return threadName;
    }

    public Throwable getThrown() {
        return thrown;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return String.format("%5s %s [%s] %s - %s%s", level, getTimeString(), threadName, loggerName, getFormattedMessage(),
                             thrown != null ? "\n" + getThrownData() : "");
    }

    CharSequence getThrownData() {
        Objects.requireNonNull(thrown);
        StringBuilder buf = new StringBuilder();
        buf.append(thrown);
        int stackDepthLimit = Integer.getInteger("TestLogger.stackLimit", 10);
        StackTraceElement[] stack = thrown.getStackTrace();
        Arrays.stream(stack).limit(stackDepthLimit).forEach(element -> buf.append("\n\t@ ").append(element));
        if (stack.length > stackDepthLimit) {
            buf.append("\n\t...");
        }
        return buf;
    }

    static Optional<StackTraceElement> getStackTopElement(Throwable t) {
        if (t == null) {
            return Optional.empty();
        }
        StackTraceElement[] stack = t.getStackTrace();
        if (stack == null || stack.length == 0) {
            return Optional.empty();
        }
        return Optional.of(stack[0]);
    }

    static Throwable extractThrownLastArg(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object lastArg = args[args.length - 1];
        return lastArg instanceof Throwable ? (Throwable) lastArg : null;
    }
}

