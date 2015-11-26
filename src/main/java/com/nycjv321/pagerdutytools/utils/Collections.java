package com.nycjv321.pagerdutytools.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Created by jvelasquez on 11/9/15.
 */
public class Collections {

    private final DB db;

    public Collections(DB db) {
        this.db = db;
    }

    public void add(BasicDBObject object, String collection) {
        retrieve(collection).insert(object);
    }

    public void add(BasicDBList list, String collection) {
        retrieve(collection).insert(list);
    }

    public DBCollection retrieve(String collection) {
        return db.getCollection(collection);
    }

    public void add(BasicDBObject[] array, String collection) {
        retrieve(collection).insert(array);

    }
}
