package com.nycjv321.pagerdutytools;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by jvelasquez on 5/7/15.
 */
public class Configuration {

    private static final Properties properties;
    static {
        properties = new Properties();
        try {
            properties.load(Configuration.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAuthorizationToken() {
        return properties.getProperty("pagerduty.rest.authorization.token");
    }

    public static String getHost() {
        return properties.getProperty("mongodb.database.host");
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("mongodb.database.port"));
    }

    public static String getDatabaseName() {
        return properties.getProperty("mongodb.database.name");
    }

    public static String getDomain() {
        return properties.getProperty("company.domain");
    }

}
