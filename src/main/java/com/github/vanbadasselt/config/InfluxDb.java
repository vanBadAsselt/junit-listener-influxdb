package com.github.vanbadasselt.config;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import java.util.logging.Logger;

public final class InfluxDb {

    private static final String ENV_INFLUX_HOST = "TEST_RESULTS_INFLUX_HOST";
    private static final String ENV_INFLUX_PORT = "TEST_RESULTS_INFLUX_PORT";
    private static final String ENV_INFLUX_USER = "TEST_RESULTS_INFLUX_USER";
    private static final String ENV_INFLUX_PASSWORD = "TEST_RESULTS_INFLUX_PASSWORD";
    private static final String ENV_INFLUX_DB_NAME = "TEST_RESULTS_INFLUX_DB";
    private final Logger log = Logger.getLogger(InfluxDb.class.getName());
    private InfluxDB influxDBConnection;
    private String influxUrl;
    private String influxUser;
    private String influxPassword;
    private String influxDatabaseName;



    /**
     * Initializes connection with an influx database.
     */
    public InfluxDb() {
        final String influxHost = getEnvironmentVariable(ENV_INFLUX_HOST, "localhost");
        final String influxPort = getEnvironmentVariable(ENV_INFLUX_PORT, "8086");
        influxUrl = "http://" + influxHost + ":" + influxPort;
        influxUser = getEnvironmentVariable(ENV_INFLUX_USER, "root");
        influxPassword = getEnvironmentVariable(ENV_INFLUX_PASSWORD, "root");
        influxDatabaseName = getEnvironmentVariable(ENV_INFLUX_DB_NAME, "testresults");
    }

    /**
     * Connects with the configured influx database.
     */
    public void connect() {
        influxDBConnection = InfluxDBFactory.connect(influxUrl, influxUser, influxPassword);
        final Pong response = this.influxDBConnection.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            log.warning("Error pinging server.");
            return;
        }
        influxDBConnection.setLogLevel(InfluxDB.LogLevel.BASIC);
        influxDBConnection.setDatabase(influxDatabaseName);
        influxDBConnection.createRetentionPolicy(
                "defaultPolicy", influxDatabaseName, "60d", 1, true);
        influxDBConnection.enableBatch(BatchOptions.DEFAULTS);
    }

    /**
     * Returns the influxDB connection setup based on host url, user and password.
     *
     * @return influxDB connection
     */
    public InfluxDB getConnection() {
        return influxDBConnection;
    }

    /**
     * Returns the host url of the influxDB connection.
     *
     * @return influx host url
     */
    public String getHostUrl() {
        return influxUrl;
    }

    /**
     * Returns the collection of Points you pushed to the Batchpoint.
     *
     * @return batch points
     */
    public BatchPoints getBatchPoints() {
        return BatchPoints
                .database(influxDatabaseName)
                .retentionPolicy("defaultPolicy")
                .build();
    }

    private String getEnvironmentVariable(final String envKey, final String alternative) {
        final String valueEnvVariable = System.getenv(envKey);
        return valueEnvVariable != null ? valueEnvVariable : alternative;
    }
}
