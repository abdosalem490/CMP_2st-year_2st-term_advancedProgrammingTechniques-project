package com.abdosalm.serachengine.Models;

import android.net.Uri;
import android.text.SpannableString;

public class ResultsModel {
    private String title;
    private SpannableString body;
    private Uri uri;
    private double finalScore;

    public ResultsModel() {
    }

    public ResultsModel(String title, SpannableString body, Uri uri,double finalScore) {
        this.title = title;
        this.body = body;
        this.uri = uri;
        this.finalScore = finalScore;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SpannableString getBody() {
        return body;
    }

    public void setBody(SpannableString body) {
        this.body = body;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    @Override
    public String toString() {
        return "ResultsModel{" +
                "title='" + title + '\'' +
                ", body=" + body +
                ", uri=" + uri +
                '}';
    }
}
