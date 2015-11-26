package com.nycjv321.pagerdutytools.rest;

import com.mongodb.DB;
import com.nycjv321.pagerdutytools.utils.Collections;
import com.nycjv321.pagerdutytools.utils.MongoConnector;

import java.io.IOException;

public class RecordInitializer {

    private final DB db;
    private DBOjectDownloader downloader = new DBOjectDownloader();
    private Collections collections;

    public RecordInitializer() {
        db = MongoConnector.getMorphium().getDatabase();
        collections = new Collections(db);
    }

    public static void main(String[] args) throws IOException {
        RecordInitializer r = new RecordInitializer();
        r.init();
        MongoConnector.close();
    }

    public void init() throws IOException {
        collections.add(downloader.getUsers(), "users");
        collections.add(downloader.getServices(), "services");
        collections.add(downloader.getIncidents(db), "incidents");
        collections.add(downloader.getNotes(), "notes");

        collections.add(downloader.getLogEntries(db), "log_entries");
    }


}
