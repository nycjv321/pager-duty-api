package com.nycjv321.pagerdutytools.documents.models;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.nycjv321.pagerdutytools.rest.MockDBObjectDownloader;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by fedora on 11/26/15.
 */
public class IncidentTest {
    private final MockDBObjectDownloader objectDownloader = new MockDBObjectDownloader();
    private final DB db = MongoConnector.getMorphium().getDatabase();

    @Test(dependsOnGroups = "initialize_fixtures")
    public void testAll() throws Exception {

        List<Incident> all = Incident.all();
        assertTrue(all.size() > 0);
        List<BasicDBObject> dbObjects = Arrays.asList(objectDownloader.getIncidents(db));

        for (Incident incident : all) {
            assertTrue(
                    dbObjects.stream().filter(
                            d -> d.getString("id").equals(
                                    incident.getIncidentId()
                            )
                    ).findFirst().isPresent()
            );
        }


    }

    @Test(dependsOnGroups = "initialize_fixtures")
    public void testFind() throws Exception {
        List<Incident> all = Incident.all();
        assertTrue(all.size() > 0);
        for (int i = 1; i <= all.size(); i++) {
            BasicDBObject object = objectDownloader.getIncident(i);
            final int currentIncidentNumber = i;
            Incident incident = all.stream().filter(
                    a -> a.getNumber() == currentIncidentNumber
            ).findFirst().get();
            assertEquals(object.get("id"), incident.getIncidentId());
        }

    }
}