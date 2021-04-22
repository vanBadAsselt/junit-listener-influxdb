package com.github.vanbadasselt;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
public class JUnitListenerInfluxDb implements TestExecutionListener {

    private static final String ENV_ENABLE_TEST_LISTENER = "TEST_RESULTS_ENABLED";
    private static final String UNKNOWN = "UNK";
    private final boolean isListenerEnabled;
    private final Logger log = Logger.getLogger(JUnitListenerInfluxDb.class.getName());

    private final InfluxDbSender influxDbSender;
    private final TestDataProcessor testDataProcessor;

    public JUnitListenerInfluxDb() {
        this.isListenerEnabled = Boolean.TRUE.toString().equals(System.getenv(ENV_ENABLE_TEST_LISTENER));
        log.info("JUnit Test Listener on? " + this.isListenerEnabled);

        this.influxDbSender = new InfluxDbSender();
        this.testDataProcessor = new TestDataProcessor();
    }

    /**
     * Called when the execution of the TestPlan has started,
     * before any test has been executed.
     *
     * @param testPlan - describes the tree of tests about to be executed
     */
    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        if (isListenerEnabled) {
            testDataProcessor.startTimeTestRun();
            testDataProcessor.setReleaseVersion();

            log.info("Starting JUnit test listener, writing results to InfluxDB " + influxDbSender.getConnectionUrl());
            log.info("For application of version " + testDataProcessor.getReleaseVersion());
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan is about to be started.
     *
     * @param testIdentifier - the identifier of the started test or container
     */
    @Override
    public void executionStarted(final TestIdentifier testIdentifier) {
        if (isListenerEnabled) {
            if (testIdentifier.isTest()) {
                testDataProcessor.startTimeTest();

                // When a new test class begins
            } else if (testIdentifier.getParentId().isPresent()) {
                final String className = testIdentifier.getDisplayName();
                testDataProcessor.setClassName(className);
                testDataProcessor.setTestType(className);

                if (testDataProcessor.getApplication().equals(UNKNOWN)) {
                    testDataProcessor.setApplication(testIdentifier.getLegacyReportingName());
                }
            }
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has been skipped.
     *
     * @param testIdentifier - the identifier of the started test or container
     * @param reason         - a human-readable message describing why the execution has been skipped
     */
    @Override
    public void executionSkipped(final TestIdentifier testIdentifier, final String reason) {
        if (isListenerEnabled) {
            testDataProcessor.setTestCaseSkipped(reason);
            testDataProcessor.setTestName(testIdentifier.getDisplayName());
            testDataProcessor.setTestLabels(testIdentifier);

            // Set a data record with the results of 1 test
            influxDbSender.setDataRecord(testDataProcessor.getJunitTestResult());
            // Reset data of the test result that changes per test
            testDataProcessor.resetTestResult();
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of the outcome.
     *
     * @param testIdentifier      - the identifier of the finished test or container
     * @param testExecutionResult - the (unaggregated) result of the execution for the supplied TestIdentifier
     */
    @Override
    public void executionFinished(final TestIdentifier testIdentifier,
            final TestExecutionResult testExecutionResult) {
        if (isListenerEnabled && testIdentifier.isTest()) {
            testDataProcessor.setDurationTest();
            testDataProcessor.setTestName(testIdentifier.getDisplayName());
            testDataProcessor.setResultAndErrorMessage(testExecutionResult);
            testDataProcessor.setTestLabels(testIdentifier);

            // Set a data record with the results of 1 test
            influxDbSender.setDataRecord(testDataProcessor.getJunitTestResult());
            // Reset data of the test result that changes per test
            testDataProcessor.resetTestResult();
        }
    }

    /**
     * Called when the execution of the TestPlan has finished,
     * after all tests have been executed.
     */
    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        if (isListenerEnabled) {
            testDataProcessor.setDurationTestRun();
            final Long testCases = testPlan.countTestIdentifiers(TestIdentifier::isTest);
            testDataProcessor.setTestCases(testCases);
            testDataProcessor.setTestRunResult();

            if (testCases != 0) {
                testDataProcessor.setTestCasesFailed();
                testDataProcessor.setTestCasesSkipped();
            }

            // Set a data record with the results of all tests
            influxDbSender.setDataRecord(testDataProcessor.getJunitTestRunResult());
            influxDbSender.sendDataRecords();
            log.info("The test results are successfully written to the InfluxDB");
        }
    }
}
