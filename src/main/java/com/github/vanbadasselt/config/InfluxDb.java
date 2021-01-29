package com.github.vanbadasselt.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;

import java.util.logging.Logger;

public class InfluxDb {

    public static final String TABLE_NAME_RUN = "junitRun";
    public static final String TABLE_NAME_TEST = "junitTest";
    private static final String INFLUX_URL = "http://localhost:8086";
    private static final String INFLUX_USER = "root";
    private static final String INFLUX_PASSWORD = "root";
    private static final String INFLUX_DB_NAME = "testResults";
    private final Logger log = Logger.getLogger(InfluxDb.class.getName());

    private final InfluxDB influxDBConnection;

    public InfluxDb() {
        influxDBConnection = InfluxDBFactory.connect(INFLUX_URL, INFLUX_USER, INFLUX_PASSWORD);
        final Pong response = this.influxDBConnection.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            log.warning("Error pinging server.");
            return;
        }
        influxDBConnection.setLogLevel(InfluxDB.LogLevel.BASIC);
        influxDBConnection.setDatabase(INFLUX_DB_NAME);
        influxDBConnection.createRetentionPolicy(
                "defaultPolicy", INFLUX_DB_NAME, "60d", 1, true);
    }

    public InfluxDB getInfluxDBConnection() {
        return influxDBConnection;
    }

    public String getInfluxUrl() {
        return INFLUX_URL;
    }

    public BatchPoints getBatchPoints() {
        return BatchPoints
                .database(INFLUX_DB_NAME)
                .retentionPolicy("defaultPolicy")
                .build();
    }
}
