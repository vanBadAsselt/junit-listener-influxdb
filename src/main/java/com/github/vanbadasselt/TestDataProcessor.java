package com.github.vanbadasselt;
import com.github.vanbadasselt.influxModels.TestResult;
import com.github.vanbadasselt.influxModels.TestRunResult;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestIdentifier;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TestDataProcessor {

    private static final String ENV_APPLICATION = "CI_PROJECT_NAME";
    private static final String ENV_RELEASE_VERSION = "CI_RELEASE_VERSION";
    private static final String OK = "SUCCESSFUL";
    private static final String NOK = "FAILED";
    private static final String SKIPPED = "SKIPPED";
    private static final String TYPE_UNIT = "UT";
    private static final String TYPE_COMPONENT = "CT";
    private static final String TYPE_INTEGRATION = "IT";

    private static final String UNKNOWN = "UNK";

    private final AtomicInteger testCasesFailed = new AtomicInteger();
    private final AtomicInteger testCasesSkipped = new AtomicInteger();

    private Instant startTimeTest;
    private Instant startTimeTestRun;

    private String application = UNKNOWN;
    private String feature = UNKNOWN;
    private String featureName = UNKNOWN;
    private String releaseVersion = UNKNOWN;
    private String risk = UNKNOWN;
    private String run = UNKNOWN;
    private String testType = UNKNOWN;

    private TestRunResult testRunResult;
    private TestResult testResult;

    TestDataProcessor() {
        this.testRunResult = new TestRunResult();
        this.testResult = new TestResult();
    }

    public TestRunResult getTestRunResult() {
        return testRunResult;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.UK)
                .withZone(ZoneId.systemDefault());
    }

    /**
     * Determine the application name based on the predefined Gitlab var 'CI_PROJECT_NAME' or a subset of the path of your project
     * TODO: make this applicable for your project
     * @param testSource of the project
     */
    public void setApplication(final String testSource) {
        if (System.getenv(ENV_APPLICATION) != null) {
            this.application = System.getenv(ENV_APPLICATION).toUpperCase(Locale.ROOT);
        }

        if (!testSource.isEmpty()) {
            String[] sourceParts = testSource.split("\\.");

            if (sourceParts[0].equalsIgnoreCase("com") && sourceParts[1].equalsIgnoreCase("github")) {
                this.application = (sourceParts[2] + "_" + sourceParts[3]).toUpperCase(Locale.ROOT);
            } else {
                this.application = "NO_GITHUB_APPLICATION";
            }
        } else {
            this.application = "UNKNOWN_APPLICATION";
        }

        this.testRunResult.setApplication(this.application);
        this.testResult.setApplication(this.application);
    }

    public String getApplication() {
        return this.application;
    }

    public void resetTestResult() {
        this.testResult = new TestResult();
        this.testResult.setApplication(this.application);
        this.testResult.setFeature(this.feature);
        this.testResult.setFeatureName(this.featureName);
        this.testResult.setReleaseVersion(this.releaseVersion);
        this.testResult.setRisk(this.risk);
        this.testResult.setRun(this.run);
        this.testResult.setTestType(this.testType);
    }

    public void setFeatureName(final String featureName) {
        this.featureName = featureName;
        this.testResult.setFeatureName(featureName);
    }

    public void setResultAndErrorMessage(final TestExecutionResult testExecutionResult) {
        final TestExecutionResult.Status status = testExecutionResult.getStatus();
        this.testResult.setResult(status.name());

        String errorMessageStr = "NA";
        if (!TestExecutionResult.Status.SUCCESSFUL.equals(status)) {
            testCasesFailed.incrementAndGet();
            Throwable errorMessage = testExecutionResult.getThrowable().orElse(new Throwable("unknown error"));
            errorMessageStr = errorMessage.getMessage();
        }

        this.testResult.setErrorMessage(errorMessageStr);
    }

    public void setTestCaseSkipped(final String reason) {
        testCasesSkipped.incrementAndGet();
        this.testResult.setResult(SKIPPED);
        this.testResult.setDurationMs(0);
        this.testResult.setSkippedMessage(reason);
    }

    /**
     * Saves the release version of your project when available,
     * TODO: make this applicable for your test project, check out the readme to export this env var.
     */
    public void setReleaseVersion() {
        final String releaseVersionFromCi = System.getenv(ENV_RELEASE_VERSION);

        if (releaseVersionFromCi != null && !releaseVersionFromCi.isEmpty()) {
            this.releaseVersion = releaseVersionFromCi;
        }

        this.testResult.setReleaseVersion(this.releaseVersion);
        this.testRunResult.setReleaseVersion(this.releaseVersion);
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setTestCases(final Long testCases) {
        this.testRunResult.setTestCasesTotal(testCases);
    }

    public void setTestCasesFailed() {
        this.testRunResult.setTestCasesFailed(testCasesFailed.intValue());
    }

    public void setTestCasesSkipped() {
        this.testRunResult.setTestCasesSkipped(testCasesSkipped.intValue());
    }

    public void setTestName(final String name) {
        this.testResult.setTestName(splitCamelCase(name));
    }

    /**
     * Determine the test type based on the path directory of your tests
     * This example requires your tests to be situated in directory 'component' or 'integration'
     * TODO: make this applicable for your test project
     *
     * @param testSource of the project
     */
    public void setTestType(final String testSource) {
        if (testSource.endsWith("Test")) {
            this.testType = TYPE_UNIT;
        } else if (testSource.endsWith(TYPE_COMPONENT)) {
            this.testType = TYPE_COMPONENT;
        } else if (testSource.endsWith(TYPE_INTEGRATION)) {
            this.testType = TYPE_INTEGRATION;
        } else
            this.testType = "UNKNOWN_TEST_TYPE";

        this.testResult.setTestType(this.testType);
        this.testRunResult.setTestType(this.testType);
    }

    /**
     * Saves the feature/risk metadata you add to your JUnit test or test class.
     * TODO: make this metadata applicable for your project, check out the readme for an example
     *
     * @param testIdentifier which contains the test tag
     */
    public void setTestLabels(final TestIdentifier testIdentifier) {
        final Set<TestTag> labels = testIdentifier.getTags();

        if (!labels.isEmpty()) {
            labels.forEach(t -> {
                final String tag = t.getName();
                switch (tag) {
                case "SMOKE":
                case "HIGH":
                case "MEDIUM":
                case "LOW":
                    this.risk = tag;
                    break;
                default:
                    this.feature = tag;
                    break;
                }
            });
        }

        this.testResult.setFeature(this.feature);
        this.testResult.setRisk(this.risk);
    }

    public void setTestRunResult() {
        final String result = testCasesFailed.intValue() > 0 ? NOK : OK;
        testRunResult.setResult(result);
    }

    /**
     * Split name of the test method into a readable sentence
     *
     * @param replaceString method to split
     * @return formatted test method name
     */
    public static String splitCamelCase(final String replaceString) {
        return replaceString.replaceAll("([A-Z][a-z]+)", " $1") // Words beginning with UC
                .replaceAll("([A-Z][A-Z]+)", " $1") // "Words" of only UC
                .replaceAll("([^A-Za-z ]+)", " $1") // "Words" of non-letters
                .trim();
    }

    public void startTimeTest() {
        this.startTimeTest = startTimer();
        this.testResult.setTime(this.startTimeTest);
    }

    public void startTimeTestRun() {
        this.startTimeTestRun = startTimer();
        this.run = getFormatter().format(this.startTimeTestRun);
        this.testRunResult.setRun(this.run);
        this.testResult.setRun(this.run);
    }

    private Instant startTimer() {
        return Instant.now();
    }

    public void setDurationTest() {
        final long duration = Duration.between
                (startTimeTest, Instant.now()).toMillis();
        this.testResult.setDurationMs(duration);
    }

    public void setDurationTestRun() {
        final long duration = Duration.between(this.startTimeTestRun, Instant.now()).toMillis();
        this.testRunResult.setDurationMs(duration);
    }
}
