package com.nycjv321.pagerdutytools.updater;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.nycjv321.pagerdutytools.utils.Collections;

import java.util.Objects;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class IncidentUpdater implements Updater {
    private final DB db;
    private Collections collections;

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
    }

    private void updateLastStatusChangeBy(BasicDBObject incident) {
        update(
                incident,
                "users",
                "last_status_change_by",
                "id",
                "last_status_change_by_id"
        );
    }

    private void updateResolvedByUser(BasicDBObject incident) {
        update(
                incident,
                "users",
                "resolved_by_user",
                "id",
                "resolved_by_user_id"
        );
    }

    private void updateIncidentAssignedToUser(BasicDBObject incident) {
        update(
                incident,
                "users",
                "assigned_to_user",
                "id",
                "assigned_to_user_id"
        );

    }


    void updateIncidentService(BasicDBObject incident) {
        update(
                incident,
                "services",
                "service",
                "id",
                "service_id"
        );
    }


    void update(BasicDBObject parent, String collectionName, String objectToUpdate, String objectKey, String objectIdentifier) {
        BasicDBObject child = (BasicDBObject) parent.get(objectToUpdate);

        if (Objects.isNull(child)) {
            return;
        }

        DBObject queriedChild = db.getCollection(collectionName).findOne(
                new BasicDBObject(objectKey, new BasicDBObject("$eq", child.getString(objectKey)))
        );

        if (Objects.isNull(queriedChild)) {
            collections.add(child, collectionName);
            queriedChild = db.getCollection(collectionName).findOne(
                    new BasicDBObject(objectKey, new BasicDBObject("$eq", child.getString(objectKey)))
            );
        }
        Object objectId = queriedChild.get("_id");
        parent.remove(objectToUpdate);
        parent.put(objectIdentifier, objectId);

    }

}
