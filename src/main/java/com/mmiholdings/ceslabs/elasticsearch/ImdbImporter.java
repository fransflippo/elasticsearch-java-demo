package com.mmiholdings.ceslabs.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class ImdbImporter {

    private final static int BATCH_SIZE = 10000;

    private static final Logger logger = LoggerFactory.getLogger(ImdbImporter.class);

    private final TransportClient client;

    public ImdbImporter() {
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch").build();
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getLoopbackAddress(), 9300));
    }

    public static void main(String[] argv) throws IOException {
        ImdbImporter imdbImporter = new ImdbImporter();
        imdbImporter.searchMovieByTitle("Avenger Infinity");
    }

    public void indexTitles() throws IOException {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        ObjectMapper objectMapper = new ObjectMapper();
        ImdbTitleIterator titleIterator = new ImdbTitleIterator("https://datasets.imdbws.com/title.basics.tsv.gz");
        int currentBatchSize = 0;
        while (titleIterator.hasNext()) {
            ImdbTitle imdbTitle = titleIterator.next();
            if (!"movie".equals(imdbTitle.getType())) continue;  // Otherwise we get too much data
            logger.debug("{}", imdbTitle);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            objectMapper.writeValue(bos, imdbTitle);
            bulkRequestBuilder.add(client.prepareIndex("imdb", "Title", imdbTitle.getId()).setSource(bos.toByteArray(), XContentType.JSON));
            if (++currentBatchSize == BATCH_SIZE) {
                logger.info("** Indexing {} titles...", currentBatchSize);
                // Flush
                bulkRequestBuilder.get(new TimeValue(30, TimeUnit.SECONDS));
                // Create a fresh BulkRequestBuilder for the next batch
                bulkRequestBuilder = client.prepareBulk();
                currentBatchSize = 0;
            }
        }
        if (currentBatchSize > 0) {
            logger.info("** Indexing {} titles...", currentBatchSize);
            bulkRequestBuilder.get(new TimeValue(30, TimeUnit.SECONDS));
        }
    }

    public void searchMovieByTitle(String title) {

        MatchQueryBuilder query = QueryBuilders.matchQuery("title.original", title);

        System.out.println("Query:");
        System.out.println(query);

        SearchResponse imdb = client.prepareSearch("imdb").setSize(50).setFrom(51).setQuery(query).get();
        for (SearchHit documentFields : imdb.getHits().getHits()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ImdbTitle imdbTitle = objectMapper.readValue(documentFields.getSourceAsString(), ImdbTitle.class);
                logger.info("{}", imdbTitle);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
