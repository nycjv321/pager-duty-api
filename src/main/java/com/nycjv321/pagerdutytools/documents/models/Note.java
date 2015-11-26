package com.nycjv321.pagerdutytools.documents.models;

import com.nycjv321.pagerdutytools.documents.QueryableDocument;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Property;
import de.caluga.morphium.annotations.caching.Cache;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.List;

import static com.nycjv321.pagerdutytools.utils.MongoConnector.createQueryFor;

/**
 * Created by jvelasquez on 4/15/15.
 */
@Entity(translateCamelCase = true, collectionName = "notes")
@Cache(maxEntries = 500)
public class Note extends QueryableDocument<Note> {
    private String content;
    private String summary;
    @Property(fieldName = "id")
    private String note_id;
    @Id
    private ObjectId _id;

    public static List<Note> all() {
        return createQueryFor(Note.class).asList();
    }

    public String getNote_id() {
        return note_id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public Query<Note> createQuery() {
        return createQueryFor(Note.class);
    }

    @Override
    public Query<Note> queryThis() {
        return createQuery().f("_id").eq(getId());
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public ObjectId getId() {
        return _id;
    }
}
