package com.nycjv321.pagerdutytools.updater;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.nycjv321.pagerdutytools.utils.Collections;
import com.nycjv321.pagerdutytools.models.Incident;
import com.nycjv321.pagerdutytools.models.User;
import org.bson.types.ObjectId;

import java.util.Objects;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class LogUpdater implements Updater {

    private final Collections collections;
    private final Incident incident;

    public LogUpdater(DB db, Incident incident) {
        this.collections = new Collections(db);
        this.incident = incident;
    }

    @Override
    public void update(BasicDBObject logInstance) {
        storeAgent(logInstance, "agent");
        storeAgent(logInstance, "user");
        storeAgent(logInstance, "assigned_user");
        final ObjectId incidentObjectId = incident.getObjectId();
        storeEmail(logInstance, incidentObjectId);
        storeNote(logInstance);
        addIncidentToLogEntry(logInstance, incidentObjectId);
    }

    private void addIncidentToLogEntry(BasicDBObject logInstance, ObjectId id) {
        DBObject incident = collections.retrieve("incidents").findOne(
                new BasicDBObject("_id", new BasicDBObject("$eq", id))
        );
        logInstance.put(
                "incident_id",
                incident.get("_id"));

    }

    private void storeAgent(BasicDBObject logEntry, String type) {
        if (logEntry.containsField(type)) {
            DBObject userObject = (DBObject) logEntry.get(type);
            Object id = userObject.get("id");
            if (Objects.nonNull(id)) {
                User user = User.find(id.toString());
                logEntry.put("user_id", user.getObjectId());
                logEntry.remove(type);
            }
        }
    }

    private void storeEmail(BasicDBObject logInstance, ObjectId incidentId) {
        if (logInstance.get("channel") != null) {
            BasicDBObject channel = (BasicDBObject) logInstance.get("channel");
            if (channel.get("type").equals("email")) {
                channel.remove("type");
                channel.put("incident_id", incidentId);
                collections.add(channel, "emails");
                logInstance.put("email_id", collections.retrieve("emails").findOne(
                        new BasicDBObject("raw_url", new BasicDBObject("$eq", channel.getString("raw_url")))
                ).get("_id"));
                logInstance.remove("channel");

            }
        }
    }

    private void storeNote(BasicDBObject logInstance) {
        if (logInstance.get("channel") != null) {
            BasicDBObject channel = (BasicDBObject) logInstance.get("channel");
            if (channel.get("type").equals("note")) {
                channel.remove("type");
                DBObject foundNote = findNote(channel);
                if (Objects.isNull(foundNote)) {
                    collections.add(channel, "notes");
                    foundNote = findNote(channel);
                }
                logInstance.put("note_id", foundNote.get("_id"));
                logInstance.remove("channel");

            }
        }
    }

    private DBObject findNote(BasicDBObject channel) {
        BasicDBObject contentEquals = new BasicDBObject("content",
                new BasicDBObject("$eq", channel.getString("content"))
        );
        BasicDBObject summaryEquals = new BasicDBObject("summary",
                new BasicDBObject("$eq", channel.getString("summary"))
        );

        BasicDBList list = new BasicDBList();
        list.add(contentEquals);
        list.add(summaryEquals);


        return collections.retrieve("notes").findOne(
                new BasicDBObject("$and", list));
    }


}
