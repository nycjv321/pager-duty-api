package com.nycjv321.pagerdutytools.documents;

import com.nycjv321.pagerdutytools.SolrClientManager;
import org.apache.solr.common.SolrDocumentList;

import java.util.Map;

/**
 * Created by fedora on 11/23/15.
 */
public interface IndexedDocument extends Document {

    default void index() {
        SolrClientManager.save(toMap());
    }

    boolean isOld();

    default boolean isIndexed() {
        SolrDocumentList documents = SolrClientManager.search("id", getId().toString());
        return documents.size() == 1;
    }

    Map<String, Object> toMap();

}
