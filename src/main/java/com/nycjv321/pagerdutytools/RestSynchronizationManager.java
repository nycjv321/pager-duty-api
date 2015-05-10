package com.nycjv321.pagerdutytools;

import com.google.common.collect.ImmutableMap;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.nycjv321.pagerdutytools.models.Incident;
import com.nycjv321.pagerdutytools.models.User;
import com.nycjv321.utilities.CalendarUtilities;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.nycjv321.utilities.HttpUtilities.get;
import static java.util.Objects.isNull;

/**
 * Created by jvelasquez on 4/14/15.
 */
public class RestSynchronizationManager {
    public static final Header authentication = new BasicHeader("Authorization", "Token token=" + Configuration.getAuthorizationToken());
    public static final Header contentType = new BasicHeader("Content-type", "application/json");
    private CalendarUtilities calendarUtilities = new CalendarUtilities("yyyy-MM-dd");

    private DB db;

    public RestSynchronizationManager() {
        db = MongoConnector.getMorphium().getDatabase();
    }

    public static void main(String[] args) throws IOException {
        RestSynchronizationManager r = new RestSynchronizationManager();
        r.init();
        MongoConnector.close();
    }


    public void updateUsers() {
        DBCollection logEntries = db.getCollection("log_entries");
        DBCursor query = logEntries.find(null);
        for (DBObject dbObject : query) {
            BasicDBObject next = (BasicDBObject) dbObject;
            storeAgent(next, "agent");
            storeAgent(next, "user");
            storeAgent(next, "assigned_user");

            logEntries.remove(next);
        }
    }

    public static BasicDBList downloadNotes(int id) {
        String json = get(PagerDutyRestEndPoints.notes(id), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);
        assert data != null;

        return (BasicDBList) data.get("notes");
    }

    public void init() throws IOException {
//        saveToCollection(downloadUsers(), "users");
//        saveToCollection(downloadServices(), "services");
//        saveToCollection(downloadIncidents(), "incidents");
//
//        saveToCollection(downloadLogEntries(), "log_entries");
        MongoConnector.close();
    }

    private BasicDBList downloadLogEntries() {
        BasicDBList logEntries = new BasicDBList();
        List<Incident> incidents = Incident.all();
        // this takes a while
        for (int i = 1; i <= incidents.size(); ++i) {

            Incident incident = incidents.get(i - 1);
            String incidentId = incident.getId();
            BasicDBList logInstances = downloadLogEntries(incidentId);
            ObjectId objectId = incident.getObjectId();
            for (int j = 0; j < logInstances.size(); ++j) {
                BasicDBObject logInstance = (BasicDBObject) logInstances.get(j);
                if (isNull(logInstance)) {
                    continue;
                }
                storeAgent(logInstance, "agent");
                storeAgent(logInstance, "user");
                storeAgent(logInstance, "assigned_user");
                storeEmail(logInstance, objectId);
                storeNote(logInstance);
//                storeChannel(logEntries);
                addIncidentToLogEntry(logInstance, i);
                logEntries.add(logInstance);
            }
            sleep();
        }
        return logEntries;
    }

    private void storeEmail(BasicDBObject logInstance, ObjectId incidentId) {
        if (logInstance.get("channel") != null) {
            BasicDBObject channel = (BasicDBObject) logInstance.get("channel");
            if (channel.get("type").equals("email")) {
                channel.remove("type");
                channel.put("incident_id", incidentId);
                addToCollection(channel, "emails");
                logInstance.put("email_id", db.getCollection("emails").findOne(
                        new BasicDBObject("raw_url", new BasicDBObject("$eq", channel.getString("raw_url")))
                ).get("_id"));
                logInstance.remove("channel");

            }
        }

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


    private void sleep() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {

        }
    }

    private void addIncidentToLogEntry(BasicDBObject logInstance, int id) {
        DBObject incident = db.getCollection("incidents").findOne(
                new BasicDBObject("incident_number", new BasicDBObject("$eq", id))
        );
        logInstance.put(
                "incident_id",
                incident.get("_id"));

    }

    public class UnResolvedIncidentsException extends Exception {
        public UnResolvedIncidentsException(String message) {
            super(message);
        }
    }

