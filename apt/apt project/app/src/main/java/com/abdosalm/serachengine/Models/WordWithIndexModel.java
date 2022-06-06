package com.abdosalm.serachengine.Models;

public class WordWithIndexModel {
    private String word;
    private int pos;

    public WordWithIndexModel(String word, int pos) {
        this.word = word;
        this.pos = pos;
    }

    public WordWithIndexModel() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "InvertedIndexModel{" +
                "word='" + word + '\'' +
                ", pos=" + pos +
                '}';
    }
}
