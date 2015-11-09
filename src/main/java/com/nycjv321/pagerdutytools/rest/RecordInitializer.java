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
         collections.addAll(downloader.getUsers(), "users");
         collections.addAll(downloader.getServices(), "services");
         collections.addAll(downloader.getIncidents(db), "incidents");
         collections.addAll(downloader.getNotes(), "notes");

         collections.addAll(downloader.getLogEntries(db), "log_entries");
        MongoConnector.close();
    }


}
