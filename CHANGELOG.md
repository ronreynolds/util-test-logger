# util-test-logger changelog
* based on https://keepachangelog.com/en/1.0.0/ and https://semver.org/
* sections: **Breaking** **Added** **Changed** **Deprecated** **Fixed** **Removed** **Security** **ToDo** (in that order)

## 1.0.2 - unreleased
### ToDo
* consider including `org.slf4j.impl.StaticLoggerBinder` and `StaticMDCBinder` so this library can be detected as a slf4j provider
  * also look into SLF4J-2+ service-provider mechanism

## 1.0.1 - 2025-03-30
### Added
* `LogEventAssert.contextMapContainsOnly()` methods `(String,String)` and `(Map.Entry<String,String>...)`
* every method of `LogEventAssert` and `LogEventListAssert` to the `TestLoggerTest` unit-test along with failure-case tests

## 1.0.0 - 2025-03-29
### Added
* `com.ronreynolds.test.logging`
  * `LogEvent` - holds the data about the logging event
  * `LogEventAssert` - AssertJ-style class to make assertions about `LogEvent`
  * `LogEventListAssert` - AssertJ-style class to make assertions about `List<LogEvent>`
  * `NoThrowAutoCloseable` - surprisingly useful simple class to use TWR blocks to invoke code when the block ends
  * `TestLogger` - an implementation of SLF4J `Logger` that makes it easy to make assertions about Logging
    * also includes unit-test that verifies all the above classes work as expected
* `com.conreynolds.util.classes.ClassInfo` - returns info about a `Class` (used to debug issue with SLF4J 2.0.17 vs 2.0.16) 
