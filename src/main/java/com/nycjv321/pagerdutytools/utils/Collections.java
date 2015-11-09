package com.nycjv321.pagerdutytools.utils;

import com.mongodb.*;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class Collections {

    private final DB db;

    public Collections(DB db) {
        this.db = db;
    }

    public void addTo(BasicDBObject object, String collection) {
        retrieve(collection).insert(object);
    }

    public void addAll(BasicDBList list, String collection) {
        DBCollection documents = retrieve(collection);
        documents.insert(list);
    }

    public DBCollection retrieve(String collection) {
        return db.getCollection(collection);
    }
}
