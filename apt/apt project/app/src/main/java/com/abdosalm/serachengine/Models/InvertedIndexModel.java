package com.abdosalm.serachengine.Models;

import static com.abdosalm.serachengine.Constants.Constants.HEADING_H1;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H2;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H3;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H4;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H5;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H6;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_TITLE;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InvertedIndexModel {
    private WebPagesModel webPagesModel;
    private int frequency;
    private List<Integer> indexes;
    private String word;
    // TF will be for every document while IDF will be for every word
    private float TF;
    private float score;
    // these are variables for headings : h1, h2, h3, h4, h5, h6
    private boolean h1;
    private boolean h2;
    private boolean h3;
    private boolean h4;
    private boolean h5;
    private boolean h6;
    private boolean isTitle;

    public InvertedIndexModel(WebPagesModel webPagesModel, String word,int index) {
        this();
        this.webPagesModel = webPagesModel;
        this.word = word;
        indexes.add(index);
    }

    public InvertedIndexModel() {
        indexes = new ArrayList<>();
        frequency = 1;
        score = 0;
        TF = 0;
        h1 = false;
        h2 = false;
        h3 = false;
        h4 = false;
        h5 = false;
        h6 = false;
        isTitle = false;
    }

    public WebPagesModel getWebPagesModel() {
        return webPagesModel;
    }

    public void setWebPagesModel(WebPagesModel webPagesModel) {
        this.webPagesModel = webPagesModel;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Integer> indexes) {
        this.indexes = indexes;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public float getTF() {
        return TF;
    }

    public void setTF(float TF) {
        this.TF = TF;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
    public void addIndex(int index){
        indexes.add(index);
        frequency++;
    }

    public boolean isH1() {
        return h1;
    }

    public void setH1(boolean h1) {
        this.h1 = h1;
    }

    public boolean isH2() {
        return h2;
    }

    public void setH2(boolean h2) {
        this.h2 = h2;
    }

    public boolean isH3() {
        return h3;
    }

    public void setH3(boolean h3) {
        this.h3 = h3;
    }

    public boolean isH4() {
        return h4;
    }

    public void setH4(boolean h4) {
        this.h4 = h4;
    }

    public boolean isH5() {
        return h5;
    }

    public void setH5(boolean h5) {
        this.h5 = h5;
    }

    public boolean isH6() {
        return h6;
    }

    public void setH6(boolean h6) {
        this.h6 = h6;
    }

    public boolean isTitle() {
        return isTitle;
    }

    public void setTitle(boolean title) {
        isTitle = title;
    }

    public void setHeadings(String headingType){
        if (headingType.equals(HEADING_H1))
            h1 = true;
        else if (headingType.equals(HEADING_H2))
            h2 = true;
        else if (headingType.equals(HEADING_H3))
            h3 = true;
        else if (headingType.equals(HEADING_H4))
            h4 = true;
        else if (headingType.equals(HEADING_H5))
            h5 = true;
        else if (headingType.equals(HEADING_H6))
            h6 = true;
        else if (headingType.equals(HEADING_TITLE))
            isTitle = true;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InvertedIndexModel other = (InvertedIndexModel) obj;
        return word.equals(other.getWord());
    }

    @Override
    public String toString() {
        return "InvertedIndexModel{" +
                "webPagesModel=" + webPagesModel +
                ", frequency=" + frequency +
                ", indexes=" + indexes +
                ", word='" + word + '\'' +
                ", TF=" + TF +
                ", score=" + score +
                ", h1=" + h1 +
                ", h2=" + h2 +
                ", h3=" + h3 +
                ", h4=" + h4 +
                ", h5=" + h5 +
                ", h6=" + h6 +
                ", isTitle=" + isTitle +
                '}';
    }
}
