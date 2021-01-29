package com.github.vanbadasselt;

import com.github.vanbadasselt.config.InfluxDb;
import org.influxdb.dto.Point;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
public class TestListenerInfluxDb implements TestExecutionListener {

    private static final String TYPE_UNIT = "UT";
    private static final String TYPE_COMPONENT = "CT";
    private static final String TYPE_INTEGRATION = "IT";
    private static final String UNKNOWN = "UNK";
    private static final String ENV_RELEASE_VERSION = "CI_RELEASE_VERSION";
    private static final String OK = "SUCCESSFUL";
    private static final String NOK = "FAILED";

    private final TestResultsSender testResultsSender;
    private final AtomicInteger testCasesFailed = new AtomicInteger();
    private final AtomicInteger testCasesSkipped = new AtomicInteger();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");
    private final Logger log = Logger.getLogger(TestListenerInfluxDb.class.getName());

    private String testType = UNKNOWN;
    private String application = UNKNOWN;
    private String riskTag = UNKNOWN;
    private String featureTag = UNKNOWN;
    private String releaseVersion = UNKNOWN;
    private ZonedDateTime startTimeTest;
    private ZonedDateTime startTimeTestRun;

    public TestListenerInfluxDb() {
        this.testResultsSender = new TestResultsSender();
    }

    /**
     * Called when the execution of the TestPlan has started,
     * before any test has been executed.
     *  @param testPlan            - describes the tree of tests about to be executed
     */
    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        startTimeTestRun = startTimer();
        setReleaseVersion();
        log.info("Starting JUnit test listener, writing results to InfluxDB " + testResultsSender.getConnectionUrl());
        log.info("For application of version " + releaseVersion);
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan is about to be started.
     *  @param testIdentifier      - the identifier of the started test or container
     */
    @Override
    public void executionStarted(final TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            startTimeTest = startTimer();
            // When a new test class begins
        } else if (testIdentifier.getParentId().isPresent()) {
            defineTestType(testIdentifier.getDisplayName());
            if (application.equals(UNKNOWN)) {
                defineApplication(testIdentifier.getLegacyReportingName());
            }
        }

    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has been skipped.
     * @param testIdentifier      - the identifier of the started test or container
     * @param reason              - a human-readable message describing why the execution has been skipped
     */
    @Override
    public void executionSkipped(final TestIdentifier testIdentifier, final String reason) {
        testCasesSkipped.incrementAndGet();
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of the outcome.
     *
     * @param testIdentifier      - the identifier of the finished test or container
     * @param testExecutionResult - the (unaggregated) result of the execution for the supplied TestIdentifier
     */
    @Override
    public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult testExecutionResult) {
        if (testIdentifier.isContainer()) {
            return;
        }
        final long durationTest = getDuration(startTimeTest);

        final TestExecutionResult.Status status = testExecutionResult.getStatus();
        final String testCaseNameReadable = splitCamelCase(testIdentifier.getDisplayName());
        final String errorMessage = getErrorMessage(testExecutionResult, status);
        setTestTags(testIdentifier);

        // Create a table with the results of 1 test
        final Point point = Point.measurement(InfluxDb.TABLE_NAME_TEST)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("application", application)
                .tag("testType", testType)
                .tag("feature", featureTag)
                .tag("risk", riskTag)
                .tag("result", status.name())
                .addField("testName", testCaseNameReadable)
                .addField("durationMs", durationTest)
                .addField("errorMessage", errorMessage)
                .addField("releaseVersion", releaseVersion)
                .build();

        testResultsSender.setPoint(point);
    }

    /**
     * Called when the execution of the TestPlan has finished,
     * after all tests have been executed.
     */
    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        final long durationTestRun = getDuration(startTimeTestRun);
        final String result = testCasesFailed.intValue() > 0 ? NOK : OK;
        final Long testCases = testPlan.countTestIdentifiers(TestIdentifier::isTest);

        if(testCases != 0) {
            // Create a table with the results of all tests
            final Point point = Point.measurement(InfluxDb.TABLE_NAME_RUN)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("application", application)
                    .tag("result", result)
                    .tag("testType", testType)
                    .addField("name", startTimeTestRun.format(formatter))
                    .addField("testCases", testCases)
                    .addField("testCasesFailed", testCasesFailed)
                    .addField("testCasesSkipped", testCasesSkipped)
                    .addField("durationMs", durationTestRun)
                    .addField("releaseVersion", releaseVersion)
                    .build();

            testResultsSender.setPoint(point);
        }

        testResultsSender.post();
        log.info("JUnit5 test listener is done, closing the InfluxDB connection");
        testResultsSender.closeConnection();
    }

    private String getErrorMessage(final TestExecutionResult testExecutionResult, final TestExecutionResult.Status status) {
        if (!TestExecutionResult.Status.SUCCESSFUL.equals(status) && testExecutionResult.getThrowable().isPresent()) {
            testCasesFailed.incrementAndGet();
            return testExecutionResult.getThrowable().get().getMessage();
        }
        return "NA";
    }

    /**
     * Determine the test type based on the path directory of your tests
     * @param testSource of the project
     */
    private void defineTestType(final String testSource) {
        if (testSource.endsWith("Test")) {
            testType = TYPE_UNIT;
        } else if (testSource.endsWith(TYPE_COMPONENT)) {
            testType = TYPE_COMPONENT;
        } else if (testSource.endsWith(TYPE_INTEGRATION)) {
            testType = TYPE_INTEGRATION;
        } else {
            testType = UNKNOWN;
        }
    }

    /**
     * Determine the application name based on the path of your project
     * @param testSource of the project
     */
    private void defineApplication(final String testSource) {
        if (testSource.contains("goodsdeclaration")) {
            application = "GOODS_DECLARATION";
        } else if (testSource.contains("clearance")) {
            application = "CLEARANCE";
        } else if (testSource.contains("com.portofrotterdam.hamis")) {
            application = "VISIT";
        }
        else {
            application = UNKNOWN;
        }
    }

    /**
     * Saves the metadata you add to your JUnit test or test class, check out the readme for an example
     * @param testIdentifier which contains the test tag
     */
    private void setTestTags(final TestIdentifier testIdentifier) {
        final Set<TestTag> tags = testIdentifier.getTags();

        if (!tags.isEmpty()) {
            tags.forEach(t -> {
                final String tag = t.getName();
                switch (tag) {
                case "SMOKE":
                case "HIGH":
                case "MEDIUM":
                case "LOW":
                    riskTag = tag;
                    break;
                default:
                    featureTag = tag;
                    break;
                }
            });
        }
    }

    /**
     * Saves the release version of your project when available, check out the readme to export this env var
     */
    private void setReleaseVersion() {
        if(System.getenv(ENV_RELEASE_VERSION) != null && !System.getenv(ENV_RELEASE_VERSION).isEmpty()){
            releaseVersion = System.getenv(ENV_RELEASE_VERSION);
        }
    }

    private ZonedDateTime startTimer() {
        return ZonedDateTime.now();
    }

    private Long getDuration(final ZonedDateTime startTime) {
        return Duration.between(startTime, ZonedDateTime.now()).toMillis();
    }

    /**
     * Split name of the test method into a readable sentence
     *
     * @param replaceString method to split
     * @return formatted test method name
     */
    private static String splitCamelCase(final String replaceString) {
        return replaceString.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }
}
