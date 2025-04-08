package com.ronreynolds.test.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ronreynolds.test.logging.LogEventListAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestLoggerTest {
    // by default the MDC-adapter is a No-Op; we need something so we can test MDC context values
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
    @SuppressWarnings("unchecked")
    void testLogger_works() {
        Level startingLevel = log.getLogLevel();
        assertThat(startingLevel).isNotEqualTo(Level.TRACE);

        try (var ignore = log.resetOnClose()) {
            AtomicInteger logCount = new AtomicInteger();
            log.setOnEvent(event -> logCount.incrementAndGet());
            // asserts are actually reusable so we create this one once and reassert on it below
            var logCountAssert = assertThat(logCount);

            log.setLogLevel(Level.TRACE);
            assertThat(log).isNotNull();
            assertThat(log.getAllEvents()).isEmpty();
            logCountAssert.hasValue(0);

            log.info("test message with answer:{}", 42);
            assertThat(log, Level.INFO)
                    .isNotEmpty()
                    .first()
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
            assertThat(log, Level.TRACE).hasSize(1).first().isLevel(Level.TRACE).hasFormattedMessage("trace message");

            log.debug("debug message");
            logCountAssert.hasValue(3);
            assertThat(log, Level.DEBUG).hasSize(1).first().isLevel(Level.DEBUG).hasFormattedMessage("debug message");

            log.info("info message");
            logCountAssert.hasValue(4);
            assertThat(log, Level.INFO).hasSize(1).first().isLevel(Level.INFO).hasFormattedMessage("info message");

            log.warn(marker, "warn message");
            logCountAssert.hasValue(5);
            assertThat(log, Level.WARN).hasSize(1).first().isLevel(Level.WARN).hasFormattedMessage("warn message")
                    .hasMarker(marker);

            try (var ignore2 = MDC.putCloseable("foo", "bar")) {
                log.error(marker, "error message - {} {}", 1, 2, t);
                logCountAssert.hasValue(6);
            }

            // has basically everything
            var logEventListAssert = assertThat(log, Level.ERROR);
            logEventListAssert.hasSize(1);
            var logEventAssert = logEventListAssert.first();
            assertThat(logEventListAssert.last()).isNotNull();
            assertThat(logEventListAssert.element(0)).isNotNull();
            logEventAssert.isLevel(Level.ERROR)
                    .hasFormattedMessage("error message - 1 2")
                    .hasMessageTemplate("error message - {} {}")
                    .containsMessageArgs(1, 2)
                    .messageArgsHasSize(2)
                    .messageArgIsEqualTo(0, 1)
                    .messageArgIsEqualTo(1, 2)
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

    @Test
    void emptyListAssertions() {
        var emptyListAssert = assertThat(log, Level.INFO);
        emptyListAssert.isEmpty();
        emptyListAssert.hasSize(0);
        // don't call these methods on empty lists (this is default assertJ behavior)
        assertThat(assertThrows(AssertionError.class, emptyListAssert::first))
                .hasMessageContaining("Expecting actual not to be empty");
        assertThat(assertThrows(AssertionError.class, emptyListAssert::last))
                .hasMessageContaining("Expecting actual not to be empty");
        assertThat(assertThrows(AssertionError.class, () -> emptyListAssert.element(42)))
                .hasMessageContaining("Expecting actual not to be empty");
    }

    /**
     * used for finding info about classes (class-file location, fields, etc);
     * not part of library's domain so made private
     */
    private static class ClassInfo {
        // added to try to debug issues finding a MDC class with an mdcAdapter field :-/
        public static String findClass(Class<?> classToFind) {
            ClassLoader loader = classToFind.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
            URL classFileUrl = loader.getResource(classToFind.getName().replace('.', '/').concat(".class"));
            return classFileUrl != null ? classFileUrl.toExternalForm() : classToFind + " not found";
        }

        public static CharSequence getClassInfo(Class<?> classToFind) {
            StringBuilder classInfo = new StringBuilder();
            classInfo.append("\nname:").append(classToFind.getName())
                    .append("\nloc:").append(findClass(classToFind))
                    .append("\nfields:")
                    .append(Stream.of(classToFind.getFields()).map(Field::getName).collect(Collectors.joining(",")))
                    .append("\ndec-fields:")
                    .append(Stream.of(classToFind.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(",")));
            return classInfo;
        }
    }
}

