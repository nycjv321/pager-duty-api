package com.nycjv321.pagerdutytools.documents.models;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Property;
import de.caluga.morphium.annotations.caching.Cache;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

import static com.nycjv321.pagerdutytools.utils.MongoConnector.createQueryFor;

/**
 * Created by jvelasquez on 4/16/15.
 */
@Entity(translateCamelCase = true, collectionName = "users")
@Cache(maxEntries = 50)
public class User {

    @Id
    private ObjectId _id;

    @Property(fieldName = "id")
    private String note_id;

    private String name;
    private String email;
    private String timeZone;
    private String role;
    private String userUrl;
    private String jobTitle;
    private String description;

    private List<Incident> incidents;

    public static List<User> all() {
        return createQueryFor(User.class).asList();
    }

    public static User findByEmail(String email) {
        return createQueryFor(User.class).f("email").eq(email).get();
    }

    public static User find(String id) {
        return createQueryFor(User.class).f("id").eq(id).get();
    }

    public List<Incident> getIncidents() {
        if (Objects.isNull(incidents)) {
            incidents = createQueryFor(Incident.class).f("resolved_by_user.id").eq(note_id).asList();
        }
        return incidents;
    }

    public int getResolveCount() {
        DBCollection database = MongoConnector.getCollection("log_entries");
        BasicDBList list = new BasicDBList();
        list.add(new BasicDBObject("type", "resolve"));
        list.add(new BasicDBObject("user_id", _id));
        BasicDBObject query = new BasicDBObject("$and", list);
        return database.find(query).count();
    }

    public String getNoteId() {
        return note_id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ObjectId getObjectId() {
        return _id;
    }
}
