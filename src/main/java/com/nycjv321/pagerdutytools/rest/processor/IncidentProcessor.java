package com.nycjv321.pagerdutytools.rest.processor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.nycjv321.pagerdutytools.utils.Collections;
import com.nycjv321.pagerdutytools.rest.DBOjectDownloader;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import com.nycjv321.pagerdutytools.exceptions.UnResolvedIncidentsException;
import com.nycjv321.pagerdutytools.models.Incident;
import com.nycjv321.pagerdutytools.updater.IncidentUpdater;
import com.nycjv321.pagerdutytools.updater.LogUpdater;
import org.apache.commons.lang3.RandomUtils;

import static java.util.Objects.isNull;

/**
 * Created by fedora on 11/21/15.
 */
public class IncidentProcessor {
    private final DB db;
    private final IncidentUpdater incidentUpdater;
    private DBOjectDownloader downloader = new DBOjectDownloader();

    private Collections collections;

    public IncidentProcessor() {
        db = MongoConnector.getMorphium().getDatabase();
        incidentUpdater = new IncidentUpdater(db);
        collections = new Collections(db);
    }

    public synchronized boolean processNewIncidents() throws UnResolvedIncidentsException {
        int restCount = downloader.getIncidentCount();
        int dbCount = Incident.getCount();
        if (restCount > dbCount) {
            for (int i = dbCount + 1; i <= restCount; ++i) {
                processNewIncident(i);
                try {
                    Thread.sleep(RandomUtils.nextInt(0, 2500));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        } else if (dbCount > restCount) {
            throw new IllegalStateException("More entries in db then rest interface! >< please rerun init()");
        }
        return false;
    }

    private void processNewIncident(int i) throws UnResolvedIncidentsException {
        final BasicDBObject incidentObject = downloader.getIncident(i);
        incidentUpdater.update(incidentObject);
        if (!incidentObject.getString("status").equals("resolved")) {
            throw new UnResolvedIncidentsException(
                    String.format(
                            "Incident %d is still %s. Lets resolve it first",
                            incidentObject.getInt("incident_number"),
                            incidentObject.getString("status"))
            );
        }
        collections.add(incidentObject, "incidents");
        updateLogEntries(Incident.find(i));
    }

    private void updateLogEntries(Incident incident) {
        BasicDBList logEntries = downloader.getLogEntries(incident.getId());
        for (int j = 0; j < logEntries.size(); ++j) {
            BasicDBObject logInstance = (BasicDBObject) logEntries.get(j);
            if (isNull(logInstance)) {
                continue;
            }
            LogUpdater logUpdater = new LogUpdater(db, incident);
            logUpdater.update(logInstance);
        }
        collections.addAll(logEntries, "log_entries");
    }
}
