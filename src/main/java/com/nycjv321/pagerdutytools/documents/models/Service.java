package com.nycjv321.pagerdutytools.documents.models;

import de.caluga.morphium.annotations.Embedded;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.caching.Cache;
import org.bson.types.ObjectId;

import static com.nycjv321.pagerdutytools.utils.MongoConnector.createQueryFor;

/**
 * Created by jvelasquez on 4/16/15.
 */
@Entity(translateCamelCase = true, collectionName = "services")
@Cache(maxEntries = 50)
public class Service {

    @Id
    private ObjectId _id;
    private String id;
    private String name;
    private String serviceUrl;
    private String autoResolveTimeout;
    private String acknowledgementTimeout;
    private String createdAt;
    private String deletedAt;
    private String status;
    private String lastIncidentTimestamp;
    private String emailIncidentCreation;
    private IncidentCounts incidentCounts;
    private String emailFilterMode;
    private String type;
    private String description;

    public Service() {
    }

    public static Service find(ObjectId service_id) {
        return createQueryFor(Service.class).f("_id").eq(service_id).get();
    }

    public String getName() {
        return name;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getId() {
        return id;
    }

    public ObjectId getObjectId() {
        return _id;
    }

    @Embedded
    private class IncidentCounts {
        private int triggered;
        private int resolved;
        private int acknowledged;
        private int total;
    }

}
