package com.nycjv321.pagerdutytools.updater;

import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by fedora on 11/26/15.
 */
public class IncidentUpdaterTest {

    @Test
    public void update() throws Exception {

        BasicDBObject incident = new BasicDBObject();
        incident.put("child", new BasicDBObject(ImmutableMap.of("id", "1", "key1", "value1", "key2", "value2")));
        DB db = MongoConnector.getMorphium().getDatabase();

        IncidentUpdater incidentUpdater = new IncidentUpdater(db);
        incidentUpdater.update(incident, "children", "child", "id", "child_id");

        assertNull(incident.get("child"));
        assertTrue(incident.get("child_id") instanceof ObjectId);
    }

    @Test
    public void updateNoChild() throws Exception {

        BasicDBObject incident = new BasicDBObject();
        DB db = MongoConnector.getMorphium().getDatabase();

        IncidentUpdater incidentUpdater = new IncidentUpdater(db);
        incidentUpdater.update(incident, "children", "child", "id", "child_id");

        assertTrue(incident.keySet().isEmpty());
    }

    @Test
    public void updateIncidentService() throws Exception {

        BasicDBObject incident = new BasicDBObject();
        incident.put("service", new BasicDBObject(ImmutableMap.of("id", "1", "key1", "value1", "key2", "value2")));
        DB db = MongoConnector.getMorphium().getDatabase();

        IncidentUpdater incidentUpdater = new IncidentUpdater(db);
        incidentUpdater.updateIncidentService(incident);

        BasicDBObject serviceObject = (BasicDBObject) incident.get("service");
        assertNull(serviceObject);
        assertTrue(incident.get("service_id") instanceof ObjectId);

        DBObject queriedService = db.getCollection("services").findOne(
                new BasicDBObject("_id", new BasicDBObject("$eq", incident.get("service_id"))
                ));

        assertNotNull(queriedService);
        assertEquals(queriedService.get("key1"), "value1");
        assertEquals(queriedService.get("key2"), "value2");
    }

}