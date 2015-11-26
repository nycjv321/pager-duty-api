package com.nycjv321.pagerdutytools.documents.updater;

import com.mongodb.BasicDBObject;

/**
 * Created by jvelasquez on 11/10/15.
 */
public interface Updater {

    boolean updates(BasicDBObject object);

    void update(BasicDBObject object);
}
