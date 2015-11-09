package com.nycjv321.pagerdutytools.utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by jvelasquez on 4/26/15.
 */
public class MongoQueryFilters {

    public static BasicDBObject and(List<BasicDBObject> basicDBObjects) {
        BasicDBList list = createList();
        list.addAll(basicDBObjects.stream().collect(Collectors.toList()));
        BasicDBObject and = createObject();
        and.put("$and", list);
        return and;
    }
    
    public static BasicDBObject createObject() {
        return new BasicDBObject();
    }

    public static BasicDBObject createObject(String key, Object value) {
        BasicDBObject object = createObject();
        object.put(key, value);
        return object;
    }

    public static BasicDBList createList() {
        return new BasicDBList();
    }

    public static DBObject sort(int direction) {
        return createObject("$sort", createObject("count", direction));
    }

    public static BasicDBObject exists(String field) {
        return createObject(field, createObject("$exists", true));

    }

    public static BasicDBObject equals(String field, String anotherField) {
        return createObject(field, createObject("$eq", anotherField));
    }

    public static BasicDBObject notEquals(String field, String anotherField) {
        return createObject(field, createObject("$ne", anotherField));
    }

    public static DBObject groupBy(String field) {
        BasicDBObject parameters = createObject("_id", String.format("$%s", field));
        BasicDBObject count = createObject("$sum", 1);
        parameters.put("count", count);
        return createObject("$group", parameters);
    }

    public static BasicDBObject matches(String subject, String regex) {
        return createObject(subject, createObject("$regex", regex));
    }

    public static BasicDBObject notMatches(String subject, Pattern pattern) {
        return createObject(subject, createObject("$not", pattern));
    }
}
