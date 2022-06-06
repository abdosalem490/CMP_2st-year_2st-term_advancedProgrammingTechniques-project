package com.abdosalm.serachengine.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebPageJointsModel {
    private String key;
    private ArrayList<String> title;
    private int id;
    private List<Integer> links;
    float[] rank;

    public WebPageJointsModel(String key, int id, List<Integer> links) {
        this.key = key;
        this.id = id;
        this.links = links;
        rank = new float[2];
    }

    public WebPageJointsModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getLinks() {
        return links;
    }

    public void setLinks(List<Integer> links) {
        this.links = links;
    }
    public void setRank(float num , int pos){
        rank[pos] = num;
    }

    public float getRank(int pos){
        return rank[pos];
    }

    public ArrayList<String> getTitle() {
        return title;
    }

    public void setTitle(ArrayList<String> title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "WebPageJointsModel{" +
                "key='" + key + '\'' +
                ", id=" + id +
                ", links=" + links +
                ", rank=" + Arrays.toString(rank) +
                '}';
    }
}