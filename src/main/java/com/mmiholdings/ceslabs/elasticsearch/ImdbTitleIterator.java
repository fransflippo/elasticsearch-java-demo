package com.mmiholdings.ceslabs.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

public class ImdbTitleIterator implements Iterator<ImdbTitle> {

    private static final Logger logger = LoggerFactory.getLogger(ImdbTitleIterator.class);

    private final BufferedReader reader;

    private transient ImdbTitle title;

    public ImdbTitleIterator(String url) throws IOException {
        logger.info("Opening {}...", url);
        InputStream is = new URL(url).openStream();
        GZIPInputStream gzis = new GZIPInputStream(is);
        this.reader = new BufferedReader(new InputStreamReader(gzis));
        reader.readLine();  // First line contains headers
        fetchNextTitle();
    }

    @Override
    public boolean hasNext() {
        return title != null;
    }

    @Override
    public ImdbTitle next() {
        if (title == null) {
            throw new NoSuchElementException();
        }
        ImdbTitle result = title;
        try {
            fetchNextTitle();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return result;
    }

    private synchronized void fetchNextTitle() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            this.title = null;
            reader.close();
        } else {
            String[] cols = line.split("\t");
            // From http://www.imdb.com/interfaces/ :
            // * - tconst (string) - alphanumeric unique identifier of the title
            // * - titleType (string) – the type/format of the title (e.g. movie, short, tvseries, tvepisode, video, etc)
            // * - primaryTitle (string) – the more popular title / the title used by the filmmakers on promotional materials at the point of release
            // * - originalTitle (string) - original title, in the original language
            // * - isAdult (boolean) - 0: non-adult title; 1: adult title.
            // * - startYear (YYYY) – represents the release year of a title. In the case of TV Series, it is the series start year.
            // * - endYear (YYYY) – TV Series end year. ‘\N’ for all other title types
            // * - runtimeMinutes – primary runtime of the title, in minutes
            // * - genres (string array) – includes up to three genres associated with the title
            ImdbTitle title = new ImdbTitle();
            title.setId(cols[0]);
            title.setType(cols[1]);
            title.setTitle(new ImdbTitle.Title(cols[2], cols[3]));
            title.setAdult(toBoolean(cols[4]));
            title.setStartYear(toNumber(cols[5]));
            title.setEndYear(toNumber(cols[6]));
            title.setRuntimeMinutes(toNumber(cols[7]));
            title.setGenres(toList(cols[8]));
            this.title = title;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private Integer toNumber(String col) {
        if (col.equals("\\N")) return null;
        return Integer.valueOf(col);
    }

    private boolean toBoolean(String col) {
        return col.equals("1");
    }

    private List<String> toList(String col) {
        return Arrays.asList(col.split(","));
    }

}
