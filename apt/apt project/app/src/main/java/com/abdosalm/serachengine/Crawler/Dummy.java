package com.abdosalm.serachengine.Crawler;

import static com.abdosalm.serachengine.Constants.Constants.CHILD_WEBPAGE_MODEL_ID;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ARABIC_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.abdosalm.serachengine.Models.WebPageJointsModel;
import com.abdosalm.serachengine.Models.WebPagesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Dummy implements Runnable{
    /*seeds -> english:
    https://en.wikipedia.org/wiki/Computer
    https://www.javatpoint.com/
    https://stackoverflow.com/
    https://www.espn.com/
    https://www.nbcsports.com/
    https://www.goal.com/en-us
    https://news.yahoo.com/
    https://sports.yahoo.com/?guccounter=1
    https://www.skysports.com/
    https://www.loc.gov/
    https://education.asianart.org/
    https://worldhistoryproject.org/
    https://www.worldhistory.org/
    https://www.theguardian.com/international
    https://www.rottentomatoes.com/
    https://www.reddit.com/
    https://www.amazon.com/
    https://www.foxnews.com/
    https://www.bbc.com/news

    seeds -> arabic:
    https://ar.wikipedia.org/wiki/%D8%A7%D9%84%D8%B5%D9%81%D8%AD%D8%A9_%D8%A7%D9%84%D8%B1%D8%A6%D9%8A%D8%B3%D9%8A%D8%A9

     */
    @Override
    public void run() {

       /*try {
            doc = Jsoup.connect("https://en.wikipedia.org/wiki/Computer").get();
            Log.d("TAG", "run: " + doc.body().text().split(" ").length);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

           String[] links = new String[]{"https://en.wikipedia.org/wiki/Computer",
                    "https://www.javatpoint.com/",
                    "https://stackoverflow.com/",
                    "https://www.espn.com/",
                    "https://www.nbcsports.com/",
                    "https://www.goal.com/en-us",
                    "https://news.yahoo.com/",
                    "https://sports.yahoo.com/?guccounter=1",
                    "https://www.skysports.com/",
                    "https://www.loc.gov/",
                    "https://education.asianart.org/",
                    "https://worldhistoryproject.org/",
                    "https://www.worldhistory.org/",
                    "https://www.theguardian.com/international",
                    "https://www.rottentomatoes.com/",
                    "https://www.reddit.com/",
                    "https://www.amazon.com/",
                    "https://www.foxnews.com/",
                    "https://www.bbc.com/news"
            };
            Map<String,WebPagesModel> test = new HashMap<>();
            for (String s : links){
                try {
                    Document document = Jsoup.connect(s).get();
                    WebPagesModel e = new WebPagesModel(s,document.title(),false,document.body().text().split(" ").length);
                    //test.put(e.getKey(),e);
                    FirebaseFirestore.getInstance().collection("englishLinks").document(e.getKey()).set(e);
             //       Log.d("TAG", "run: " + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*FirebaseFirestore databaseReference = FirebaseFirestore.getInstance();
            databaseReference.collection(WEBPAGES_FIREBASE).document(WEBPAGES_FIREBASE_ENGLISH_LINKS).set(test,SetOptions.merge());*/

            /*for (WebPagesModel webPagesModel : test){
                databaseReference.child(WEBPAGES_FIREBASE).child(WEBPAGES_FIREBASE_ENGLISH_LINKS).child(webPagesModel.getKey()).setValue(webPagesModel);
            }*/

            /*String[] links = new String[]{"https://ar.wikipedia.org/wiki/%D8%A7%D9%84%D8%B5%D9%81%D8%AD%D8%A9_%D8%A7%D9%84%D8%B1%D8%A6%D9%8A%D8%B3%D9%8A%D8%A9",
                    "https://www.olx.com.eg/",
                    "https://www.amazon.eg/",
                    "https://www.yallakora.com/",
                    "https://www.masrawy.com/",
                    "https://www.egypt.gov.eg/arabic/home.aspx",
                    "https://digital.gov.eg/",
                    "https://arabic.rt.com/",
                    "https://www.independentarabia.com/",
                    "https://www.presidency.eg/ar",
                    "https://www.jumia.com.eg/ar/"
            };

            Map<String,WebPagesModel> test = new HashMap<>();
            for (String s : links){
                try {
                    Document document = Jsoup.connect(s).get();
                    WebPagesModel e = new WebPagesModel(s,document.title(),false,document.body().text().split(" ").length);
                    //test.put(e.getKey(),e);
                    FirebaseFirestore.getInstance().collection("arabicLinks").document(e.getKey()).set(e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/

        /*FirebaseFirestore databaseReference = FirebaseFirestore.getInstance();
        databaseReference.collection(WEBPAGES_FIREBASE).document(WEBPAGES_FIREBASE_ARABIC_LINKS).set(test);*/

            /*for (WebPagesModel webPagesModel : test){
                databaseReference.child("test").child(WEBPAGES_FIREBASE_ARABIC_LINKS).child(webPagesModel.getKey()).setValue(webPagesModel);
            }*/
      //      Log.d("TAG", "run: success");

           /* doc = Jsoup.connect("https://en.wikipedia.org/wiki/Computer").get();
            Elements links = doc.select("a[href]");

            Log.d("crawl", "links size  = "+ doc.title() + " " + links.size());
            int count = 0;
            LinkedHashSet<String> test = new LinkedHashSet<>();

            Log.d("crawl", "run: " + test);
            for (Element link : links){
                Log.d("crawl", link.attr("abs:href") + " ---------- " + link.className());
                String t1 = Uri.parse(link.attr("abs:href")).getHost();
                // using authority instead of host will make no such a big difference
                //Log.d("crawl", Uri.parse(link.attr("abs:href")).normalizeScheme() + " == " +Uri.parse(link.attr("abs:href")).getHost());
                if(!link.attr("abs:href").contains("#")  && !link.className().equals("image") && !link.className().equals("internal")
                        && t1 != null && Uri.parse(link.attr("abs:href")).getHost().equals(Uri.parse(doc.location()).getHost())
                        && !link.attr("abs:href").contains(".php") && !link.attr("abs:href").contains(".xml"))
                {
                    Log.d("crawl", link.attr("abs:href") + " ---------- " + link.className());
                    count++;
                }


            }
            Log.d("crawl", "links size  = "+ count);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
