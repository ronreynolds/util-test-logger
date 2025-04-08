# util-test-logger
an slf4j impl useful for testing logging

## TestLogger
### public methods
#### static methods
* `getLogger(Class<?>): TestLogger`
  * return a cached `TestLogger` instance by class-name
* `getLogger(String): TestLogger`
  * return a cached `TestLogger` instance by name
* `setDefaultLoggerLevel(Level): void`
  * set the starting log level for new `TestLogger` instances
* `setOnAllEvents(Consumer<LogEvent>): void`
  * set the default on-event callback for new `TestLogger` instances
* `getAllEventsAtLevel(Level): List<LogEvent>`
  * gather all the events of a particular level across all `TestLogger` instances and return as a `List`
* `resetAll(): void`
  * reset log events in all `TestLogger` instances (leaves all other settings as-is: level, on-event callback)
#### non-static methods
* `TestLogger(String)`
  * creates a new (and uncached) `TestLogger` with the specified name
* `setLogLevel(Level): TestLogger`
  * sets the log-level for this `TestLogger`; `this` returned for method chaining
* `getLogLevel(): Level`
  * returns the log-level for this `TestLogger`, if set; otherwise returns the global default log-level
* `setOnEvent(Consumer<LogEvent>): TestLogger`
  * sets a callback to be invoked on every qualified `LogEvent` received by this `TestLogger`; `this` returned for method chaining
  * pass in `null` to reset on-event callback to the global default on-event callback
* `getLogEventMap(): Map<Level, List<LogEvent>>`
  * return a copy of the map of events already received by this `TestLogger`; never returns `null`
* `getEventsAtLevel(Level): List<LogEvent>`
  * return a copy of the `LogEvent`s received at a particular level in the order they occurred; never returns `null`
* `getAllEvents(): List<LogEvent>`
  * return a list of all `LogEvent`s received by this `TestLogger` sorted in the order they occurred; never returns `null`
* `clearEventsAtLevel(Level): void`
  * clears the list of events at a particular level for this `TestLogger` in a thread-safe way
* `reset(): void`
  * clears all `LogEvent`s received by this `TestLogger`
* `resetOnClose(): NoThrowAutoCloseable`
  * returns an `AutoCloseable` that can be used in a Try-With-Resources (TWR) block to reset the `TestLogger` when closed
  * also resets the `TestLogger`'s log-level and on-event callback to pre-TWR values

## LogEventAssert
### public methods
#### static methods
* `assertThat(LogEvent): LogEventAssert`
  * factory method to create a `LogEventAssert` for a `LogEvent`
#### non-static methods
* `containsMessageArgs(Object...): LogEventAssert`
  * assert that the event contains the specified var-args in any order
* `contextMapContains(String, String): LogEventAssert`
  * assert that the MDC has the specified key-value pair when the event was logged
* `contextMapContainsOnly(String, String): LogEventAssert`
  * assert that the MDC had ONLY the specified key-value pair when the event was logged
* `contextMapContains(String, Predicate<String>): LogEventAssert`
  * assert that the MDC when the event was logged has a key with value that matches the `Predicate`
* `contextMapContains(Entry<String,String>...): LogEventAssert`
  * assert that the MDC contained all the provided key-value pairs when the event was logged
* `contextMapContainsOnly(Entry<String,String>...): LogEventAssert`
  * assert that the MDC contained ONLY the provided key-value pairs when the event was logged
* `contextMapDoesNotContain(String): LogEventAssert`
  * assert that the MDC did NOT contain the specified key when the event was logged
* `contextMapMatches(Predicate(? super Map<String,String>>): LogEventAssert`
  * assert that the MDC when the event was logged matches the provided `Predicate`
* `hasFormattedMessage(String): LogEventAssert`
  * assert that the message after parameter substitution matches the specified value
* `hasLoggerName(String): LogEventAssert`
  * assert that the event was logged to a logger with the specified name
* `hasMarker(marker): LogEventAssert`
  * assert that the event was logged with the specified marker
* `hasMessageTemplate(String): LogEventAssert`
  * assert that the original log message (potentially with `{}` parameter markers) matches the specified value
* `hasThreadName(String): LogEventAssert`
  * assert that the specified thread logged the event
* `hasThrown(Throwable): LogEventAssert`
  * assert that the event contained the specified `Throwable`
* `isLevel(Level): LogEventAssert`
  * assert that the event was of the specified level 
* `messageArgsMatch(Predicate<Object[]>): LogEventAssert`
  * assert that the var-args provided when the event was created match the provided `Predicate`

## LogEventListAssert
### public methods
#### static methods
* `assertThat(List<LogEvent>): LogEventListAssert`
  * this version is a little less useful than the 2-arg version because it must be invoked via `LogEventListAssert.assertThat` if 
the common `org.assertj.core.api.Assertions.assertThat` is statically imported (which is always the case with AssertJ-using code)
* `assertThat(TestLogger, Level): LogEventListAssert`
  * this version (which differs in args from `Assertions.assertThat(T)`) allows us to import both statically and use this version thus:
`var logEventListAssert = assertThat(log, Level.ERROR);`

## example usage
```java
    // uses TestLogger as factory rather than TestLoggerFactory for conciseness
    private static final TestLogger log = TestLogger.getLogger(LOGGER_NAME);

    void foo() {
        // returns org.slf4j.event.Level (part of slf4j-api; makes fewer new classes)
        Level startingLevel = log.getLogLevel();
        // NoThrowAutoCloseable that resets log on close to set back log-level and clear all log-events
        try (var ignore = log.resetOnClose()) {
            // crank up logging level so we can test all the things
            log.setLogLevel(Level.TRACE);

            AtomicInteger logCount = new AtomicInteger();
            // sets an on-log-event callback (if the event's level meets or exceeds the logger's log-level)
            log.setOnEvent(event -> logCount.incrementAndGet());
            
            // MDC.putCloseable(k,v) returns an auto-closeable that adds the specified key-value pair and removes it on close()
            try (var ignore2 = MDC.putCloseable("foo", "bar")) {
                log.error(marker, "error message - {} {}", 1, 2, t);
            }
            
            // creates a LogEventListAssert of all the errors in the log
            var logEventListAssert = assertThat(log, Level.ERROR);

            // assert that there's only 1 log event in the list
            logEventListAssert.hasSize(1);
            // assert that even with just 1 event in the list the last and 0th event return non-null
            // NOTE - if the list is empty first(), last(), and element() throw AssertionError so check isNotEmpty() first
            assertThat(logEventListAssert.last()).isNotNull();
            assertThat(logEventListAssert.element(0)).isNotNull();
            
            // get LogEventAssert for first element of list
            var logEventAssert = logEventListAssert.first();
            // you can assert about everything in a log-event
            logEventAssert.isLevel(Level.ERROR)
                    .hasFormattedMessage("error message - 1 2")
                    .hasMessageTemplate("error message - {} {}")
                    .messageArgsHasSize(2)
                    .messageArgIsEqualTo(0, 1)
                    .messageArgIsEqualTo(1, 2)
                    .hasMarker(marker)
                    .hasThrown(t)
                    .hasThreadName(Thread.currentThread().getName())
                    .hasLoggerName(LOGGER_NAME)
                    .contextMapContainsOnly("foo", "bar")
            ;
        }
        // verify that resetOnClose.close() worked
        assertThat(log.getLogLevel()).isSameAs(startingLevel);
        assertThat(log.getAllEvents()).isEmpty();
    }
```