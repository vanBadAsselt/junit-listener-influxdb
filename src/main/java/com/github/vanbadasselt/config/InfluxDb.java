package com.github.vanbadasselt.config;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;

import java.util.logging.Logger;

/**
 * Addresses the right InfluxDb instance via environment variables in order to interact with this instance.
 *
 * @author vanbadasselt
 */
public final class InfluxDb {

    /**
     * Sets the right InfluxDB host via the environment variable 'TEST_RESULTS_INFLUX_HOST'.
     */
    private static final String ENV_INFLUX_HOST = "TEST_RESULTS_INFLUX_HOST";

    /**
     * Sets the right InfluxDB port via the environment variable 'TEST_RESULTS_INFLUX_PORT'.
     */
    private static final String ENV_INFLUX_PORT = "TEST_RESULTS_INFLUX_PORT";

    /**
     * Sets the right InfluxDB user via the environment variable 'TEST_RESULTS_INFLUX_USER'.
     */
    private static final String ENV_INFLUX_USER = "TEST_RESULTS_INFLUX_USER";

    /**
     * Sets the right InfluxDB password via the environment variable 'TEST_RESULTS_INFLUX_PASSWORD'.
     */
    private static final String ENV_INFLUX_PASSWORD = "TEST_RESULTS_INFLUX_PASSWORD";

    /**
     * Sets the right InfluxDB database name via the environment variable 'TEST_RESULTS_INFLUX_DB'.
     */
    private static final String ENV_INFLUX_DB_NAME = "TEST_RESULTS_INFLUX_DB";

    /**
     * Logs in case of warning.
     */
    private final Logger log = Logger.getLogger(InfluxDb.class.getName());

    /**
     * Represents the connection with the addressed InfluxDB instance.
     */
    private InfluxDB influxDBConnection;

    /**
     * Aggregation of Influx host and port.
     */
    private final String influxUrl;

    /**
     * Represents the Influx user.
     */
    private final String influxUser;

    /**
     * Represents the Influx password.
     */
    private final String influxPassword;

    /**
     * Represents the Influx database name.
     */
    private final String influxDatabaseName;

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
