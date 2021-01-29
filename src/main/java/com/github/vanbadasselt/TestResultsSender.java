package com.github.vanbadasselt;

import com.github.vanbadasselt.config.InfluxDb;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class TestResultsSender {

    private final InfluxDb influxClient;
    private final BatchPoints batchPoints;

    public TestResultsSender() {
        this.influxClient = new InfluxDb();
        this.batchPoints = influxClient.getBatchPoints();
    }

    public void setPoint(final Point point) {
        this.batchPoints.point(point);
    }

    public void post() {
        influxClient.getInfluxDBConnection().write(this.batchPoints);
    }

    public String getConnectionUrl() {
        return influxClient.getInfluxUrl();
    }

    public void closeConnection() {
        influxClient.getInfluxDBConnection().close();
    }
}
