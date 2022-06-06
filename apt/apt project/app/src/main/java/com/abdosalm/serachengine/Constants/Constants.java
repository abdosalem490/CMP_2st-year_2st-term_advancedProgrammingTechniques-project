package com.abdosalm.serachengine.Constants;

public class Constants {

    // used by both indexer and crawler
    public static final String WEBPAGES_FIREBASE = "webpages";
    public static final String WEBPAGES_FIREBASE_ENGLISH_LINKS = "english";
    public static final String WEBPAGES_FIREBASE_ARABIC_LINKS = "arabic";
    public static final String WEBPAGES_FIREBASE_FLAGS = "flags";


    public static final String INTENT_KEYWORD = "KEYWORD";
    public static final String INTENT_LANGUAGE = "LANGUAGE";
    public static final String SPINNER_LANGUAGE_ARABIC = "العربية";
    public static final String SPINNER_LANGUAGE_ENGLISH = "English";
    public static final String INVERTED_INDEX_FIREBASE = "InvertedIndex";
    public static final String INVERTED_INDEX_FIREBASE_IDF = "IDF";
    public static final String INVERTED_INDEX_FIREBASE_DOCUMENTS = "documents";
    public static final String WEBPAGES_VISITED = "visited";
    public static final String NUM_OF_WEBPAGES = "numberOfLinks";
    public static final String STOP_WORDS_ENGLISH = "stopwords";
    public static final String CHILD_WEBPAGE_MODEL_ID = "id";
    public static final String CHILD_WEBPAGE_MODEL_RANK  = "rank";
    public static final int MAX_NUM_OF_LINKS_PER_WEBPAGE = 100;
    public static final int MAX_NUM_OF_WORDS_INDEXED_PER_LINK = 500;
    public static final String  WEBPAGES_LINKS = "Links";
    public static final String  WEBPAGES_IMPORTANT = "important";
    public static final String FLAGS_TO_SYNCHRONIZE = "sync";


    // specific to crawler
    public static final String WEBPAGES_FIREBASE_FINISHED_CRAWL_FLAG = "finishedCrawling";
    public static final int MAX_NUM_OF_VISITED_PAGE_ENGLISH = 80;
    public static final int MAX_NUM_OF_VISITED_PAGE_ARABIC = 120;
    public static final int MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_AR = 200;
    public static final int MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_EN = 400;

    // specific to indexer
    public static final String INVERTED_FILE = "invertedFile";
    public static final int NUM_OF_lINKS = 500;
    public static final String WEBPAGES_FIREBASE_FINISHED_INDEX_FLAG = "finishedIndexing";
    public static final String SYNC_LINK_NUM = "processNumber";


    // specific to flags
    public static final String FLAGS_NUM_OF_AR_LINKS = "numOfArabicLinks";
    public static final String FLAGS_NUM_OF_EN_LINKS = "numOfEnglishLinks";
    public static final String FLAGS_FINISHED_CRAWL = "finishedCrawling";
    public static final String FLAGS_FINISHED_INDEX = "finishedIndexing";
    public static final String FLAGS_NUM_OF_AR_WORDS = "numOfArabicWords";
    public static final String FLAGS_NUM_OF_EN_WORDS = "numOfEnglishWords";

    // the next ones is to decide the type of the heading
    public static final String HEADING_H1 = "h1";
    public static final String HEADING_H2 = "h2";
    public static final String HEADING_H3 = "h3";
    public static final String HEADING_H4 = "h4";
    public static final String HEADING_H5 = "h5";
    public static final String HEADING_H6 = "h6";
    public static final String HEADING_TITLE = "title";

    // needed by the intents
    public static final String INTENT_IS_ADMIN = "isAdmin";

    // specific to shared preference
    public static final String SHARED_PREFERENCE_NAME = "sharedPreference";
    public static final String HINTS_LIST = "HintsList";

    // needed by the recycler view adapter
    public static final int SEE_MORE_BUTTON = 0;
    public static final int SEARCH_BAR = 1;
    public static final int NORMAL_CELL = 2;
    public static final int SEE_MORE_RESULTS_NUM = 5;
    public static final int NUM_OF_WORDS_BEFORE_AFTER = 15;

    // needed for search
    public static final String SEARCH_WORD = "word";

    //specified to the ranker
    public static final double TITLE_WEIGHT = 1;
    public static final double H1_WEIGHT = 0.8;
    public static final double H2_WEIGHT = 0.6;
    public static final double H3_WEIGHT = 0.4;
    public static final double H4_WEIGHT = 0.3;
    public static final double H5_WEIGHT = 0.1;
    public static final double H6_WEIGHT = 0.1;

}
