package com.github.vanbadasselt;
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
    private String featureLabel = UNKNOWN;
    private String riskLabel = UNKNOWN;
    private String releaseVersion = UNKNOWN;

    private final JunitTestRunResult junitTestRunResult;
    private final JunitTestResult junitTestResult;

    TestDataProcessor() {
        this.junitTestRunResult = new JunitTestRunResult();
        this.junitTestResult = new JunitTestResult();
    }

    public JunitTestRunResult getJunitTestRunResult() {
        return junitTestRunResult;
    }

    public JunitTestResult getJunitTestResult() {
        return junitTestResult;
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
            application = System.getenv(ENV_APPLICATION).toUpperCase(Locale.ROOT);
        }

        if (!testSource.isEmpty()) {
            String[] sourceParts = testSource.split("\\.");

            if (sourceParts[0].equalsIgnoreCase("com") && sourceParts[1].equalsIgnoreCase("github")) {
                application = (sourceParts[2] + "_" + sourceParts[3]).toUpperCase(Locale.ROOT);
            } else {
                application = "NO_GITHUB_APPLICATION";
            }
        } else {
            application = "UNKNOWN_APPLICATION";
        }

        this.junitTestRunResult.setApplication(application);
        this.junitTestResult.setApplication(application);
    }

    public String getApplication() {
        return this.application;
    }

    public void resetTestResult() {
        this.junitTestResult.setTestName(UNKNOWN);
        this.junitTestResult.setDurationMs(0);
        this.junitTestResult.setResult(UNKNOWN);
        this.junitTestResult.setErrorMessage(UNKNOWN);
        this.junitTestResult.setSkippedMessage(UNKNOWN);
        this.junitTestResult.setFeatureLabel(this.featureLabel);
        this.junitTestResult.setRiskLabel(this.riskLabel);
    }

    public void setClassName(final String className) {
        this.junitTestResult.setClassName(className);
    }

    public void setResultAndErrorMessage(final TestExecutionResult testExecutionResult) {
        final TestExecutionResult.Status status = testExecutionResult.getStatus();
        this.junitTestResult.setResult(status.name());

        String errorMessageStr = "NA";
        if (!TestExecutionResult.Status.SUCCESSFUL.equals(status)) {
            testCasesFailed.incrementAndGet();
            Throwable errorMessage = testExecutionResult.getThrowable().orElse(new Throwable("unknown error"));
            errorMessageStr = errorMessage.getMessage();
        }

        this.junitTestResult.setErrorMessage(errorMessageStr);
    }

    public void setTestCaseSkipped(final String reason) {
        testCasesSkipped.incrementAndGet();
        this.junitTestResult.setResult(SKIPPED);
        this.junitTestResult.setDurationMs(0);
        this.junitTestResult.setSkippedMessage(reason);
    }

    /**
     * Saves the release version of your project when available,
     * TODO: make this applicable for your test project, check out the readme to export this env var.
     */
    public void setReleaseVersion() {
        final String releaseVersionFromCi = System.getenv(ENV_RELEASE_VERSION);

        if (releaseVersionFromCi != null && !releaseVersionFromCi.isEmpty()) {
            releaseVersion = releaseVersionFromCi;
        }

        this.junitTestResult.setReleaseVersion(releaseVersion);
        this.junitTestRunResult.setReleaseVersion(releaseVersion);
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setTestCases(final Long testCases) {
        this.junitTestRunResult.setTestCases(testCases);
    }

    public void setTestCasesFailed() {
        this.junitTestRunResult.setTestCasesFailed(testCasesFailed.intValue());
    }

    public void setTestCasesSkipped() {
        this.junitTestRunResult.setTestCasesSkipped(testCasesSkipped.intValue());
    }

    public void setTestName(final String name) {
        this.junitTestResult.setTestName(splitCamelCase(name));
    }

    /**
     * Determine the test type based on the path directory of your tests
     * This example requires your tests to be situated in directory 'component' or 'integration'
     * TODO: make this applicable for your test project
     *
     * @param testSource of the project
     */
    public void setTestType(final String testSource) {
        String testType;
        if (testSource.endsWith("Test")) {
            testType = TYPE_UNIT;
        } else if (testSource.endsWith(TYPE_COMPONENT)) {
            testType = TYPE_COMPONENT;
        } else if (testSource.endsWith(TYPE_INTEGRATION)) {
            testType = TYPE_INTEGRATION;
        } else
            testType = "UNKNOWN_TEST_TYPE";

        this.junitTestResult.setTestType(testType);
        this.junitTestRunResult.setTestType(testType);
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
                    riskLabel = tag;
                    break;
                default:
                    featureLabel = tag;
                    break;
                }
            });
        }

        this.junitTestResult.setFeatureLabel(featureLabel);
        this.junitTestResult.setRiskLabel(riskLabel);
    }

    public void setTestRunResult() {
        final String result = testCasesFailed.intValue() > 0 ? NOK : OK;
        junitTestRunResult.setResult(result);
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
        startTimeTest = startTimer();
        this.junitTestResult.setTime(startTimeTest);
    }

    public void startTimeTestRun() {
        startTimeTestRun = startTimer();
        this.junitTestRunResult.setRun(getFormatter().format(startTimeTestRun));
        this.junitTestResult.setRun(getFormatter().format(startTimeTestRun));
    }

    private Instant startTimer() {
        return Instant.now();
    }

    public void setDurationTest() {
        Long duration = Duration.between(startTimeTest, Instant.now()).toMillis();
        this.junitTestResult.setDurationMs(duration);
    }

    public void setDurationTestRun() {
        Long duration = Duration.between(startTimeTestRun, Instant.now()).toMillis();
        this.junitTestRunResult.setDurationMs(duration);
    }
}
