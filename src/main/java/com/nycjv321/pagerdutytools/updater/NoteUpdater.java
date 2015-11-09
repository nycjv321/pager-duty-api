package com.nycjv321.pagerdutytools.updater;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.nycjv321.pagerdutytools.utils.Collections;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class NoteUpdater implements Updater {

    private final Collections collections;
    private final int incidentId;

    public NoteUpdater(DB db, BasicDBObject incident) {
        this.collections = new Collections(db);
        this.incidentId = incident.getInt("incident_number");
    }

    @Override
    public void update(BasicDBObject note) {
        DBCollection users = collections.retrieve("users");

        note.put(
                "incident_id",
                collections.retrieve("incidents").findOne(
                        new BasicDBObject("incident_number", new BasicDBObject("$eq", incidentId))
                ).get("_id"));
        BasicDBObject user = (BasicDBObject) note.get("user");
        DBCursor storedUser = users.find(
                new BasicDBObject("id", new BasicDBObject("$eq", user.getString("id")))
        );
        if (!storedUser.hasNext()) {
            users.insert(user);
        }
        note.remove("user");
        note.put("user_id", users.findOne(
                new BasicDBObject("id", new BasicDBObject("$eq", user.getString("id")))
        ).get("_id"));
    }

}
