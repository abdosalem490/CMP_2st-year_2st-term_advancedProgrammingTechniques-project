package com.abdosalm.serachengine.Crawler;


import static com.abdosalm.serachengine.Constants.Constants.CHILD_WEBPAGE_MODEL_ID;
import static com.abdosalm.serachengine.Constants.Constants.CHILD_WEBPAGE_MODEL_RANK;
import static com.abdosalm.serachengine.Constants.Constants.FLAGS_FINISHED_CRAWL;
import static com.abdosalm.serachengine.Constants.Constants.FLAGS_NUM_OF_AR_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.FLAGS_NUM_OF_EN_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_AR;
import static com.abdosalm.serachengine.Constants.Constants.MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_EN;
import static com.abdosalm.serachengine.Constants.Constants.MAX_NUM_OF_VISITED_PAGE_ARABIC;
import static com.abdosalm.serachengine.Constants.Constants.MAX_NUM_OF_VISITED_PAGE_ENGLISH;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ARABIC_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FINISHED_CRAWL_FLAG;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_IMPORTANT;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_VISITED;

import android.content.Context;
import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;

import com.abdosalm.serachengine.Models.FlagsModel;
import com.abdosalm.serachengine.Models.WebPageJointsModel;
import com.abdosalm.serachengine.Models.WebPagesModel;
import com.abdosalm.serachengine.Util.Util;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Crawler implements Runnable {
    private final List<WebPagesModel> webPages;
    private final int numOfThreads;
    private final List<Thread> threads;
    private String language;
    private int MAX_NUM_OF_VISITED_PAGE;
    private final Context context;

    public Crawler(int numOfThreads,Context context) {
        this.numOfThreads = numOfThreads;
        webPages = new ArrayList<>();
        threads = new ArrayList<>();
        this.context = context;
    }

    public void crawl() {
        // get the language to crawl for from database
        FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    FlagsModel flagsModel = task.getResult().toObject(FlagsModel.class);
                    if (flagsModel != null){
                        language = flagsModel.getLanguage();
                        if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS)){
                            flagsModel.setLanguage(WEBPAGES_FIREBASE_ARABIC_LINKS);
                            MAX_NUM_OF_VISITED_PAGE = MAX_NUM_OF_VISITED_PAGE_ENGLISH;
                        }else{
                            flagsModel.setLanguage(WEBPAGES_FIREBASE_ENGLISH_LINKS);
                            MAX_NUM_OF_VISITED_PAGE = MAX_NUM_OF_VISITED_PAGE_ARABIC;
                        }
                        FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).set(flagsModel);
                        crawlByLanguage();
                    }
                }
            }
        });
    }

    private void crawlByLanguage() {
        // get the links of the web pages on firebase add add them to the arraylist
        FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    WebPagesModel webPagesModel = document.toObject(WebPagesModel.class);

                    // if the web page is visited before we will store it to reset its value after crawling but we will not visit it
                    webPages.add(webPagesModel);
                }
                operateOnLinks();
            }
        });

    }

    private void operateOnLinks() {
        for (int i = 0; i < numOfThreads; i++) {
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

        resetAllNodesVisited();
        FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(WEBPAGES_FIREBASE_FINISHED_CRAWL_FLAG,true);

    }

    private synchronized void resetAllNodesVisited() {
        for (WebPagesModel webPagesModel : webPages) {
            FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).document(webPagesModel.getKey()).update(WEBPAGES_VISITED,false);
        }
        webPages.clear();

        // store number of links we gripped and rank all the pages
        FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //FirebaseFirestore.getInstance().collection(WEBPAGES_FIREBASE_FLAGS).document(NUM_OF_WEBPAGES+language).set(task.getResult().size());
                String flagType = FLAGS_NUM_OF_AR_LINKS;
                if (language.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                    flagType = FLAGS_NUM_OF_EN_LINKS;

                FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(flagType,task.getResult().size());
                int id = 0;
                // assign a unique integer for each one of them
                for (QueryDocumentSnapshot document : task.getResult()) {
                    WebPagesModel webPagesModel = document.toObject(WebPagesModel.class);
                    FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).document(webPagesModel.getKey()).update(CHILD_WEBPAGE_MODEL_ID,id++);
                }

                // loop to clean the titles
                List<WebPageJointsModel> rankedWebPages = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    WebPagesModel webPagesModel = document.toObject(WebPagesModel.class);

                    if (webPagesModel.getTotalNumberOfWords() != 0){
                        // attributes of model needed by the rank
                        String title = webPagesModel.getTitle();
                        int childID = webPagesModel.getId();
                        String key = webPagesModel.getKey();
                        List<Integer> linkedWebPage = new ArrayList<>();

                        // clean words of the title
                        WebPageJointsModel webPageJointsModel = new WebPageJointsModel(key,childID,linkedWebPage);
                        webPageJointsModel.setTitle(Util.cleanEnglishTitle(title,context));
                        rankedWebPages.add(webPageJointsModel);
                    }
                }

                // loop to find relation between links
                int ComparisonNumOfCommonWords = 1;
                if (language.equals(WEBPAGES_FIREBASE_ARABIC_LINKS))
                    ComparisonNumOfCommonWords = 0;

                for (WebPageJointsModel webPageJointsModelCurrent : rankedWebPages){
                    //Log.d("TAG", "onComplete: " + webPageJointsModelCurrent.getId());
                    for (WebPageJointsModel webPageJointsModelIterator : rankedWebPages){
                        if (webPageJointsModelCurrent != webPageJointsModelIterator){
                            ArrayList<String> dummy = new ArrayList<>(webPageJointsModelCurrent.getTitle());
                            // check if there is a common word between the titles , if there is then there is a link
                            dummy.retainAll(webPageJointsModelIterator.getTitle());
                            if (dummy.size() > ComparisonNumOfCommonWords){
                                // then there is common words
                                webPageJointsModelCurrent.getLinks().add(webPageJointsModelIterator.getId());
                            }
                        }
                    }
                }
                int column = Util.rankWebPages(rankedWebPages);
                for (WebPageJointsModel webPageJointsModel : rankedWebPages){
                    if(webPageJointsModel.getKey() != null){
                        FirebaseFirestore.getInstance().collection((language+WEBPAGES_LINKS)).document(webPageJointsModel.getKey()).update(CHILD_WEBPAGE_MODEL_RANK,webPageJointsModel.getRank(column) * 100000);
                     }
                 }

            }

        });
    }

    @Override
    public void run() {
        int ID = Integer.parseInt(Thread.currentThread().getName());
        int start = (int) (((float) ID / numOfThreads) * webPages.size());
        int end = (int) (((float) (ID + 1) / numOfThreads) * webPages.size());
        List<WebPagesModel> list = new ArrayList<>();
        AtomicInteger numOfVisitedWebSites = new AtomicInteger(0);
        for (int i = start; i < end; i++) {
            list.add(webPages.get(i));
        }
        processList(list,numOfVisitedWebSites);
    }

    private void processList(List<WebPagesModel> list,AtomicInteger numOfVisitedWebSites) {
        int i = 0;

        while (true) {
            int size = list.size();
            if(i == size)
                break;
            for (; i < size; i++) {
                if (!list.get(i).isVisited()) {
                    getLinksInaWebPage(list, list.get(i),numOfVisitedWebSites);
                }
            }
        }
    }

    private void getLinksInaWebPage(List<WebPagesModel> webPagesModels, WebPagesModel parentWebPage,AtomicInteger numOfVisitedWebSites) {
        if(numOfVisitedWebSites.get() < (MAX_NUM_OF_VISITED_PAGE / numOfThreads)) {
            try {
                Document doc = Jsoup.connect(parentWebPage.getUri()).get();
                Elements links = doc.select("a[href]");
                numOfVisitedWebSites.incrementAndGet();
                parentWebPage.setVisited(true);

                FirebaseFirestore.getInstance().collection(language+WEBPAGES_LINKS).document(parentWebPage.getKey()).update(WEBPAGES_VISITED,true);

                // add a limit to number of links per webpage according to the language
                int MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE = MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_EN;
                if (language.equals(WEBPAGES_FIREBASE_ARABIC_LINKS))
                    MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE = MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE_AR;

                int counter = 0;
                for (Element link : links) {
                    String host = Uri.parse(link.attr("abs:href")).getHost();
                    String uri = link.attr("abs:href");

                    // using authority instead of host will make no such a big difference
                    if (!uri.contains("#") && !link.className().equals("image") && !link.className().equals("internal")
                            && host != null && Uri.parse(uri).getHost().equals(Uri.parse(doc.location()).getHost())
                            && !uri.contains(".php") && !uri.contains(".xml")) {
                        try {
                            Document d1 = Jsoup.connect(uri).get();
                            counter++;
                            WebPagesModel webPagesModel = new WebPagesModel(uri, d1.title(), false, d1.body().text().split(" ").length);
                            uploadWebPage(webPagesModels, webPagesModel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (counter >= MAX_NUM_OF_LINKS_PER_CRAWLING_ONE_WEBPAGE)
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void uploadWebPage(List<WebPagesModel> webPagesModels, WebPagesModel webPagesModel) {
        String lan = language;
        FirebaseFirestore.getInstance().collection(lan+WEBPAGES_LINKS).document(webPagesModel.getKey()).set(webPagesModel);
        if (webPagesModels.size() < (MAX_NUM_OF_VISITED_PAGE / numOfThreads)){
            webPagesModels.add(webPagesModel);
        }
    }
}