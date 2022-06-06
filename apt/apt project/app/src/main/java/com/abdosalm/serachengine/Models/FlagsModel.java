package com.abdosalm.serachengine.Models;

public class FlagsModel {
    private int numOfEnglishLinks;
    private int numOfArabicLinks;
    private int numOfEnglishWords;
    private int numOfArabicWords;
    private boolean finishedCrawling;
    private boolean finishedIndexing;
    private int arabicProcessNumber;
    private int englishProcessNumber;
    private boolean finishedIndex;
    private String language;

    public FlagsModel(int numOfEnglishLinks, int numOfArabicLinks, int numOfEnglishWords, int numOfArabicWords, boolean finishedCrawling, boolean finishedIndexing, int arabicProcessNumber, int englishProcessNumber, boolean finishedIndex, String language) {
        this.numOfEnglishLinks = numOfEnglishLinks;
        this.numOfArabicLinks = numOfArabicLinks;
        this.numOfEnglishWords = numOfEnglishWords;
        this.numOfArabicWords = numOfArabicWords;
        this.finishedCrawling = finishedCrawling;
        this.finishedIndexing = finishedIndexing;
        this.arabicProcessNumber = arabicProcessNumber;
        this.englishProcessNumber = englishProcessNumber;
        this.finishedIndex = finishedIndex;
        this.language = language;
    }

    public FlagsModel() {
    }

    public int getNumOfEnglishLinks() {
        return numOfEnglishLinks;
    }

    public void setNumOfEnglishLinks(int numOfEnglishLinks) {
        this.numOfEnglishLinks = numOfEnglishLinks;
    }

    public int getNumOfArabicLinks() {
        return numOfArabicLinks;
    }

    public void setNumOfArabicLinks(int numOfArabicLinks) {
        this.numOfArabicLinks = numOfArabicLinks;
    }

    public int getNumOfEnglishWords() {
        return numOfEnglishWords;
    }

    public void setNumOfEnglishWords(int numOfEnglishWords) {
        this.numOfEnglishWords = numOfEnglishWords;
    }

    public int getNumOfArabicWords() {
        return numOfArabicWords;
    }

    public void setNumOfArabicWords(int numOfArabicWords) {
        this.numOfArabicWords = numOfArabicWords;
    }

    public boolean isFinishedCrawling() {
        return finishedCrawling;
    }

    public void setFinishedCrawling(boolean finishedCrawling) {
        this.finishedCrawling = finishedCrawling;
    }

    public boolean isFinishedIndexing() {
        return finishedIndexing;
    }

    public void setFinishedIndexing(boolean finishedIndexing) {
        this.finishedIndexing = finishedIndexing;
    }


    public boolean isFinishedIndex() {
        return finishedIndex;
    }

    public void setFinishedIndex(boolean finishedIndex) {
        this.finishedIndex = finishedIndex;
    }

    public int getArabicProcessNumber() {
        return arabicProcessNumber;
    }

    public void setArabicProcessNumber(int arabicProcessNumber) {
        this.arabicProcessNumber = arabicProcessNumber;
    }

    public int getEnglishProcessNumber() {
        return englishProcessNumber;
    }

    public void setEnglishProcessNumber(int englishProcessNumber) {
        this.englishProcessNumber = englishProcessNumber;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

