# JUnit test listener

This is
the [Junit Test Execution Listener](https://junit.org/junit5/docs/5.0.3/api/org/junit/platform/launcher/TestExecutionListener.html)
to write testresults to an Influx database.

## Install via Maven

```
    <dependency>
        <groupId>com.github.vanbadasselt</groupId>
        <artifactId>junit-listener-influxdb</artifactId>
        <version>${junit-listener-influxdb.version}</version>
        <scope>test</scope>
    </dependency>
```

## Usage

If you imported this library and you run your tests with JUnit 5 (JUnit Jupiter), then this JUnit Test Execution
Listener will run automatically due to a Service Loader.

### Usage local

If you want to run the tests with this listener locally, make sure to set the environment
variable `TEST_LISTENER_ENABLED` to true. Default influx properties point to the instance `localhost:8086` with
credentials `root/root`. So, if no other environment variables are set for the influx
properties (`TEST_LISTENER_INFLUX_HOST`, `TEST_LISTENER_INFLUX_PORT`, `TEST_LISTENER_INFLUX_USER`
, `TEST_LISTENER_INFLUX_PASSWORD`, `TEST_LISTENER_INFLUX_DB`), make sure to install and run a local influx db instance
with a database `testresults` created.

### Usage CI/CD

Set the environment variables `TEST_LISTENER_ENABLED` default to true. Set this environment variable to false in the
jobs where you don't need the test results (branch/merge jobs for example). Set the right influx properties globally for
the instance in your landscape (`TEST_LISTENER_INFLUX_HOST`, `TEST_LISTENER_INFLUX_PORT`, `TEST_LISTENER_INFLUX_USER`
, `TEST_LISTENER_INFLUX_PASSWORD`, `TEST_LISTENER_INFLUX_DB`). In Gitlab this is possible via the global CI/CD settings.

## Extracted data

Some data is extracted based on a particular situation. See below for these kinds of data, check and make this
applicable for your situation if necessary.

### Release version

This listener extracts the release version of based on the `CI_RELEASE_VERSION` variable. Add the script below to your
pipeline yaml and find the right version back in your InfluxDB!

```
    - git fetch --all --tags
    - export CI_RELEASE_VERSION=$(git describe --tags `git rev-list --tags --max-count=1`)
```

### Application name

This listener extracts the application name based on the Gitlab predefined variable `CI_PROJECT_NAME` or the 3rd part of
the source path.

### Test Metadata

Within this listener you can create a generic collection of your test metadata; feature and risk categories for example.
This is how you can categorize your fixture or test:
Check out `TestMetadata` to add your own features or other categories.

```
@Tag(TestMetadata.Risk.HIGH)
@Tag(TestMetadata.Feature.LOGIN)
```

This metadata is saved in the database, in order to filter or group on it across multiple applications. You can create a
dashboard per feature category for example!

### Test Name

If there is a test name defined with `@DisplayName`, this will automatically be saved as test name. If not, the method
name will be split based on camel case format.

### Test Type

Unit, component and integration tests are defined as test types and based on the file name or location of the tests. If
the file ends with Test, it's a unit test. If it's situated in the directory `component`, it's a component test and the
same goes for the `integration` directory.

## Author

anaisvanasselt (https://linkedin.com/in/anais-van-asselt)
Thanks to Burak Yurdakul for his contribution!
