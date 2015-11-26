package com.nycjv321.pagerdutytools.documents;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.nycjv321.pagerdutytools.documents.updater.Updater;
import com.nycjv321.pagerdutytools.utils.MongoConnector;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.query.Query;

import java.util.function.Supplier;

import static com.nycjv321.reflectiveutilities.ReflectiveUtilities.getAnnotation;

/**
 * Created by fedora on 11/26/15.
 */
public abstract class QueryableDocument<T> implements Document {


    public void update(Supplier<BasicDBObject> objectSupplier, Updater updater) {
        BasicDBObject object = objectSupplier.get();
        updater.update(object);
        getCollection().update(
                queryThis().toQueryObject(), object);
    }

    public abstract Query<T> createQuery();

    public abstract Query<T> queryThis();

    public void delete() {
        MongoConnector.getMorphium().delete(queryThis());
    }

    public DBCollection getCollection() {
        return MongoConnector.getCollection(getAnnotation(getClass(), Entity.class).collectionName());
    }
}
