package com.nycjv321.pagerdutytools;

import com.google.common.base.Strings;

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
        try (InputStream resourceAsStream = Configuration.class.getResourceAsStream(getConfigPath())) {
            if (Objects.isNull(resourceAsStream)) {
                throw new NullPointerException("/application.properties does not exist!");
            }
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getEnvironment() {
        return System.getProperty("pagerduty.env", "development");
    }

    public static String getAuthorizationToken() {
        return getProperty("pagerduty.rest.authorization.token", false);
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
        return getProperty("company.domain", false);
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

    public static String getIndexerLocation() {
        return getProperty("solr.location");
    }

    public static String getIndexCollection() {
        return getProperty("solr.collection");

    }

    private static String getConfigPath() {
        String environment = getEnvironment();
        if (Strings.isNullOrEmpty(environment)) {
            return "/staging/application.properties";
        } else {
            return String.format("/%s/application.properties", environment);
        }
    }



    private static final class NotDefinedException extends RuntimeException {
        public NotDefinedException(String configuration) {
            super(String.format("%s not defined in %s", configuration, getConfigPath()));
        }
    }

}
