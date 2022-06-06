package com.abdosalm.serachengine.Models;

import androidx.annotation.Nullable;

import java.util.Objects;

public class WebPagesModel {
    private String uri;
    private String title;
    private boolean isVisited;
    private int totalNumberOfWords;
    private String key;
    private float rank;
    private int id;

    public WebPagesModel(String uri, String title, boolean isVisited, int totalNumberOfWords) {
        this.uri = uri;
        this.title = title;
        this.isVisited = isVisited;
        this.totalNumberOfWords = totalNumberOfWords;
        this.id = 0;
        this.rank = 0;
        if (uri.length() > 750)
            uri = uri.substring(0,750);
        key = (uri.toLowerCase()).replace('.','_').replace('#','_').replace('$','_').replace('[','_').replace(']','_').replace('/','_');
    }

    public int getTotalNumberOfWords() {
        return totalNumberOfWords;
    }

    public void setTotalNumberOfWords(int totalNumberOfWords) {
        this.totalNumberOfWords = totalNumberOfWords;
    }

    public WebPagesModel() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebPagesModel other = (WebPagesModel) obj;
        return uri.equals(other.getUri());
    }

    @Override
    public String toString() {
        return "WebPagesModel{" +
                "uri='" + uri + '\'' +
                ", title='" + title + '\'' +
                ", isVisited=" + isVisited +
                ", totalNumberOfWords=" + totalNumberOfWords +
                '}';
    }


}
