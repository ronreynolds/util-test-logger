package com.ronreynolds.test.logging;

import static com.ronreynolds.test.logging.LogEventListAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ronreynolds.util.classes.ClassInfo;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;

class TestLoggerTest {
    static {
        // default MDC adapter when there isn't one available; force in the basic version instead
        if (MDC.getMDCAdapter() instanceof NOPMDCAdapter) {
            Class<?> mdcClass = MDC.class;
            try {
                Field mdcAdapter;
                try {
                    mdcAdapter = mdcClass.getDeclaredField("MDC_ADAPTER");  // 2.0.17
                } catch (NoSuchFieldException fnf) {
                    mdcAdapter = mdcClass.getDeclaredField("mdcAdapter");   // 2.0.16 and before used "mdcAdapter"
                }
                mdcAdapter.setAccessible(true);
                mdcAdapter.set(null, new BasicMDCAdapter());
            } catch (ReflectiveOperationException fail) {
                assertThat(fail).as("info:%s", ClassInfo.getClassInfo(mdcClass)).isNull();  // obviously this will fail
            }
        }
    }

    private static final String LOGGER_NAME = "Test-" + Thread.currentThread().getName();
    private static final TestLogger log = TestLogger.getLogger(LOGGER_NAME);

    @Test
    void testLogger_works() {
        Level startingLevel = log.getLogLevel();
        assertThat(startingLevel).isNotEqualTo(Level.TRACE);

        try (var ignore = log.resetOnClose()) {
            AtomicInteger logCount = new AtomicInteger();
            log.setOnEvent(event -> logCount.incrementAndGet());
            var logCountAssert = assertThat(logCount);

            log.setLogLevel(Level.TRACE);
            assertThat(log).isNotNull();
            assertThat(log.getAllEvents()).isEmpty();
            logCountAssert.hasValue(0);

            log.info("test message with answer:{}", 42);
            assertThat(log, Level.INFO)
                .isNotEmpty()
                .firstLogEvent()
                .isLevel(Level.INFO)
                .hasMessageTemplate("test message with answer:{}")
                .hasFormattedMessage("test message with answer:42")
                .containsMessageArgs(42)
                .hasLoggerName(LOGGER_NAME)
                .contextMapMatches(Map::isEmpty)
                .hasMarker(null)
                .hasThrown(null);
            logCountAssert.hasValue(1);
            log.clearEventsAtLevel(Level.INFO);

            Throwable t = new Throwable();
            Marker marker = new BasicMarkerFactory().getDetachedMarker("fred");
            log.trace("trace message");
            logCountAssert.hasValue(2);
            log.debug("debug message");
            logCountAssert.hasValue(3);
            log.info("info message");
            logCountAssert.hasValue(4);
            log.warn(marker, "warn message");
            logCountAssert.hasValue(5);
            try (var ignore2 = MDC.putCloseable("foo", "bar")) {
                log.error(marker, "error message - {} {}", 1, 2, t);
                logCountAssert.hasValue(6);
            }

            assertThat(log, Level.TRACE).hasSize(1).firstLogEvent().isLevel(Level.TRACE).hasFormattedMessage("trace message");
            assertThat(log, Level.DEBUG).hasSize(1).firstLogEvent().isLevel(Level.DEBUG).hasFormattedMessage("debug message");
            assertThat(log, Level.INFO).hasSize(1).firstLogEvent().isLevel(Level.INFO).hasFormattedMessage("info message");
            assertThat(log, Level.WARN).hasSize(1).firstLogEvent().isLevel(Level.WARN).hasFormattedMessage("warn message")
                                       .hasMarker(marker);

            // has basically everything
            var logEventListAssert = assertThat(log, Level.ERROR);
            logEventListAssert.hasSize(1);
            var logEventAssert = logEventListAssert.firstLogEvent();
            assertThat(logEventListAssert.lastLogEvent()).isNotNull();
            assertThat(logEventListAssert.logElement(0)).isNotNull();
            logEventAssert.isLevel(Level.ERROR)
                          .hasFormattedMessage("error message - 1 2")
                          .hasMessageTemplate("error message - {} {}")
                          .containsMessageArgs(1, 2)
                          .messageArgsMatch(args -> args.length == 2 && args[0] == Integer.valueOf(1) && args[1] == Integer.valueOf(2))
                          .hasMarker(marker)
                          .hasThrown(t)
                          .hasThreadName(Thread.currentThread().getName())
                          .hasLoggerName(LOGGER_NAME)
                          .contextMapContainsOnly("foo", "bar")
                          .contextMapContains("foo", "bar")
                          .contextMapContains("foo", "bar"::equals)
                          .contextMapContains(Map.entry("foo", "bar"))
            ;

            // test failure cases
            assertThrows(AssertionError.class, () -> logEventAssert.isLevel(Level.INFO));
            assertThrows(AssertionError.class, () -> logEventAssert.hasFormattedMessage(null));
            assertThrows(AssertionError.class, () -> logEventAssert.hasMessageTemplate(null));
            assertThrows(AssertionError.class, () -> logEventAssert.containsMessageArgs(42));
            assertThrows(AssertionError.class, () -> logEventAssert.hasMarker(null));
            assertThrows(AssertionError.class, () -> logEventAssert.hasThrown(null));
            assertThrows(AssertionError.class, () -> logEventAssert.hasThreadName("foo"));
            assertThrows(AssertionError.class, () -> logEventAssert.hasLoggerName("bar"));
            assertThrows(AssertionError.class, () -> logEventAssert.contextMapContains("fuzzy", "wuzzy"));
            assertThrows(AssertionError.class, () -> logEventAssert.contextMapDoesNotContain("foo"));
            assertThrows(AssertionError.class, () -> logEventAssert.contextMapContainsOnly(Map.entry("foo", "bar"),
                                                                                           Map.entry("fuzzy", "wuzzy")));
        }
        // verify that resetOnClose.close() worked
        assertThat(log.getLogLevel()).isSameAs(startingLevel);
        assertThat(log.getAllEvents()).isEmpty();
    }
}