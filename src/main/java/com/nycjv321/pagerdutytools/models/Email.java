package com.nycjv321.pagerdutytools.models;

import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.caching.Cache;
import org.bson.types.ObjectId;

/**
 * Created by jvelasquez on 5/4/15.
 */
@Entity(translateCamelCase = true, collectionName = "emails")
@Cache(maxEntries = 500)
public class Email {
    @Id
    private ObjectId _id;
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

    public static Email findByIncident(Incident incidentId) {
        return MongoConnector.createQueryFor(Email.class).f("incident_id").eq(incidentId.getObjectId()).get();
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
}
