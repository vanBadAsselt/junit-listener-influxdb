package com.github.vanbadasselt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;

@SuppressFBWarnings("URF_UNREAD_FIELD")
@Measurement(name = "junitRun")
public class JunitTestRunResult {

    @TimeColumn
    @Column(name = "time")
    private Instant time;

    @Column(name = "application", tag = true)
    private String application;

    @Column(name = "durationMs")
    private long durationMs;

    @Column(name = "releaseVersion", tag = true)
    private String releaseVersion;

    @Column(name = "result", tag = true)
    private String result;

    @Column(name = "run")
    private String run;

    @Column(name = "testCases")
    private long testCases;

    @Column(name = "testCasesFailed")
    private long testCasesFailed;

    @Column(name = "testCasesSkipped")
    private long testCasesSkipped;

    @Column(name = "testType", tag = true)
    private String testType;

    private static final String UNKNOWN = "UNK";

    public JunitTestRunResult(){
        this.application = UNKNOWN;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
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

    public void setTestCases(long testCases) {
        this.testCases = testCases;
    }

    public void setTestCasesFailed(long testCasesFailed) {
        this.testCasesFailed = testCasesFailed;
    }

    public void setTestCasesSkipped(long testCasesSkipped) {
        this.testCasesSkipped = testCasesSkipped;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
