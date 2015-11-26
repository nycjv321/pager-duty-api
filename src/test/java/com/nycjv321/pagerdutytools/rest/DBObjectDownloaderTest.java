package com.nycjv321.pagerdutytools.rest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by fedora on 11/26/15.
 */
public class DBObjectDownloaderTest {

    private final MockDBObjectDownloader objectDownloader = new MockDBObjectDownloader();
    private final DB db = MongoConnector.getMorphium().getDatabase();


    @BeforeClass
    public void beforeClass() {
        resetDB();
    }

    private void resetDB() {
        db.getCollection("incidents").drop();
        db.getCollection("notes").drop();
        db.getCollection("services").drop();
        db.getCollection("log_entries").drop();
        db.getCollection("users").drop();
    }

    @Test
    public void getIncident() {
        BasicDBObject incident = objectDownloader.getIncident(1);
        assertEquals(incident.getString("id"), "B7242X");
    }

    @Test(groups = {"initialize_fixtures"}, dependsOnMethods = "getServices")
    public void getIncidents() {
        BasicDBObject[] incidents = objectDownloader.getIncidents(db);
        assertTrue(incidents.length > 0);
        db.getCollection("incidents").insert(incidents);
    }

    @Test
    public void getNotes() {
        BasicDBObject[] notes = objectDownloader.getNotes("PIJ90N7");
        assertTrue(notes.length > 0);
        assertEquals(notes[0].getString("id"), "22012");

    }

    @Test(groups = {"initialize_fixtures"}, dependsOnMethods = "getIncidents")
    public void getAllNotes() {
        BasicDBObject[] notes = objectDownloader.getNotes();
        assertTrue(notes.length > 0);
        db.getCollection("notes").insert(notes);

    }

    @Test(groups = {"initialize_fixtures"}, dependsOnMethods = "getUsers")
    public void getServices() {
        BasicDBObject[] services = objectDownloader.getServices();
        assertTrue(services.length > 0);
        db.getCollection("services").insert(services);
    }

    @Test(groups = {"initialize_fixtures"})
    public void getUsers() {
        BasicDBObject[] users = objectDownloader.getUsers();
        assertTrue(users.length > 0);
        db.getCollection("users").insert(users);
    }


    @Test(groups = {"initialize_fixtures"}, dependsOnMethods = "getIncidents")
    public void getLogEntries() {
        BasicDBObject[] logEntries = objectDownloader.getLogEntries(db);
        assertTrue(logEntries.length > 0);
        db.getCollection("log_entries").insert(logEntries);

    }


}