package com.nycjv321.pagerdutytools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by jvelasquez on 5/7/15.
 */
public class Configuration {

    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream resourceAsStream = Configuration.class.getResourceAsStream("/application.properties")) {
            if (Objects.isNull(resourceAsStream)) {
                throw new NullPointerException("/application.properties does not exist!");
            }
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getAuthorizationToken() {
        return getProperty("pagerduty.rest.authorization.token");
    }

    public static String getHost() {
        return getProperty("mongodb.database.host");
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("mongodb.database.port"));
    }

    public static String getDatabaseName() {
        return getProperty("mongodb.database.name");
    }

    public static String getDomain() {
        return getProperty("company.domain");
    }

    private static String getProperty(String property) {
        return getProperty(property, true);
    }

    private static String getProperty(String property, boolean required) {
        String value = properties.getProperty(property);
        if (required && Objects.isNull(value)) {
            throw new NotDefinedException(property);
        } else {
            return value;
        }
    }

    private static final class NotDefinedException extends RuntimeException {
        public NotDefinedException(String configuration) {
            super(String.format("%s not defined in /application.properties", configuration));
        }
    }

}
