package com.abdosalm.serachengine.Ranker;

import static com.abdosalm.serachengine.Constants.Constants.H1_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.H2_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.H3_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.H4_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.H5_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.H6_WEIGHT;
import static com.abdosalm.serachengine.Constants.Constants.NUM_OF_WORDS_BEFORE_AFTER;
import static com.abdosalm.serachengine.Constants.Constants.SEE_MORE_RESULTS_NUM;
import static com.abdosalm.serachengine.Constants.Constants.TITLE_WEIGHT;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdosalm.serachengine.Adapters.ResultsRecyclerViewAdapter;
import com.abdosalm.serachengine.Interfaces.RankerAddedClickListener;
import com.abdosalm.serachengine.Models.InvertedIndexModel;
import com.abdosalm.serachengine.Models.InvertedWordModel;
import com.abdosalm.serachengine.Models.ResultsModel;
import com.abdosalm.serachengine.ResultsActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ranker extends Activity{
    private List<ResultsModel> results;
    private final List<InvertedIndexModel> invertedWordModelList;
    private final boolean isEnglishLinks;
    private final List<String> query;
    private int counterToUpdateView = 0;
    private final ExecutorService executor;
    private final Context context;
    private final RankerAddedClickListener rankerAddedClickListener;
    private final List<ResultsModel> dummyAdding;

    public Ranker(List<InvertedIndexModel> invertedWordModelList, List<String> query, boolean isEnglishLinks, Context context , RankerAddedClickListener rankerAddedClickListener,ExecutorService executor) {
        this.invertedWordModelList = invertedWordModelList;
        this.isEnglishLinks = isEnglishLinks;
        this.query = query;
        this.executor = executor;

        this.context = context;
        this.rankerAddedClickListener = rankerAddedClickListener;
        //this.results = results;
        this.results = new ArrayList<>();
        dummyAdding = new ArrayList<>();

        // clean the query
        for (int i = 0; i < query.size(); i++) {
            query.set(i,query.get(i).toLowerCase());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void rank() {

        // because this requires internet connection then run on multiple threads to make it fast
        executor.execute(() -> {
            // calculate score first to sort the links
            for (InvertedIndexModel invertedIndexModel : invertedWordModelList) {
                setData(invertedIndexModel);
            }

            // sort the results
            sortResults();
      //      for (ResultsModel resultsModel : results)
        //        Log.d("results", "run: " + resultsModel.getFinalScore());

            // get the description
            SpannableString description;
            for (ResultsModel resultsModel : results){
                dummyAdding.add(resultsModel);

                description = getDescription(resultsModel.getUri().toString());
                // sets the results mode
                resultsModel.setBody(description);
                // get the description of each link and send it
                if (results.size() <SEE_MORE_RESULTS_NUM || counterToUpdateView %  SEE_MORE_RESULTS_NUM == 0){
        //            Log.d("testData", "setData: " + counterToUpdateView);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("test", "run: " + results.size());
                            rankerAddedClickListener.addItem(dummyAdding);
                            dummyAdding.clear();
                        }
                    });
                }
            }
        });

        //Collections.sort(results, Comparator.comparing(ResultsModel::getFinalScore));
        // sorting the results
        //sortResults();

    }

    private void setData(InvertedIndexModel invertedIndexModel) {
        double score = 0;
        float rank = 0;
        double tagsWeight = 0;
        double finalScore = 0;


        ResultsModel resultsModel = new ResultsModel();

        // get the rank
        rank = invertedIndexModel.getWebPagesModel().getRank();

        // calculate the tags weight
        if (invertedIndexModel.isTitle())
            tagsWeight += TITLE_WEIGHT;
        if (invertedIndexModel.isH1())
            tagsWeight += H1_WEIGHT;
        if (invertedIndexModel.isH2())
            tagsWeight += H2_WEIGHT;
        if (invertedIndexModel.isH3())
            tagsWeight += H3_WEIGHT;
        if (invertedIndexModel.isH4())
            tagsWeight += H4_WEIGHT;
        if (invertedIndexModel.isH5())
            tagsWeight += H5_WEIGHT;
        if (invertedIndexModel.isH6())
            tagsWeight += H6_WEIGHT;

        if (rank > 0)
            rank *= 0.00000001;


        // equation for the final score
        if (isEnglishLinks)
            finalScore = 79 * score + 500 * tagsWeight + 5 *  rank ;
        else
            finalScore = 69 * score + 500 * tagsWeight + 5 * rank;

        resultsModel.setTitle(invertedIndexModel.getWebPagesModel().getTitle());
        resultsModel.setUri(Uri.parse(invertedIndexModel.getWebPagesModel().getUri()));
        resultsModel.setFinalScore(finalScore);

        results.add(resultsModel);
    }
    private SpannableString getDummyDescription(String link){
        Document document = null;
        try {
            document = Jsoup.connect(link).get();
            String[] text = document.text().split(" ");
          //  Log.d("test", "getDummyDescription: ");
            StringBuilder dummy = new StringBuilder();
            int i = 0;
            for (String s : text) {
                if (i < 20){
                    i++;
                    dummy.append(s).append(" ");
                }
            }
          //  Log.d("wow", "getDummyDescription: " + dummy.toString());
            return new SpannableString(dummy.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private SpannableString getDescription(String link) {
        counterToUpdateView++;
        List<Integer> indexes = new ArrayList<>();
        try {
            Document document = Jsoup.connect(link).get();
            List<String> text = Arrays.asList(document.text().split(" "));

            // clean the document
            for (int i = 0; i < text.size(); i++) {
                text.set(i,text.get(i).toLowerCase());
            }

            for (String s : query) {
                if (text.contains(s))
                    indexes.add(text.indexOf(s));
            }

            StringBuilder descriptionToShow = new StringBuilder();
            String s = text.toString();
          //  Log.d("bidyText", "getDescription: " +s);
            if (indexes.size() != 0) {
                // show only a paragraph of 30 word for every word
                for (String m : query) {
                //    Log.d("indexOFNUm", "getDescription: " + s.indexOf(m));
                    int start = text.indexOf(m);
                    if (start != -1) {
                 //       Log.d("startOF", "getDescription: " + start);
                        int min = start - NUM_OF_WORDS_BEFORE_AFTER;
                        int max = start + NUM_OF_WORDS_BEFORE_AFTER;
                        if (min < 0)
                            min = 0;
                        if (max > s.length())
                            max = s.length();
                        for(int i = min; i < max ; i++) {
                            descriptionToShow.append(text.get(i)).append(" ");
                        }
                    //    Log.d("descritpionTOShowTo", "getDescription: " + descriptionToShow.toString());
                    }
                    descriptionToShow.append("\n");
                }

             //   Log.d("show", "getDescription: ");

                SpannableString description = new SpannableString(descriptionToShow.toString());

                for (String m : query) {
              //      Log.d("indexOFNUm", "getDescription: " + s.indexOf(m));
                    if (s.contains(m)){
                        int start = descriptionToShow.toString().indexOf(m);
                        description.setSpan(new UnderlineSpan(), start, start + m.length(), 0);
                        description.setSpan(new ForegroundColorSpan(Color.RED), start, start + m.length(), 0);
                    }
                }
            //    Log.d("testDescriptionHEllo", "getDescription: " +description.toString());

                if (description.toString().trim().equals(""))
                    return getDummyDescription(link);
                else
                    return description;

            } else{
                // prints out by default the first 15 word in the document
                return getDummyDescription(link);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //to sort the result according to the final score
    public void sortResults() {
        List<ResultsModel> temp = new ArrayList<ResultsModel>(this.results);
        this.results = new ArrayList<ResultsModel>();

        while (!temp.isEmpty()) {
            ResultsModel max;
            max = temp.get(0);
            for (int j = 0; j < temp.size(); j++) {
                if (temp.get(j).getFinalScore() > max.getFinalScore())
                    max = temp.get(j);
            }
            temp.remove(max);
            this.results.add(max);
        }
    }

}
