package com.nycjv321.pagerdutytools;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Map;

/**
 * Created by fedora on 11/23/15.
 */
public class SolrClientManager {
    private static SolrClient solrClient;

    static {
        solrClient = new CloudSolrClient(Configuration.getIndexerLocation());
        ((CloudSolrClient) solrClient).setDefaultCollection(Configuration.getIndexCollection());
    }

    private static SolrClient getClient() {
        return solrClient;
    }

    public static void save(Map<String, Object> map) {
        SolrInputDocument solrInputFields = new SolrInputDocument();

        for (Map.Entry<String, Object> next : map.entrySet()) {
            solrInputFields.addField(next.getKey(), next.getValue());
        }
        SolrClient client = getClient();
        try {
            client.add(solrInputFields);
            client.commit();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void close() throws IOException {
        solrClient.close();
    }

    public static SolrDocumentList search(String field, Object value) {
        SolrQuery parameters = new SolrQuery();
        parameters.set("q", String.format("%s:%s", field, value));
        try {
            return getClient().query(parameters).getResults();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
