package com.github.vanbadasselt.influxModels;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;

@SuppressFBWarnings("URF_UNREAD_FIELD")
@SuppressWarnings("PMD.UnusedPrivateField")

@Measurement(name = "testResults")
public class TestResult {

    @TimeColumn
    @Column(name = "time")
    private Instant time;

    @Column(name = "application", tag = true)
    private String application;

    @Column(name = "applicationType", tag = true)
    private String applicationType;

    @Column(name = "durationMs")
    private long durationMs;

    @Column(name = "errorMessage")
    private String errorMessage;

    @Column(name = "feature", tag = true)
    private String feature;

    @Column(name = "featureName")
    private String featureName;

    @Column(name = "releaseVersion", tag = true)
    private String releaseVersion;

    @Column(name = "result", tag = true)
    private String result;

    @Column(name = "risk", tag = true)
    private String risk;

    @Column(name = "run", tag = true)
    private String run;

    @Column(name = "skippedMessage")
    private String skippedMessage;

    @Column(name = "testName")
    private String testName;

    @Column(name = "testType", tag = true)
    private String testType;

    @Column(name = "warningMessage")
    private String warningMessage;

    private static final String UNKNOWN = "UNK";
    private static final String APPLICATION_TYPE_BACKEND = "BE";

    public TestResult() {
        this.application = UNKNOWN;
        this.applicationType = APPLICATION_TYPE_BACKEND;
        this.durationMs = 0;
        this.errorMessage = UNKNOWN;
        this.feature = UNKNOWN;
        this.featureName = UNKNOWN;
        this.releaseVersion = UNKNOWN;
        this.result = UNKNOWN;
        this.risk = UNKNOWN;
        this.run = UNKNOWN;
        this.skippedMessage = UNKNOWN;
        this.testName = UNKNOWN;
        this.testType = UNKNOWN;
        this.warningMessage = UNKNOWN;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public void setSkippedMessage(String skippedMessage) {
        this.skippedMessage = skippedMessage;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
