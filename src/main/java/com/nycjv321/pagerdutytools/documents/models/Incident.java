package com.nycjv321.pagerdutytools.documents.models;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.nycjv321.pagerdutytools.SolrClientManager;
import com.nycjv321.pagerdutytools.documents.IndexedDocument;
import com.nycjv321.pagerdutytools.documents.QueryableDocument;
import com.nycjv321.pagerdutytools.rest.DBObjectDownloader;
import com.nycjv321.pagerdutytools.updater.IncidentUpdater;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Property;
import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.annotations.caching.Cache;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nycjv321.pagerdutytools.utils.MongoConnector.createQueryFor;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 * Created by jvelasquez on 4/14/15.
 */
@Entity(translateCamelCase = true, collectionName = "incidents")
@Cache(maxEntries = 500)
public class Incident extends QueryableDocument<Incident> implements Comparable<Incident>, IndexedDocument {
    @Id
    private ObjectId _id;

    @Property(fieldName = "trigger_details_html_url")
    private String triggerDetailsHtmlURL;
    @Property(fieldName = "id")
    private String incidentId;
    @Property(fieldName = "incident_number")
    private int incident_number;
    private String status;
    private String createdOn;
    private String last_status_change_on;
    @Property(fieldName = "html_url")
    private String url;
    @Property(fieldName = "trigger_type")
    private String triggerType;
    // perform translation to user objects?
    @Reference(fieldName = "last_status_change_by_id", lazyLoading = true)
    private User lastStateChangeBy;
    @Reference(fieldName = "resolved_by_user_id", lazyLoading = true)
    private User resolvedByUser;
    private int numberOfEscalations;
    private Map<String, String> triggerSummaryData;
    @Reference(fieldName = "service_id", lazyLoading = true)
    private Service service;
    private List<LogEntry> logEntries;
    private List<Note> notes;
    private String resolved_by_user_id;

    public static List<Incident> all() {
        return createQueryFor(Incident.class).asList();
    }

    public static List<Incident> get(int size) {
        return createQueryFor(Incident.class).sort("-incident_number").limit(size).asList();
    }

    public static int getCount() {
        return createQueryFor(Incident.class).asList().size();
    }

    public static List<Incident> findAll() {
        return MongoConnector.createQueryFor(Incident.class).asList();
    }

    public static Incident find(String incident_number) {
        return createQueryFor(Incident.class).f("id").eq(incident_number).get();
    }

    public static Incident find(ObjectId id) {
        return createQueryFor(Incident.class).f("_id").eq(id).get();

    }

    public static Incident find(int i) {
        return createQueryFor(Incident.class).f("incident_number").eq(i).get();
    }

    public void update(DBObjectDownloader downloader, IncidentUpdater updater) {
        BasicDBObject incident = downloader.getIncident(incident_number);
        updater.update(incident);
        DB database = MongoConnector.getMorphium().getDatabase();
        database.getCollection("incidents").update(queryThis().toQueryObject(), incident);
    }

    @Override
    public void delete() {
        getLogEntries().forEach(LogEntry::delete);
        getNotes().forEach(QueryableDocument::delete);
        Email email = getEmail();
        if (nonNull(email)) {
            email.delete();
        }
        super.delete();
    }

    @Override
    public Query<Incident> createQuery() {
        return MongoConnector.createQueryFor(Incident.class);
    }

    @Override
    public Query<Incident> queryThis() {
        return createQuery().f("_id").eq(getId());
    }

    public String getSummary() {
        return triggerSummaryData.get("subject");
    }

    public List<Note> getNotes() {
        if (Objects.isNull(notes)) {

            BasicDBObject query = new BasicDBObject();
            query = query.append("incident_id", new BasicDBObject("$exists", true));
            query = query.append("incident_id", new BasicDBObject("$eq", getId()));
            notes = createQueryFor(Note.class).complexQuery(query);
        }
        return notes;
    }

    public Email getEmail() {
        return Email.findByIncident(this);
    }

    public Service getService() {
        return service;
    }

    public String getIncidentId() {
        return incidentId;
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
            logEntries = createQueryFor(LogEntry.class).f("incident_id").eq(getId()).asList();
        }
        return logEntries;
    }

    @Override
    public boolean equals(Object that) {
        if (Objects.isNull(that) || !(that instanceof Incident)) {
            return false;
        }

        return ((Incident) that).getId().equals(this.getId());
    }

    public String getServiceName() {
        final Service service = getService();
        if (nonNull(service)) {
            return service.getName();
        } else {
            return "No Service Defined";
        }
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

    @Override
    public void index() {
        Map<String, Object> data = new HashMap<>();
        Email email = getEmail();
        if (nonNull(email)) {
            data.putAll(email.toMap());
        }
        List<String> notes = getNotes().stream().map(Note::getContent).collect(toList());
        if (!notes.isEmpty()) {
            data.put("notes", notes);
        }

        data.put("trigger_type", triggerType);
        data.put("number_of_escalations", numberOfEscalations);
        data.put("created_on", createdOn);
        data.put("last_status_change_on", last_status_change_on);
        data.put("id", getId().toString());
        SolrClientManager.save(data);
    }

    @Override
    public boolean isOld() {
        return false;
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public ObjectId getId() {
        return _id;
    }
}
