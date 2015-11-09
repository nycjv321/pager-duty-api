package com.nycjv321.pagerdutytools.models;

import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.caching.Cache;
import org.bson.types.ObjectId;

import java.util.List;

import static com.nycjv321.pagerdutytools.utils.MongoConnector.createQueryFor;

/**
 * Created by jvelasquez on 4/15/15.
 */
@Entity(translateCamelCase = true, collectionName = "notes")
@Cache(maxEntries = 500)
public class Note {
    @Id
    private ObjectId _id;

    private String content;
    private String summary;

    private String id;

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public static List<Note> all() {
        return createQueryFor(Note.class).asList();
    }


    public ObjectId getObjectId() {
        return _id;
    }

    public String getSummary() {
        return summary;
    }
}
