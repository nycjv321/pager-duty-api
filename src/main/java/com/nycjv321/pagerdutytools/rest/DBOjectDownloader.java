package com.nycjv321.pagerdutytools.rest;

import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.util.JSON;
import com.nycjv321.http.SimpleHttpClient;
import com.nycjv321.http.SimpleHttpClientBuilder;
import com.nycjv321.pagerdutytools.Configuration;
import com.nycjv321.pagerdutytools.models.Incident;
import com.nycjv321.pagerdutytools.updater.IncidentUpdater;
import com.nycjv321.pagerdutytools.updater.LogUpdater;
import com.nycjv321.utilities.CalendarUtilities;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class DBOjectDownloader {

    public static final Header authentication = new BasicHeader("Authorization", "Token token=" + Configuration.getAuthorizationToken());
    public static final Header contentType = new BasicHeader("Content-type", "application/json");
    private CalendarUtilities calendarUtilities = new CalendarUtilities("yyyy-MM-dd");
    private static final SimpleHttpClient httpClient = SimpleHttpClientBuilder.create().build();

    public BasicDBObject getIncident(int i) {
        String json = httpClient.get(EndPoints.incident(i), contentType, authentication);
        BasicDBObject data = (BasicDBObject) JSON.parse(json);
        // insert user translation
        assert data != null;
        return data;
    }

    public BasicDBList getLogEntries(String incidentId) {
        String json = httpClient.get(
                EndPoints.logEntries(incidentId), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);

        // insert user translation
        assert data != null;
        return (BasicDBList) data.get("log_entries");
    }


    public BasicDBList getNotes(int id) {
        String json = httpClient.get(EndPoints.notes(id), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);
        assert data != null;

        return (BasicDBList) data.get("notes");
    }


    public BasicDBList getNotes() {
        int incidents = Incident.all().size();
        BasicDBList notes = new BasicDBList();
        // this takes a while
        for (int i = 1; i <= incidents; ++i) {
            BasicDBList noteInstances = getNotes(i);
            notes.addAll(noteInstances);
        }
        return notes;
    }


    public BasicDBList getServices() {
        BasicDBList services = new BasicDBList();
        String json = httpClient.get(EndPoints.services(), contentType, authentication);
        BasicDBObject data = (BasicDBObject) JSON.parse(json);
        // insert user translation
        assert data != null;
        services.addAll((BasicDBList) data.get("services"));
        return services;
    }

    public BasicDBList getIncidents(DB db) {
        BasicDBList incidents = new BasicDBList();
        int expectedIncidents = getIncidentCount();
        int offset = 0;
        IncidentUpdater incidentUpdater = new IncidentUpdater(db);
        while (incidents.size() < expectedIncidents) {
            String json = httpClient.get(
                    EndPoints.incidents(
                            ImmutableMap.of("date_range", "all", "offset", String.valueOf(offset))), contentType, authentication);

            BasicDBObject data = (BasicDBObject) JSON.parse(json);

            assert data != null;
            BasicDBList incidentList = (BasicDBList) data.get("incidents");

            Iterator<Object> iterator = incidentList.iterator();
            while (iterator.hasNext()) {
                BasicDBObject incident = (BasicDBObject) iterator.next();
                incidentUpdater.update(incident);
            }

            incidents.addAll(incidentList);
            offset += 100;
        }
        return incidents;
    }

    public BasicDBList getUsers() {
        BasicDBList users = new BasicDBList();
        String json = httpClient.get(EndPoints.users(ImmutableMap.of("limit", "100")), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);

        // insert user translation
        assert data != null;
        BasicDBList userList = (BasicDBList) data.get("users");
        for (Object user : userList) {
            ((BasicDBObject) user).put("active", true);
        }
        users.addAll(userList);

        return users;
    }

    /**
     * Return incidents since provided calendar up to 30 days after the provided calendar
     *
     * @param calendar
     * @return
     */
    public int getIncidentCountSince(Calendar calendar) {
        ImmutableMap<String, String> params;
        if (isNull(calendar)) {
            params = ImmutableMap.of("date_range", "all");
        } else {
            Calendar to = (Calendar) calendar.clone();
            to.add(Calendar.DAY_OF_YEAR, 30);
            params = ImmutableMap.of("since", calendarUtilities.getFormattedCalendar(calendar), "until", calendarUtilities.getFormattedCalendar(to));
        }
        String json = httpClient.get(EndPoints.incidentCount(params), contentType, authentication);
        try {
            return new JSONObject(json).getInt("total");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public BasicDBList getLogEntries(DB db) {
        BasicDBList logEntries = new BasicDBList();
        List<Incident> incidents = Incident.all();
        // this takes a while
        for (int i = 1; i <= incidents.size(); ++i) {

            Incident incident = incidents.get(i - 1);
            String incidentId = incident.getId();
            BasicDBList logInstances = getLogEntries(incidentId);
            LogUpdater logUpdater = new LogUpdater(db, incident);
            for (int j = 0; j < logInstances.size(); ++j) {
                BasicDBObject logInstance = (BasicDBObject) logInstances.get(j);
                if (isNull(logInstance)) {
                    continue;
                }
                logUpdater.update(logInstance);
                logEntries.add(logInstance);
            }
            try {
                Thread.sleep(RandomUtils.nextInt(0, 2500));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return logEntries;
    }

    public int getIncidentCount() {
        return getIncidentCountSince(null);
    }
}
