package com.nycjv321.pagerdutytools.documents.models;

import com.nycjv321.pagerdutytools.documents.QueryableDocument;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.*;
import de.caluga.morphium.annotations.caching.Cache;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.Objects;

/**
 * Created by jvelasquez on 4/16/15.
 */
@Entity(translateCamelCase = true, collectionName = "log_entries")
@Cache(maxEntries = 5000)
public class LogEntry extends QueryableDocument<LogEntry> {

    @Id
    private ObjectId _id;
    @Property(fieldName = "id")
    private String log_entry_id;
    private String type;
    private String created_at;
    private Channel channel;

    private ObjectId incidentId;

    @Reference(fieldName = "user_id", lazyLoading = true)
    private User userId;
    private ObjectId note_id;

    public User getUser() {
        return userId;
    }

    public Incident getIncident() {
        return Incident.find(incidentId);
    }

    public String getLogEntry() {
        return log_entry_id;
    }

    public String getType() {
        return type;
    }

    public String getCreationDate() {
        return created_at;
    }

    public boolean hasChannel() {
        return Objects.nonNull(channel);
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean hasNotes() {
        return Objects.nonNull(note_id);
    }

    public Note getNote() {
        return MongoConnector.createQueryFor(Note.class).f("_id").eq(note_id).get();
    }

    @Override
    public Query<LogEntry> createQuery() {
        return MongoConnector.createQueryFor(LogEntry.class);
    }

    @Override
    public Query<LogEntry> queryThis() {
        return createQuery().f("_id").eq(getId());
    }

    @Override
    public ObjectId getId() {
        return _id;
    }

    @Embedded
    public static class Channel {
        private String date;
        private String to;
        private String subject;
        private String body;
        private String from;
        private String summary;
        private String type;

        public String getDate() {
            return date;
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getFrom() {
            return from;
        }

        public String getType() {
            return type;
        }

        public String getSummary() {
            return summary;
        }
    }


}
