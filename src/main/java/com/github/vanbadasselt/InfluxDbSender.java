package com.github.vanbadasselt;

import com.github.vanbadasselt.config.InfluxDb;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class InfluxDbSender {

    private InfluxDb influxClient;
    private BatchPoints batchPoints;

    public InfluxDbSender() {
        this.influxClient = new InfluxDb();
        this.batchPoints = influxClient.getBatchPoints();
    }

    public void setDataRecord(final JunitTestResult junitTestResult) {
        final Point point = Point.measurementByPOJO(junitTestResult.getClass()).addFieldsFromPOJO(junitTestResult).build();
        this.batchPoints.point(point);
    }

    public void setDataRecord(final JunitTestRunResult junitTestRunResult) {
        final Point point = Point.measurementByPOJO(junitTestRunResult.getClass()).addFieldsFromPOJO(junitTestRunResult).build();
        this.batchPoints.point(point);
    }

    public void sendDataRecords() {
        influxClient.connect();
        influxClient.getConnection().write(this.batchPoints);
        influxClient.getConnection().close();
    }

    public String getConnectionUrl() {
        return influxClient.getHostUrl();
    }
}
