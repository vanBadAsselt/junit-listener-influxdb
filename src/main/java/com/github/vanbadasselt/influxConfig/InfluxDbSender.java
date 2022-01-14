package com.github.vanbadasselt.influxConfig;

import com.github.vanbadasselt.influxModels.TestResult;
import com.github.vanbadasselt.influxModels.TestRunResult;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class InfluxDbSender {

    private InfluxDb influxDb;
    private BatchPoints batchPoints;

    public InfluxDbSender() {
        this.influxDb = new InfluxDb();
        this.batchPoints = influxDb.getBatchPoints();
    }

    public void savePoint(final TestResult testResult) {
        final Point point = Point.measurementByPOJO(testResult.getClass()).addFieldsFromPOJO(testResult).build();
        this.batchPoints.point(point);
    }

    public void savePoint(final TestRunResult testRunResult) {
        final Point point = Point.measurementByPOJO(testRunResult.getClass()).addFieldsFromPOJO(testRunResult).build();
        this.batchPoints.point(point);
    }

    public void sendPoints() {
        influxDb.connect();
        influxDb.getConnection().write(this.batchPoints);
        influxDb.getConnection().close();
    }

    public String getConnectionUrl() {
        return influxDb.getInfluxUrl();
    }
}
