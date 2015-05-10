package com.nycjv321.pagerdutytools.models;

import com.nycjv321.pagerdutytools.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Property;
import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.annotations.caching.Cache;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

import static com.nycjv321.pagerdutytools.MongoConnector.createQueryFor;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by jvelasquez on 4/14/15.
 */
@Entity(translateCamelCase = true, collectionName = "incidents")
@Cache(maxEntries = 500)
public class Incident implements Comparable<Incident> {

    @Property(fieldName = "trigger_details_html_url")
    private String triggerDetailsHtmlURL;
    @Id
    private ObjectId _id;
    private String id;
    @Property(fieldName = "incident_number")
    private int incident_number;
    private String status;
    private String createdOn;
    @Property(fieldName = "html_url")
    private String url;
    private List<LogEntry> notes;
    private String triggerType;
    // perform translation to user objects?
    @Reference(fieldName = "last_status_change_by_id", lazyLoading = true)
    private User lastStateChangeBy;
    @Reference(fieldName = "resolved_by_user_id", lazyLoading = true)
    private User resolvedByUser;
    private int numberOfEscalations;
    private String incidentKey;
    private Map<String, String> triggerSummaryData;
    @Reference(fieldName = "service_id", lazyLoading = true)
    private Service service;
    private List<LogEntry> logEntries;

    private static Pattern getPattern(String searchTerm) {
        return Pattern.compile(".*" + searchTerm + ".*", Pattern.CASE_INSENSITIVE);
    }

    public static List<Incident> all() {
        return createQueryFor(Incident.class).asList();
    }

    public static List<Incident> get(int size) {
        return createQueryFor(Incident.class).sort("-incident_number").limit(size).asList();
    }

    public static int getCount() {
        return createQueryFor(Incident.class).asList().size();
    }

    public static Incident find(int i) {
        return createQueryFor(Incident.class).f("incident_number").eq(i).get();
    }

    public static Incident find(ObjectId incidentId) {
        return createQueryFor(Incident.class).f("_id").eq(incidentId).get();
    }

    public String getSummary() {
        return triggerSummaryData.get("subject");
    }

    public List<LogEntry> getNotes() {
        if (Objects.isNull(notes)) {
            notes = createQueryFor(LogEntry.class).f("incident_id").eq(_id).asList().stream().filter(LogEntry::hasNotes).collect(toList());
        }
        return notes;
    }

    public Email getEmail() {
        return Email.findByIncident(this);
    }

    public Service getService() {
        return service;
    }

    public String getId() {
        return id;
    }

    public int getNumber() {
        return incident_number;
    }

    public String getStatus() {
        return status;
    }

    public String getTriggerDetailsHtmlURL() {
        return triggerDetailsHtmlURL;
    }

    public String getCreationDate() {
        return createdOn;
    }

    public String getUrl() {
        return url;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public int getEscalations() {
        return numberOfEscalations;
    }

    public User getLastStateChangeBy() {
        return lastStateChangeBy;
    }

    public User getResolvedBy() {
        return resolvedByUser;
    }

    public List<LogEntry> getLogEntries() {
        if (Objects.isNull(logEntries)) {
            logEntries = createQueryFor(LogEntry.class).f("incident_id").eq(this._id).asList();
        }
        return logEntries;
    }

    @Override
    public boolean equals(Object that) {

        if (Objects.isNull(that) || that instanceof Incident) {
            return false;
        }

        Incident otherIncident = (Incident) that;

        if (otherIncident._id.equals(this._id)) {
            return true;
        } else {
            return false;
        }
    }

    public String getTeam() {
        return getService().getName();
    }

    @Override
    public int compareTo(Incident that) {
        if (that.getNumber() > this.getNumber()) {
            return -1;
        } else if (that.getNumber() < this.getNumber()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static List<Incident> findByTeam(String team) {
        ObjectId service = createQueryFor(Service.class).f("name").matches(getPattern(team)).get().getObjectId();
        List<Incident> incidents = createQueryFor(Incident.class).f("service_id").eq(service).asList();
        Collections.reverse(incidents);
        return incidents;
    }

    public static List<Incident> find(String string) {
        Set<Incident> incidents;
        Query<Email> emailQuery = createQueryFor(Email.class).q();

        incidents = emailQuery.or(
                emailQuery.q().f("subject").matches(getPattern(string)),
                emailQuery.q().f("body").matches(getPattern(string)),
                emailQuery.q().f("summary").matches(getPattern(string)),
                emailQuery.q().f("from").matches(getPattern(string)),
                emailQuery.q().f("to").matches(getPattern(string))
        ).asList().stream().map(Email::getIncident).collect(toSet());

        Query<Note> query = createQueryFor(Note.class).q();

        incidents.addAll(query.or(
                query.q().f("content").matches(getPattern(string)),
                query.q().f("summary").matches(getPattern(string))
        ).asList().stream().map(Incident::findByNote).collect(toSet()));

        List<Incident> incidentList = new ArrayList<>(incidents);
        Collections.sort(incidentList);
        Collections.reverse(incidentList);
        return incidentList;
    }

    private static Incident findByNote(Note note) {
        return MongoConnector.createQueryFor(LogEntry.class).f("note_id").eq(note.getObjectId()).get().getIncident();
    }

    public static List<Incident> find(int start, int end) {
        List<Incident> incidents = createQueryFor(Incident.class).f("incident_number").gte(start).f("incident_number").lte(end).asList();
        Collections.reverse(incidents);
        return incidents;
    }

    public ObjectId getObjectId() {
        return _id;
    }
}
