package com.github.vanbadasselt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;

@SuppressFBWarnings("URF_UNREAD_FIELD")
@SuppressWarnings("PMD.UnusedPrivateField")
@Measurement(name = "junitTest")
public class JunitTestResult {

    @TimeColumn
    @Column(name = "time")
    private Instant time;

    @Column(name = "application")
    private String application;

    @Column(name = "featureLabel", tag = true)
    private String featureLabel;

    @Column(name = "className")
    private String className;

    @Column(name = "durationMs")
    private long durationMs;

    @Column(name = "errorMessage")
    private String errorMessage;

    @Column(name = "releaseVersion", tag = true)
    private String releaseVersion;

    @Column(name = "result", tag = true)
    private String result;

    @Column(name = "run", tag = true)
    private String run;

    @Column(name = "riskLabel", tag = true)
    private String riskLabel;

    @Column(name = "skippedMessage")
    private String skippedMessage;

    @Column(name = "testName")
    private String testName;

    @Column(name = "testType", tag = true)
    private String testType;

    private static final String UNKNOWN = "UNK";

    public JunitTestResult(){
        this.application = UNKNOWN;
        this.className = UNKNOWN;
        this.testName = UNKNOWN;
        this.durationMs = 0;
        this.skippedMessage = UNKNOWN;
        this.errorMessage = UNKNOWN;
        this.featureLabel = UNKNOWN;
        this.releaseVersion = UNKNOWN;
        this.riskLabel = UNKNOWN;
        this.testType = UNKNOWN;
        this.run = UNKNOWN;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setFeatureLabel(String featureLabel) {
        this.featureLabel = featureLabel;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
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
}
