package com.mmiholdings.ceslabs.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * title.basics.tsv.gz - Contains the following information for titles:
 * - tconst (string) - alphanumeric unique identifier of the title
 * - titleType (string) – the type/format of the title (e.g. movie, short, tvseries, tvepisode, video, etc)
 * - primaryTitle (string) – the more popular title / the title used by the filmmakers on promotional materials at the point of release
 * - originalTitle (string) - original title, in the original language
 * - isAdult (boolean) - 0: non-adult title; 1: adult title.
 * - startYear (YYYY) – represents the release year of a title. In the case of TV Series, it is the series start year.
 * - endYear (YYYY) – TV Series end year. ‘\N’ for all other title types
 * - runtimeMinutes – primary runtime of the title, in minutes
 * - genres (string array) – includes up to three genres associated with the title
 */
public class ImdbTitle {
    private String id;
    private String type;
    private Title title;
    private boolean adult;
    private Integer startYear;
    private Integer endYear;
    private Integer runtimeMinutes;
    private List<String> genres = new ArrayList<String>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Title getTitle() {
        return title;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setGenres(List<String> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
    }

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    public String toString() {
        return title.getOriginal() + " (" + startYear + ")";
    }

    public static class Title {
        private final String primary;
        private final String original;

        public Title(String primary, String original) {
            this.primary = primary;
            this.original = original;
        }

        public String getPrimary() {
            return primary;
        }

        public String getOriginal() {
            return original;
        }
    }
}
