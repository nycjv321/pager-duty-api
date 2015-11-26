package com.nycjv321.pagerdutytools.documents.models;

import com.nycjv321.pagerdutytools.documents.IndexedDocument;
import com.nycjv321.pagerdutytools.documents.QueryableDocument;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.caching.Cache;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jvelasquez on 5/4/15.
 */
@Entity(translateCamelCase = true, collectionName = "emails")
@Cache(maxEntries = 500)
public class Email extends QueryableDocument<Email> implements IndexedDocument {
    private static final Pattern serverProcessPattern = Pattern.compile("([\\w\\d.]+):([\\w\\d]+)");
    private String to;
    private String from;
    private String body;
    private String subject;
    private String summary;
    private String rawUrl;
    private String bodyContentType;
    private String details;
    private String htmlUrl;
    private ObjectId incident_id;
    @Id
    private ObjectId _id;

    public static Email findByIncident(Incident incident) {
        return MongoConnector.createQueryFor(Email.class).f("incident_id").eq(incident.getId()).get();
    }

    public static List<Email> findAll() {
        return MongoConnector.createQueryFor(Email.class).asList();
    }

    @Override
    public Query<Email> createQuery() {
        return MongoConnector.createQueryFor(Email.class);
    }

    @Override
    public Query<Email> queryThis() {
        return createQuery().f("_id").eq(getId());
    }

    public boolean isAutomated() {
        return from.toLowerCase().contains("xymon");
    }

    public String getSubject() {
        return subject;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    public String getSummary() {
        return summary;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public Incident getIncident() {
        return MongoConnector.createQueryFor(Incident.class).f("_id").eq(incident_id).get();
    }

    @Override
    public boolean isOld() {
        return false;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", getId().toString());
        data.put("type", "email");
        data.put("to", to);

        data.put("source", from.split("@")[1].replace(">", ""));
        boolean automated = isAutomated();
        data.put("is_automated", automated);
        if (automated) {
            Matcher matcher = serverProcessPattern.matcher(summary);
            if (matcher.find() && matcher.groupCount() == 2) {
                data.put("server", matcher.group(1));
                data.put("process", matcher.group(2));
            }
        }

        data.put("from", from);
        data.put("body", body);
        data.put("subject", subject);
        data.put("details", details);
        data.put("type", "incident");
        return data;
    }

    @Override
    public ObjectId getId() {
        return _id;
    }
}
