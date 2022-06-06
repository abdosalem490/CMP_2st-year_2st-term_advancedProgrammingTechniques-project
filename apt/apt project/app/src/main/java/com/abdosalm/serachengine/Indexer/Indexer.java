package com.abdosalm.serachengine.Indexer;

import static com.abdosalm.serachengine.Constants.Constants.FLAGS_FINISHED_INDEX;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H1;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H2;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H3;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H4;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H5;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_H6;
import static com.abdosalm.serachengine.Constants.Constants.HEADING_TITLE;
import static com.abdosalm.serachengine.Constants.Constants.INVERTED_INDEX_FIREBASE;
import static com.abdosalm.serachengine.Constants.Constants.NUM_OF_lINKS;
import static com.abdosalm.serachengine.Constants.Constants.STOP_WORDS_ENGLISH;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ARABIC_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FINISHED_INDEX_FLAG;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_IMPORTANT;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_LINKS;

import android.content.Context;
import android.util.Log;

import com.abdosalm.serachengine.Models.FlagsModel;
import com.abdosalm.serachengine.Models.InvertedIndexModel;
import com.abdosalm.serachengine.Models.InvertedWordModel;
import com.abdosalm.serachengine.Models.WebPageHeadingsModel;
import com.abdosalm.serachengine.Models.WordWithIndexModel;
import com.abdosalm.serachengine.Models.WebPagesModel;
import com.abdosalm.serachengine.Util.Stemmer;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Indexer implements Runnable{
    private final int numOfThreads;
    private final List<String> stopWords;
    private final List<WebPagesModel> webPagesUrls;
    private final List<Thread>threads;
    private String language;
    private final Map<String,InvertedWordModel> invertedToBeUploaded;
    private final DocumentReference syncFlags;
    private boolean finished = false;


    public Indexer(int numOfThreads, Context context){
        this.numOfThreads = numOfThreads;
        stopWords = new ArrayList<>();
        webPagesUrls = new ArrayList<>();
        threads = new ArrayList<>();
        invertedToBeUploaded = new HashMap<>();

        syncFlags = FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS);

        // reading Stop words from file
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(STOP_WORDS_ENGLISH), StandardCharsets.UTF_8));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                stopWords.add(mLine);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void index(){
        indexByLanguage();
    }


    public void indexByLanguage(){
        // see which part of data should i take
        FirebaseFirestore.getInstance().runTransaction((Transaction.Function<Void>) transaction -> {
            FlagsModel flags = transaction.get(syncFlags).toObject(FlagsModel.class);
            if (flags != null){

                // get the language from the database
                language = flags.getLanguage();

                // get max num of links to visit according to the language
                int start = 0;
                if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                    start = flags.getEnglishProcessNumber();
                else
                    start = flags.getArabicProcessNumber();

                int end = start + NUM_OF_lINKS;

                // see if we reached the limit
                int max = flags.getNumOfArabicLinks();
                if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                    max = flags.getNumOfEnglishLinks();

                if (end > max){
                    // reset the value and flip the language
                    if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS)){
                        flags.setEnglishProcessNumber(0);
                        flags.setLanguage(WEBPAGES_FIREBASE_ARABIC_LINKS);
                    }
                    else{
                        flags.setArabicProcessNumber(0);
                        flags.setLanguage(WEBPAGES_FIREBASE_ENGLISH_LINKS);
                    }

                }else{
                    // increment that value
                    if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                        flags.setEnglishProcessNumber(end);
                    else
                        flags.setArabicProcessNumber(end);
                }
                transaction.set(syncFlags,flags);


                int finalStart = start;
                FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                   //     Log.d("TAG", "indexByLanguage: " + finalStart + " --- " + end);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WebPagesModel webPagesModel = document.toObject(WebPagesModel.class);
                            if (webPagesModel.getId() < end && webPagesModel.getId()  >= finalStart){
                                webPagesUrls.add(webPagesModel);
                            }
                        }

                        operateOnLinks();
                    }
                });

            }
            return null;
        });

    }

    private void operateOnLinks(){

        for (int i = 0; i < numOfThreads; i++) {
            // Log.d("TAG", "operateOnLinks: " + i);
            Thread t = new Thread(this);
            t.setName(String.valueOf(i));
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
      //  Log.d("invertedFile", "operateOnLinks: " + invertedToBeUploaded.size());

        // upload hash map tp database
        uploadHashMapToWeb();



    }

    private void uploadHashMapToWeb(){
        // rise the index flag to let other process know that you are uploading
        syncFlags.addSnapshotListener((value, error) -> {
            if (value != null && !finished){
                FlagsModel flagsModel = value.toObject(FlagsModel.class);
                if (flagsModel != null){
                    if (flagsModel.isFinishedIndex()){
                        // run a transaction to take control and upload
                        FirebaseFirestore.getInstance().runTransaction((Transaction.Function<Void>) transaction -> {
                            FlagsModel flags = transaction.get(syncFlags).toObject(FlagsModel.class);
                            if (flags != null && flags.isFinishedIndex()){
                                flags.setFinishedIndex(false);

                                // calculate number of words
                                flagsModel.setNumOfEnglishWords(flags.getNumOfArabicWords()+invertedToBeUploaded.size());
                                if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                                    flagsModel.setNumOfEnglishWords(flags.getNumOfEnglishWords()+invertedToBeUploaded.size());

                                transaction.set(syncFlags,flags);

                   //             Log.d("TAG", "i take");

                                // begin uploading
                                int i = 0;
                                for (Map.Entry<String,InvertedWordModel> modelEntry : invertedToBeUploaded.entrySet()){
                                    if (!modelEntry.getKey().equals("") && !modelEntry.getKey().equals(" ")){
                                       Log.d("TAG", "uploadHashMapToWeb: " + modelEntry.getKey() + " ---> " + modelEntry.getValue().getInvertedIndexModels().size());

                                        FirebaseFirestore.getInstance().collection(language+INVERTED_INDEX_FIREBASE).document(modelEntry.getKey()).set(modelEntry.getValue(), SetOptions.merge());
                                        try {
                                           Log.d("upload", i + " out of " + invertedToBeUploaded.size() + " -> lang = " + language);
                                            i++;
                                            if (modelEntry.getValue().getInvertedIndexModels().size() > 20)
                                                Thread.sleep(1000);
                                            else
                                                Thread.sleep(50);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                                // reset the value
                                FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(FLAGS_FINISHED_INDEX,true);
                                FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(WEBPAGES_FIREBASE_FINISHED_INDEX_FLAG,true);
                                finished = true;
                            }
                            return null;
                        });
                    }
                }
            }
        });

    }

    @Override
    public void run() {
        int ID = Integer.parseInt(Thread.currentThread().getName());
        int start = (int) (((float) ID / numOfThreads) * webPagesUrls.size());
        int end = (int) (((float) (ID + 1) / numOfThreads) * webPagesUrls.size());

        indexWebPages(start , end);
    }
    private void cleanArabicWords(List<WordWithIndexModel> list){
        for (int j = 0; j < list.size(); j++) {
            list.get(j).setWord(list.get(j).getWord().replaceAll("،","").replaceAll("؟","").replaceAll("[\"%.#$/\\[\\]]"," "));
        }
    }

    private void cleanArabicHeadings(List<String> heading){
        for (int j = 0; j < heading.size(); j++) {
            heading.set(j,heading.get(j).replaceAll("،","").replaceAll("؟","").replaceAll("[\"%.#$/\\[\\]]"," "));
        }
    }

    private void cleanEnglishWords(List<WordWithIndexModel> list){
        String regexWord = "^[a-zA-Z0-9]*$"; // ---> to get only english words
        String regexPunctuates = "[_$&+,:;=\\\\?@#|/'<>.^*()%!-\"]";   //  ---> to remove punctuates as , or $ or \ etc....
        String regexNum = "[\\d-]"; // ---> to remove numbers from the word

        ////////// HERE REMOVING PUNCTUATES //////////
        for (int j = 0; j < list.size(); j++) {
            String temp = list.get(j).getWord().replaceAll(regexPunctuates, "").toLowerCase().replaceAll(regexNum,"");
            if(temp.matches(regexWord) && !temp.isEmpty() && !stopWords.contains(temp)){
                try {
                    temp = Stemmer.Stemming(temp);
                    list.get(j).setWord(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                list.remove(j);
                j--;
            }
        }
    }

    private void cleanEnglishHeadings(List<String> heading){
        String regexWord = "^[a-zA-Z0-9]*$"; // ---> to get only english words
        String regexPunctuates = "[_$&+,:;=\\\\?@#|/'<>.^*()%!-\"]";   //  ---> to remove punctuates as , or $ or \ etc....
        String regexNum = "[\\d-]"; // ---> to remove numbers from the word

        for (int j = 0; j < heading.size(); j++) {
            String temp = heading.get(j).replaceAll(regexPunctuates, "").toLowerCase().replaceAll(regexNum,"");
            if(temp.matches(regexWord) && !temp.isEmpty() && !stopWords.contains(temp)){
                try {
                    temp = Stemmer.Stemming(temp);
                    heading.set(j,temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                heading.remove(j);
                j--;
            }
        }
    }
    private void StoreHeadingsInArray(List<WebPageHeadingsModel> headings,Document document){
        List<String> headingsDummy;

        // get title and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.title().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            //Log.d("arab", "StoreHeadingsInArray: " + s);
            headings.add(new WebPageHeadingsModel(HEADING_TITLE,s));
        }

        // get h1 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H1).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H1,s));
        }

        // get h2 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H2).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H2,s));
        }

        // get h3 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H3).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H3,s));
        }

        // get h4 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H4).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H4,s));
        }

        // get h5 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H5).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H5,s));
        }

        // get h6 and clean the values in it
        headingsDummy = new ArrayList<>(Arrays.asList(document.select(HEADING_H6).text().split(" ")));
        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
            cleanEnglishHeadings(headingsDummy);
        else
            cleanArabicHeadings(headingsDummy);
        for (String s : headingsDummy){
            headings.add(new WebPageHeadingsModel(HEADING_H6,s));
        }
    }

    private void indexWebPages(int start , int end){
        // create array of words where : word -> {document1,frequency,size,{indexes of the word}} , {document1,frequency,size,{indexes of the word}}, etc....
        List<WordWithIndexModel> documentWords = new ArrayList<>();
        List<InvertedIndexModel> invertedFile = new ArrayList<>();
        List<WebPageHeadingsModel> headings = new ArrayList<>();

        for (int k = start; k < end ; k++ ) //WebPagesModel webPagesModel : webPagesUrls){
        {
            try {
                WebPagesModel webPagesModel = webPagesUrls.get(k);

                // get the words in the link to clean them
                Document doc = Jsoup.connect(webPagesModel.getUri()).get();
                String[] dummy = doc.body().text().split(" ");
                // storing each word with its index in the document
                for (int i = 0; i < dummy.length; i++) {
                    documentWords.add(new WordWithIndexModel(dummy[i], i));
                }

                // only stem and remove words if the word is english
                if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                    cleanEnglishWords(documentWords);
                else
                    cleanArabicWords(documentWords);

                // get the headings of that document
                StoreHeadingsInArray(headings, doc);


                // get the frequency of each word
                for (WordWithIndexModel wordWithIndexModel : documentWords) {
                    InvertedIndexModel temp = new InvertedIndexModel(webPagesModel, wordWithIndexModel.getWord(), wordWithIndexModel.getPos());
                    int index = invertedFile.indexOf(temp);
                    if (index == -1) {
                        invertedFile.add(temp);
                    } else {
                        invertedFile.get(index).addIndex(wordWithIndexModel.getPos());
                    }
                }

                // see what is the words which is in headings and which is not
                for (InvertedIndexModel invertedIndexModel : invertedFile) {
                    invertedIndexModel.getWebPagesModel().setTotalNumberOfWords(documentWords.size());
                    String word = invertedIndexModel.getWord();
                    for (WebPageHeadingsModel webPageHeadingsModel : headings) {
                        if (word.equals(webPageHeadingsModel.getWord()))
                            invertedIndexModel.setHeadings(webPageHeadingsModel.getHeadingType());
                    }
                }
                Log.d("TAG", "indexWebPages: " + invertedFile.size() + " ID == " + invertedFile.get(0).getWebPagesModel().getId());

                //insert into the database
                insertIntoHashmap(invertedFile);

                documentWords.clear();
                invertedFile.clear();
                headings.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void insertIntoHashmap(List<InvertedIndexModel> documentWords){
        for (int i = 0; i < documentWords.size(); i++){
            InvertedIndexModel invertedIndexModel = documentWords.get(i);
            if (invertedToBeUploaded.containsKey(invertedIndexModel.getWord())){
                Objects.requireNonNull(invertedToBeUploaded.get(invertedIndexModel.getWord())).addUrl(invertedIndexModel);
            }else{
                InvertedWordModel invertedWordModel = new InvertedWordModel(0,invertedIndexModel.getWord());
                invertedWordModel.addUrl(invertedIndexModel);
                invertedToBeUploaded.put(invertedIndexModel.getWord(),invertedWordModel);
            }
        }
    }
}