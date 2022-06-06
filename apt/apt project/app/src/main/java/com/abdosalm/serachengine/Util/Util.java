package com.abdosalm.serachengine.Util;

import static com.abdosalm.serachengine.Constants.Constants.INVERTED_FILE;
import static com.abdosalm.serachengine.Constants.Constants.STOP_WORDS_ENGLISH;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_IMPORTANT;

import android.content.Context;
import android.util.Log;

import com.abdosalm.serachengine.Models.FlagsModel;
import com.abdosalm.serachengine.Models.InvertedIndexModel;
import com.abdosalm.serachengine.Models.InvertedWordModel;
import com.abdosalm.serachengine.Models.WebPageJointsModel;
import com.abdosalm.serachengine.Models.WordWithIndexModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Util {

    public static int rankWebPages(List<WebPageJointsModel> list){
        int old = 0;    // used as index to the old rank
        int newEle = 1; // used as index to the new rank
        int iterationStep = 1;
        float dampingFactor = 0.85f; //   0  < damping factor < 1

        // initialization
        for (WebPageJointsModel webPageJointsModel : list){
            webPageJointsModel.setRank((float) 1/list.size(),newEle);
        }

        // iterate to calculate
        while (iterationStep <= 2){

            // swap old and new
            int temp = old;
            old = newEle;
            newEle = temp;

            // --- here is the main algo ---
            for (int i = 0; i < list.size();i++){
                // see who is pointing to me
                int id = list.get(i).getId();
                float rank = 0;
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getLinks().contains(id))
                        rank += list.get(j).getRank(old) / list.get(j).getLinks().size();
                }
                list.get(i).setRank(rank,newEle);
            }

            // next iteration
            iterationStep++;
        }

        // original equation : (1 - dampingFactor) + dampingFactor * webPageJointsModel.getRank(newEle)

        // add the dumping factor
        for (WebPageJointsModel webPageJointsModel : list) {
            webPageJointsModel.setRank( dampingFactor * webPageJointsModel.getRank(newEle), newEle);
        }

        return newEle;
    }

    public static ArrayList<String> cleanEnglishTitle(String title, Context context){
        ArrayList<String> cleanedTitle = new ArrayList<>(Arrays.asList(title.split(" ")));

        ArrayList<String> stopWords = new ArrayList<>();
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

        //
        String regexWord = "^[a-zA-Z0-9]*$"; // ---> to get only english words
        String regexPunctuates = "[_$&+,:;=\\\\?@#|/'<>.^*()%!-\"]";   //  ---> to remove punctuates as , or $ or \ etc....
        String regexNum = "[\\d-]"; // ---> to remove numbers from the word

        ////////// HERE REMOVING PUNCTUATES //////////
        for (int j = 0; j < cleanedTitle.size(); j++) {
            String temp = cleanedTitle.get(j).replaceAll(regexPunctuates, "").toLowerCase().replaceAll(regexNum,"");
            if(temp.matches(regexWord) && !temp.isEmpty() && !stopWords.contains(temp)){
                try {
                    temp = Stemmer.Stemming(temp);
                    cleanedTitle.set(j,temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                cleanedTitle.remove(j);
                j--;
            }
        }

        return cleanedTitle;
    }
    public static ArrayList<String> cleanArabicTitle(String title){
        ArrayList<String> cleanedTitle = new ArrayList<>(Arrays.asList(title.split(" ")));
        for (int i = 0; i < cleanedTitle.size() ; i++)
            cleanedTitle.set(i,cleanedTitle.get(i).replaceAll("،","").replaceAll("؟","").replaceAll("[\"%.#$/\\[\\]]"," "));
        return cleanedTitle;
    }

    public static void calculate_TF_IDF_Score(List<InvertedWordModel> invertedWordModelList , int totalNumberOfDocuments){
        // ---- calculations of score , IDF , TF is done at client side
        // loop over the database to calculate TF(normalized) , IDF , score
        // TF(normalized) = frequency of word / number of words in document
        // IDF = log (total number of the documents in database / number of the documents related to this word)
        // score = IDF * TF(normalized)
        // if the word is mentioned in the title or the headings we should increase the score by a factor , this value is determined by the ranker

        // get total number of documents
        float IDF = 0;
        float TF = 0;
        for (InvertedWordModel invertedWordModel : invertedWordModelList){
            // calculate IDF
            IDF = (float) Math.log((float) totalNumberOfDocuments / invertedWordModel.getInvertedIndexModels().size());
            invertedWordModel.setIDF(IDF);
            // calculate TF normalized for each document
            for (Map.Entry<String, InvertedIndexModel> set : invertedWordModel.getInvertedIndexModels().entrySet()) {
                InvertedIndexModel invertedUrl = set.getValue();
                TF = (float) invertedUrl.getFrequency() / invertedUrl.getWebPagesModel().getTotalNumberOfWords();
                invertedUrl.setTF(TF);
                invertedUrl.setScore(TF * IDF);
            }
        }
    }
}
