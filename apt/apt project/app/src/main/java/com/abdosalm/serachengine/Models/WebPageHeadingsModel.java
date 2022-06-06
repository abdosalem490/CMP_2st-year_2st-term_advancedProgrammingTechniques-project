package com.abdosalm.serachengine.Models;

import androidx.annotation.Nullable;

public class WebPageHeadingsModel {
    private String headingType;
    private String word;

    public WebPageHeadingsModel(String headingType, String word) {
        this.headingType = headingType;
        this.word = word;
    }

    public WebPageHeadingsModel() {
    }

    public String getHeadingType() {
        return headingType;
    }

    public void setHeadingType(String headingType) {
        this.headingType = headingType;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebPageHeadingsModel other = (WebPageHeadingsModel) obj;
        return word.equals(other.getWord());
    }

    @Override
    public String toString() {
        return "WebPageHeadingsModel{" +
                "headingType='" + headingType + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}
