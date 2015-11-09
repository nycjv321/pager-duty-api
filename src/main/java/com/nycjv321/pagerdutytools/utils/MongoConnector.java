package com.nycjv321.pagerdutytools.utils;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.nycjv321.pagerdutytools.Configuration;
import de.caluga.morphium.Morphium;
import de.caluga.morphium.MorphiumConfig;
import de.caluga.morphium.MorphiumSingleton;
import de.caluga.morphium.query.Query;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static com.nycjv321.pagerdutytools.utils.MongoQueryFilters.*;

/**
 * Created by jvelasquez on 4/14/15.
 */
public class MongoConnector {

    private static final Morphium morphium;

    static {
        // param this stuff
        MorphiumConfig cfg = new MorphiumConfig();
        try {
            cfg.addHost(System.getProperty("mongodb.host", Configuration.getHost()), Configuration.getPort());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        cfg.setDatabase(Configuration.getDatabaseName());
        MorphiumSingleton.setConfig(cfg);
        morphium = MorphiumSingleton.get();

    }

    public static Iterable<DBObject> groupByMatchAggregate(DBCollection database, List<BasicDBObject> filters, String groupBy, int direction) {
        BasicDBObject match = new BasicDBObject();
        match.put("$match", and(filters));
        AggregationOutput aggregationOutput;
        aggregationOutput = database.aggregate(
                Arrays.asList(
                        match,
                        groupBy(groupBy),
                        sort(direction)
                )
        );
        return aggregationOutput.results();
    }

    public static Morphium getMorphium() {
        return morphium;
    }

    public static <T> Query<T> createQueryFor(Class<? extends T> type) {
        return getMorphium().createQueryFor(type);
    }

    public static DBCollection getCollection(String collection) {
        return MongoConnector.getMorphium().getDatabase().getCollection(collection);
    }

    public static void close() {
        getMorphium().close();
    }

}
