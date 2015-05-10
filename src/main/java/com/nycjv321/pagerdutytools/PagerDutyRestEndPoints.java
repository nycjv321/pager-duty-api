package com.nycjv321.pagerdutytools;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.nycjv321.utilities.HttpUtilities.buildParameters;


/**
 * Created by jvelasquez on 4/15/15.
 */
public class PagerDutyRestEndPoints {

    private static final String domain = Configuration.getDomain();
    private static final String baseURL = String.format("https://%s.pagerduty.com/api/v1/", domain);

    public static String incidents(Map<String, String> parameters) {
        String url = baseURL + "incidents/";
        return url + buildParameters(parameters);
    }

    public static String incidentCount(Map<String, String> parameters) {
        String url = baseURL + "incidents/count/";
        return url + buildParameters(parameters);
    }

    public static String notes(int id) {
        return incidents(ImmutableMap.of()) + id + "/notes/";
    }

    public static String users(Map<String, String> parameters) {
        return baseURL + "users/" + buildParameters(parameters);
    }

    public static String services() {
        return baseURL + "services/";
    }

    public static String incident(int id) {
        return baseURL + "incidents/" + id + "/";
    }

    public static String logEntries(String i) {
        return baseURL + "incidents/" + i + "/log_entries?include[]=channel";
    }
}
