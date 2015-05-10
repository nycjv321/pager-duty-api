package com.nycjv321.pagerdutytools.models;

import com.nycjv321.pagerdutytools.MongoConnector;
import de.caluga.morphium.annotations.Embedded;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.annotations.caching.Cache;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by jvelasquez on 4/16/15.
 */
@Entity(translateCamelCase = true, collectionName = "log_entries")
@Cache(maxEntries = 5000)
public class LogEntry {

    @Id
    private ObjectId _id;

    private String id;
    private String type;
    private String created_at;
    private Channel channel;

    private ObjectId incidentId;

    @Reference(fieldName = "user_id", lazyLoading = true)
    private User userId;

    public User getUser() {
        return userId;
    }

    public Incident getIncident() {
        return Incident.find(incidentId);
    }

    /**
     * @return All triggers group by user
     */
    public static Map<String, List<LogEntry>> getTriggersByUser() {
        List<LogEntry> logEntries = MongoConnector.createQueryFor(LogEntry.class).
                f("channel.from").exists().f("channel.to").ne(null).f("type").eq("trigger").asList();

        Map<String, List<LogEntry>> triggersByUser = new HashMap<>();
        for (LogEntry logEntry : logEntries) {
            String from = logEntry.getChannel().getFrom();
            if (triggersByUser.containsKey(from)) {
                List<LogEntry> logEntryList = triggersByUser.get(from);
                logEntryList.add(logEntry);
                triggersByUser.put(from, logEntryList);
            } else {
                List<LogEntry> newEntry = new ArrayList<>();
                newEntry.add(logEntry);
                triggersByUser.put(logEntry.getChannel().getFrom(), newEntry);
            }
        }
        return triggersByUser;
    }

    public String getId() {
        return id;
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

    private ObjectId note_id;

    public Note getNote() {
        return MongoConnector.createQueryFor(Note.class).f("_id").eq(note_id).get();
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
