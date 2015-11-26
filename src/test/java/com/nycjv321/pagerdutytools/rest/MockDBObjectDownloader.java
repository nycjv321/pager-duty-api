package com.nycjv321.pagerdutytools.rest;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.util.JSON;
import com.nycjv321.pagerdutytools.documents.models.Incident;
import com.nycjv321.pagerdutytools.updater.IncidentUpdater;
import com.nycjv321.pagerdutytools.updater.LogUpdater;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by fedora on 11/26/15.
 */
public class MockDBObjectDownloader extends DBObjectDownloader {


    @Override
    public BasicDBObject getIncident(int i) {
        return (BasicDBObject) loadResource(String.format("/api_data/incidents/%d.json", i));
    }

    private Object loadResource(String path) {
        try (InputStream resource = getClass().getResourceAsStream(path)) {
            if (Objects.isNull(resource)) {
                throw new NullPointerException(path + " not found");
            }
            return JSON.parse(IOUtils.toString(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BasicDBObject[] getIncidents(DB db) {
        BasicDBList incidents = (BasicDBList) ((BasicDBObject) loadResource("/api_data/incidents.json")).get("incidents");
        IncidentUpdater incidentUpdater = new IncidentUpdater(db);
        Iterator<Object> iterator = incidents.iterator();

        while (iterator.hasNext()) {
            incidentUpdater.update((BasicDBObject) iterator.next());
        }

        return incidents.toArray(new BasicDBObject[incidents.size()]);
    }

    @Override
    public BasicDBObject[] getNotes(String id) {
        BasicDBList noteList = (BasicDBList) ((BasicDBObject) loadResource(String.format("/api_data/incidents/%s/notes.json", id))).get("notes");
        return noteList.toArray(new BasicDBObject[noteList.size()]);
    }

    @Override
    public BasicDBObject[] getNotes() {
        List<Incident> all = Incident.all();
        List<BasicDBObject> noteList = new ArrayList<>();
        for (Incident incident : all) {
            BasicDBObject[] notes;
            try {
                notes = getNotes(incident.getIncidentId());
            } catch (NullPointerException e) {
                continue;
            }
            if (notes.length == 0) {
                continue;
            }
            noteList.addAll(Arrays.asList(notes));
        }
        return noteList.toArray(new BasicDBObject[noteList.size()]);
    }

    @Override
    public BasicDBObject[] getServices() {
        BasicDBList services = (BasicDBList) ((BasicDBObject) loadResource("/api_data/services.json")).get("services");
        return services.toArray(new BasicDBObject[services.size()]);
    }

    @Override
    public BasicDBObject[] getUsers() {
        BasicDBList services = (BasicDBList) ((BasicDBObject) loadResource("/api_data/users.json")).get("users");
        return services.toArray(new BasicDBObject[services.size()]);
    }

    @Override
    public BasicDBObject[] getLogEntries(DB db) {
        List<Incident> all = Incident.all();
        List<BasicDBObject> logEntriesList = new ArrayList<>();
        for (Incident incident : all) {
            BasicDBList logEntries;
            try {
                logEntries = (BasicDBList) ((BasicDBObject) loadResource(String.format("/api_data/incidents/%s/log_entries.json", incident.getIncidentId()))).get("log_entries");
            } catch (NullPointerException e) {
                continue;
            }

            LogUpdater logUpdater = new LogUpdater(db, incident);

            Iterator<Object> iterator = logEntries.iterator();
            while (iterator.hasNext()) {
                BasicDBObject next = (BasicDBObject) iterator.next();
                logUpdater.update(next);
            }

            BasicDBObject[] basicDBObjects = logEntries.toArray(new BasicDBObject[logEntries.size()]);
            logEntriesList.addAll(Arrays.asList(basicDBObjects));
        }
        return logEntriesList.toArray(new BasicDBObject[logEntriesList.size()]);

    }


}
