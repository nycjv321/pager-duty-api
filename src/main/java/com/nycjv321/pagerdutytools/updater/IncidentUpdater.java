package com.nycjv321.pagerdutytools.updater;

import com.mongodb.*;
import com.nycjv321.pagerdutytools.rest.DBOjectDownloader;
import com.nycjv321.pagerdutytools.utils.Collections;

import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class IncidentUpdater implements Updater {
    private final DB db;
    private Collections collections;
    private DBOjectDownloader downloader = new DBOjectDownloader();

    public IncidentUpdater(DB db) {
        this.db = db;
        collections = new Collections(db);
    }

    @Override
    public void update(BasicDBObject incident) {
        updateIncidentService(incident);
        updateIncidentAssignedToUser(incident);
        updateResolvedByUser(incident);
        updateLastStatusChangeBy(incident);
        updateNotes(incident);
    }



    private void updateNotes(BasicDBObject incident) {
        BasicDBList noteInstances = downloader.getNotes(incident.getInt("incident_number"));
        for (int x = 0; x < noteInstances.size(); ++x) {
            BasicDBObject noteInstance = (BasicDBObject) noteInstances.get(x);
            NoteUpdater noteUpdater = new NoteUpdater(db, incident);
            noteUpdater.update(noteInstance);
            collections.addTo(noteInstance, "notes"); // make this bulk op?
        }
    }


    private void updateLastStatusChangeBy(BasicDBObject incident) {
        BasicDBObject user = (BasicDBObject) incident.get("last_status_change_by");
        if (isNull(user)) {
            return;
        }
        Object userId = db.getCollection("users").findOne(
                new BasicDBObject("id", new BasicDBObject("$eq", user.getString("id")))
        ).get("_id");
        incident.remove("last_status_change_by");
        incident.put("last_status_change_by_id", userId);
    }

    private void updateResolvedByUser(BasicDBObject incident) {
        BasicDBObject user = (BasicDBObject) incident.get("resolved_by_user");
        if (isNull(user)) {
            return;
        }

        Object userId;
        DBCollection users = db.getCollection("users");
        try {
            userId = users.findOne(
                    new BasicDBObject("id", new BasicDBObject("$eq", user.getString("id")))
            ).get("_id");
        } catch (NullPointerException e) {
            user.put("active", false);
            users.insert(user);
            userId = users.findOne(
                    new BasicDBObject("id", new BasicDBObject("$eq", user.getString("id")))
            ).get("_id");
        }
        incident.remove("resolved_by_user");
        incident.put("resolved_by_user_id", userId);
    }

    private void updateIncidentAssignedToUser(BasicDBObject incident) {
        BasicDBObject assignedToUser = (BasicDBObject) incident.get("assigned_to_user");
        if (isNull(assignedToUser)) {
            return;
        }
        Object userId = db.getCollection("users").findOne(
                new BasicDBObject("id", new BasicDBObject("$eq", assignedToUser.getString("id")))
        ).get("_id");
        incident.remove("assigned_to_user");
        incident.put("assigned_to_user_id", userId);

    }

    private void updateIncidentService(BasicDBObject incident) {
        BasicDBObject service = (BasicDBObject) incident.get("service");
        final DBObject serviceObject = db.getCollection("services").findOne(
                new BasicDBObject("id", new BasicDBObject("$eq", service.getString("id")))
        );

        if (Objects.isNull(serviceObject)) {
            collections.addTo(service, "services");
        }
        Object serviceId = service.get("_id");
        incident.remove("service");
        incident.put("service_id", serviceId);

    }

}
