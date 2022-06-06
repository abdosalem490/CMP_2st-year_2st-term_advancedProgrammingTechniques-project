package com.abdosalm.serachengine.Models;

import java.util.HashMap;
import java.util.Map;

public class InvertedWordModel {
    private float IDF;
    private String word;
    private Map<String,InvertedIndexModel> invertedIndexModels;

    public InvertedWordModel(float IDF,String word) {
        super();
        this.IDF = IDF;
        this.word = word;
        invertedIndexModels = new HashMap<>();
    }

    public InvertedWordModel() {
        this.IDF = 0;
        this.invertedIndexModels = new HashMap<>();
    }

    public float getIDF() {
        return IDF;
    }

    public void setIDF(float IDF) {
        this.IDF = IDF;
    }

    public Map<String, InvertedIndexModel> getInvertedIndexModels() {
        return invertedIndexModels;
    }

    public void setInvertedIndexModels(Map<String, InvertedIndexModel> invertedIndexModels) {
        this.invertedIndexModels = invertedIndexModels;
    }
    public void addUrl(InvertedIndexModel invertedIndexModel){
        invertedIndexModels.put(invertedIndexModel.getWebPagesModel().getKey(),invertedIndexModel);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
