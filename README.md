# JUnit test listener

This is the [Junit Test Execution Listener](https://junit.org/junit5/docs/5.0.3/api/org/junit/platform/launcher/TestExecutionListener.html) to write testresults to an Influx database. 

## Install via Maven

```
    <dependency>
        <groupId>com.github.vanbadasselt</groupId>
        <artifactId>junit-test-listener</artifactId>
        <version>${junit-test-listener.version}</version>
        <scope>test</scope>
    </dependency>
```

## Usage

If you imported this library and you run your tests with JUnit 5 (JUnit Jupiter), then this JUnit Test Execution Listener will run automatically via the Service Loader. 

### Extract release version

Export the release version of your application via the `CI_RELEASE_VERSION` variable. Add this script to your pipeline yaml and find it back in your InfluxDB!

```
    - git fetch --all --tags
    - export CI_RELEASE_VERSION=$(git describe --tags `git rev-list --tags --max-count=1`)
```

### Test Metadata

Within this listener you can create a generic collection of your test metadata; feature and risk categories for example. This is how you can categorize your fixture or test:
Check out `TestMetadata` to add your own features or other categories.

```
@Tag(TestMetadata.Risk.HIGH)
@Tag(TestMetadata.Feature.LOGIN)
```

This metadata is saved in the database, in order to filter or group on it across multiple applications. You can create a dashboard per feature category for example!

## TODO

### Influx properties
Set the influx properties as environment variables. I couldn't make it work so far due to the static context.

### Deactivating
It should be possible to deactivate this listener with the configuration parameter `junit.platform.execution.listeners.deactivate="*.TestListenerInfluxDb""`
NOTE: I don't have this seen working yet.

## Author
anaisvanasselt (https://linkedin.com/in/anais-van-asselt)
a big thanks to Burak Yurdakul for your contribution!
