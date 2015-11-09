package com.nycjv321.pagerdutytools.updater;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * Created by jvelasquez on 11/9/15.
 */
public interface Updater {

    void update(BasicDBObject object);

    default void update(BasicDBList objects) {
        for (Object incidentObject : objects) {
            update((BasicDBObject) incidentObject);
        }
    }
}
