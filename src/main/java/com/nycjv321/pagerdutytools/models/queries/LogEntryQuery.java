package com.nycjv321.pagerdutytools.models.queries;

import com.nycjv321.pagerdutytools.models.LogEntry;
import com.nycjv321.pagerdutytools.utils.MongoConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fedora on 11/21/15.
 */
public class LogEntryQuery {
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
}
