package com.nycjv321.pagerdutytools.rest.processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.nycjv321.pagerdutytools.documents.models.Incident;
import com.nycjv321.pagerdutytools.exceptions.UnResolvedIncidentsException;
import com.nycjv321.pagerdutytools.rest.DBObjectDownloader;
import com.nycjv321.pagerdutytools.updater.IncidentUpdater;
import com.nycjv321.pagerdutytools.updater.LogUpdater;
import com.nycjv321.pagerdutytools.updater.NoteUpdater;
import com.nycjv321.pagerdutytools.utils.Collections;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import org.apache.commons.lang3.RandomUtils;

import static java.util.Objects.isNull;

/**
 * Created by fedora on 11/21/15.
 */
public class IncidentProcessor {
    private final DB db;
    private final IncidentUpdater incidentUpdater;
    private DBObjectDownloader downloader = new DBObjectDownloader();

    private Collections collections;

    public IncidentProcessor() {
        db = MongoConnector.getMorphium().getDatabase();
        incidentUpdater = new IncidentUpdater(db);
        collections = new Collections(db);
    }

    public synchronized boolean processResolvedIncidents() throws UnResolvedIncidentsException {
        int restCount = downloader.getIncidentCount();
        int dbCount = Incident.getCount();
        if (restCount > dbCount) {
            for (int i = dbCount + 1; i <= restCount; ++i) {
                processIncident(i);
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

    private void processIncident(int i) throws UnResolvedIncidentsException {
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
        updateNotes(incidentObject);
    }

    public void updateNotes(BasicDBObject incident) {
        BasicDBObject[] noteInstances = downloader.getNotes(incident.getString("incident_number"));
        for (int x = 0; x < noteInstances.length; ++x) {
            BasicDBObject noteInstance = noteInstances[x];
            NoteUpdater noteUpdater = new NoteUpdater(db, incident);
            noteUpdater.update(noteInstance);
            collections.add(noteInstance, "notes"); // make this bulk op?
        }
    }


    private void updateLogEntries(Incident incident) {
        BasicDBObject[] logEntries = downloader.getLogEntries(incident.getIncidentId());
        for (int j = 0; j < logEntries.length; ++j) {
            BasicDBObject logInstance = logEntries[j];
            if (isNull(logInstance)) {
                continue;
            }
            LogUpdater logUpdater = new LogUpdater(db, incident);
            logUpdater.update(logInstance);
        }
        collections.add(logEntries, "log_entries");
    }
}