    public synchronized boolean updateIncidents() throws UnResolvedIncidentsException {
        int restCount = getIncidentCount();
        int dbCount = Incident.getCount();
        if (restCount > dbCount) {
            for (int i = dbCount + 1; i <= restCount; ++i) {
                BasicDBObject incidentObject = downloadIncident(i);

                if (!incidentObject.getString("status").equals("resolved")) {
                    throw new UnResolvedIncidentsException(
                            String.format(
                                    "Incident %d is still %s. Lets resolve it first",
                                    incidentObject.getInt("incident_number"),
                                    incidentObject.getString("status"))
                    );
                }
                addToCollection(incidentObject, "incidents");
                Incident incident = Incident.find(i);
                BasicDBList logEntries = downloadLogEntries(incident.getId());
                for (int j = 0; j < logEntries.size(); ++j) {
                    BasicDBObject logInstance = (BasicDBObject) logEntries.get(j);
                    if (isNull(logInstance)) {
                        continue;
                    }
                    storeAgent(logInstance, "agent");
                    storeAgent(logInstance, "user");
                    storeAgent(logInstance, "assigned_user");
                    storeEmail(logInstance, incident.getObjectId());
                    storeNote(logInstance);
                    addIncidentToLogEntry(logInstance, i);
                }
                saveToCollection(logEntries, "log_entries");

//                BasicDBList noteInstances = downloadNotes(i);
//                for (int x = 0; x < noteInstances.size(); ++x) {
//                    updateNote((BasicDBObject) noteInstances.get(x), i);
//                }
//                saveToCollection(noteInstances, "notes");
            }
            return true;
        } else if (dbCount > restCount) {
            throw new IllegalStateException("More entries in db then rest interface! please rerun init()");
        } else {
            Incident.find(dbCount - 10, dbCount);
        }
        return false;
    }

    private void storeNote(BasicDBObject logInstance) {
        if (logInstance.get("channel") != null) {
            BasicDBObject channel = (BasicDBObject) logInstance.get("channel");
            if (channel.get("type").equals("note")) {
                channel.remove("type");
                DBObject foundNote = findNote(channel);
                if (Objects.isNull(foundNote)) {
                    addToCollection(channel, "notes");
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


        return db.getCollection("notes").findOne(
                new BasicDBObject("$and", list));
    }

    private BasicDBObject downloadIncident(int i) {

        String json = get(PagerDutyRestEndPoints.incident(i), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);
        // insert user translation
        assert data != null;
        updateIncident(data);

        return data;
    }

    private BasicDBList downloadLogEntries(String i) {

        String json = get(
                PagerDutyRestEndPoints.logEntries(i), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);

        // insert user translation
        assert data != null;
        return (BasicDBList) data.get("log_entries");
    }

    private void saveToCollection(BasicDBList list, String collection) {
        DBCollection documents = getCollection(collection);
        for (int i = 0; i < list.size(); ++i) {
            documents.insert((DBObject) list.get(i));
        }
    }

    private void addToCollection(BasicDBObject object, String collection) {
        getCollection(collection).insert(object);
    }

    private DBCollection getCollection(String collection) {
        return db.getCollection(collection);
    }

    public BasicDBList downloadServices() {
        BasicDBList users = new BasicDBList();
        String json = get(PagerDutyRestEndPoints.services(), contentType, authentication);

        BasicDBObject data = (BasicDBObject) JSON.parse(json);

        // insert user translation
        assert data != null;
        users.addAll((BasicDBList) data.get("services"));

        return users;
    }

    public BasicDBList downloadUsers() {
        BasicDBList users = new BasicDBList();
        String json = get(PagerDutyRestEndPoints.users(ImmutableMap.of("limit", "100")), contentType, authentication);

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

    public void updateNote(BasicDBObject note, int incident) {
        DBCollection users = getCollection("users");

        note.put(
                "incident_id",
                db.getCollection("incidents").findOne(
                        new BasicDBObject("incident_number", new BasicDBObject("$eq", incident))
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

    public BasicDBList downloadAllNotes() {
        int incidents = Incident.all().size();
        BasicDBList notes = new BasicDBList();
        // this takes a while
        for (int i = 1; i <= incidents; ++i) {
            BasicDBList noteInstances = downloadNotes(i);
            for (int j = 0; j < noteInstances.size(); ++j) {
                updateNote((BasicDBObject) noteInstances.get(j), i);
            }
            notes.addAll(noteInstances);
        }
        return notes;
    }

    public BasicDBList downloadIncidents() {
        BasicDBList incidents = new BasicDBList();
        int expectedIncidents = getIncidentCount();
        int offset = 0;
        while (incidents.size() < expectedIncidents) {
            String json = get(
                    PagerDutyRestEndPoints.incidents(
                            ImmutableMap.of("date_range", "all", "offset", String.valueOf(offset))), contentType, authentication);

            BasicDBObject data = (BasicDBObject) JSON.parse(json);

            assert data != null;
            BasicDBList incidentList = (BasicDBList) data.get("incidents");

            incidentList.forEach(this::updateIncident);

            incidents.addAll(incidentList);
            offset += 100;
        }
        return incidents;
    }

    private void updateIncident(Object object) {
        BasicDBObject incident = (BasicDBObject) object;
        updateIncidentService(incident);
        updateIncidentAssignedToUser(incident);
        updateResolvedByUser(incident);
        updateLastStatusChangeBy(incident);
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
        Object serviceId = db.getCollection("services").findOne(
                new BasicDBObject("id", new BasicDBObject("$eq", service.getString("id")))
        ).get("_id");
        incident.remove("service");
        incident.put("service_id", serviceId);

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
        String json = get(PagerDutyRestEndPoints.incidentCount(params), contentType, authentication);
        try {
            return new JSONObject(json).getInt("total");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public int getIncidentCount() {
        return getIncidentCountSince(null);
    }


}
