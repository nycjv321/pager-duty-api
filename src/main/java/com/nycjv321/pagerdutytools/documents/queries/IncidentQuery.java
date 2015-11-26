package com.nycjv321.pagerdutytools.documents.queries;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.nycjv321.pagerdutytools.documents.models.User;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import com.nycjv321.pagerdutytools.utils.MongoQueryFilters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jvelasquez on 4/26/15.
 */
public class IncidentQuery {

    public static List<DBObject> getTriggerCount() {
        DBCollection database = MongoConnector.getCollection("emails");
        Iterable<DBObject> dbObjects = MongoConnector.groupByMatchAggregate(
                database,
                Arrays.asList(
                        MongoQueryFilters.exists("from"),
                        MongoQueryFilters.notMatches("subject", Pattern.compile("^Re: ")),
                        MongoQueryFilters.notEquals("to", null)
                ),
                "from",
                -1
        );
        return Lists.newArrayList(dbObjects);
    }

    // TODO convert this to aggregation
    public static List<DBObject> getResolveCount() {
        List<User> users = User.all();

        List<DBObject> resolveCount = new ArrayList<>();
        for (User user : users) {
            BasicDBObject object = MongoQueryFilters.createObject("user", user.getName());
            object.put("count", user.getResolveCount());
            resolveCount.add(object);
        }
        Collections.sort(resolveCount, (one, o2) -> {
            if (Integer.parseInt(one.get("count").toString()) == Integer.parseInt(o2.get("count").toString())) {
                return 0;
            } else if (Integer.parseInt(one.get("count").toString()) > Integer.parseInt(o2.get("count").toString())) {
                return -1;
            } else {
                return 1;
            }
        });

        return resolveCount;
    }


}
