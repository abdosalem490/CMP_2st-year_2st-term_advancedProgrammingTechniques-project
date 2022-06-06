package com.abdosalm.serachengine;

import static com.abdosalm.serachengine.Constants.Constants.INTENT_KEYWORD;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_LANGUAGE;
import static com.abdosalm.serachengine.Constants.Constants.INVERTED_INDEX_FIREBASE;
import static com.abdosalm.serachengine.Constants.Constants.SEARCH_WORD;
import static com.abdosalm.serachengine.Constants.Constants.SEE_MORE_RESULTS_NUM;
import static com.abdosalm.serachengine.Constants.Constants.SPINNER_LANGUAGE_ARABIC;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ARABIC_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_IMPORTANT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.abdosalm.serachengine.Adapters.ResultsRecyclerViewAdapter;
import com.abdosalm.serachengine.Interfaces.RankerAddedClickListener;
import com.abdosalm.serachengine.Models.FlagsModel;
import com.abdosalm.serachengine.Models.InvertedIndexModel;
import com.abdosalm.serachengine.Models.InvertedWordModel;
import com.abdosalm.serachengine.Models.ResultsModel;
import com.abdosalm.serachengine.Ranker.Ranker;
import com.abdosalm.serachengine.Util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ResultsActivity extends AppCompatActivity implements RankerAddedClickListener{
    private RecyclerView resultsRecyclerView;
    private ResultsRecyclerViewAdapter resultsRecyclerViewAdapter;
    private List<ResultsModel> list;
    private String languageSelected;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    boolean isPhraseSearch = false;
    private List<String> queryToSearchFor;
    private ArrayList<String> wordsBeforeCleaning;
    private List<InvertedWordModel> invertedWordModelList;
    private List<ResultsModel> results;
    private int counter = 0;
    private int pos;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        invertedWordModelList = new ArrayList<>();
        results  = new ArrayList<>();

        Intent intent = getIntent();
        String keyword = intent.getStringExtra(INTENT_KEYWORD);
        String language = intent.getStringExtra(INTENT_LANGUAGE);

        if (language.equals(SPINNER_LANGUAGE_ARABIC))
            languageSelected = WEBPAGES_FIREBASE_ARABIC_LINKS;
        else
            languageSelected = WEBPAGES_FIREBASE_ENGLISH_LINKS;

        executor = Executors.newFixedThreadPool(1);


        String lan = WEBPAGES_FIREBASE_ENGLISH_LINKS;
        if (language.equals(SPINNER_LANGUAGE_ARABIC))
            lan = WEBPAGES_FIREBASE_ARABIC_LINKS;

        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        list = new ArrayList<>();

        int pos = 1;
        if (language.equals(SPINNER_LANGUAGE_ARABIC))
            pos = 0;

        String t = "I am dummy text";
        SpannableString test = new SpannableString(t);
        test.setSpan(new UnderlineSpan(), 0, 10, 0);
        test.setSpan(new ForegroundColorSpan(Color.RED), 0, 10, 0);

        // get the type of query (free (same as single) , phrase)
        if (keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"') {
            isPhraseSearch = true;
       //     Log.d("TAG", "phrase");
            keyword = keyword.substring(1, keyword.length() - 1);
        }

        // clean that word and convert it into clean string array
        if (language.equals(SPINNER_LANGUAGE_ARABIC))
            queryToSearchFor = Util.cleanArabicTitle(keyword);
        else
            queryToSearchFor = Util.cleanEnglishTitle(keyword, getApplicationContext());

        // this list is used to Bold the places of the occurrence of the word
        wordsBeforeCleaning = new ArrayList<>(Arrays.asList(keyword.split(" ")));

        if (queryToSearchFor.isEmpty()) {
            ConstraintLayout layout = findViewById(R.id.resultRelativeLayout);
            Snackbar snackbar = Snackbar.make(layout, "No results", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        // most of the code here is the job of query processor
        // grep the data from the database
        String finalLan = lan;
        FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> flag) {
                if (flag.isSuccessful() && flag.getResult() != null && !queryToSearchFor.isEmpty()) {
                    FlagsModel flagsModel = flag.getResult().toObject(FlagsModel.class);
                    FirebaseFirestore.getInstance().collection(finalLan + INVERTED_INDEX_FIREBASE).whereIn(SEARCH_WORD, queryToSearchFor).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().size() != 0) {
                      //      Log.d("TAG", "onCreate: " + queryToSearchFor.toString());
                     //       Log.d("search", "onCreate: " + queryToSearchFor.toString() + " --> " + task.getResult().size());
                            for (QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()) {
                                InvertedWordModel invertedWordModel = queryDocumentSnapshots.toObject(InvertedWordModel.class);
                                invertedWordModelList.add(invertedWordModel);
                            }
                        }

                        // calculate the IDF , TF , score for each link;
                        assert flagsModel != null;
                        int totalNumOfLinks = flagsModel.getNumOfArabicLinks();
                        if (finalLan.equals(WEBPAGES_FIREBASE_ENGLISH_LINKS))
                            totalNumOfLinks = flagsModel.getNumOfEnglishLinks();
                        Util.calculate_TF_IDF_Score(invertedWordModelList, totalNumOfLinks);


                        if (isPhraseSearch) {
                            removeUnCommonLinks(invertedWordModelList);
                        }

                        ArrayList<InvertedIndexModel> toBeRanked = new ArrayList<>();
                        for (InvertedWordModel invertedWordModel : invertedWordModelList){
                            for (Map.Entry<String,InvertedIndexModel> invertedIndexModelEntry : invertedWordModel.getInvertedIndexModels().entrySet())
                                toBeRanked.add(invertedIndexModelEntry.getValue());
                        }

                        boolean isEnglish = true;
                        if (language.equals(SPINNER_LANGUAGE_ARABIC))
                            isEnglish = false;

                        Ranker ranker = new Ranker(toBeRanked,wordsBeforeCleaning,isEnglish,ResultsActivity.this,ResultsActivity.this,executor);
                        ranker.rank();

                  //      Log.d("size", "onComplete: " + results.size());

                  //      Log.d("TAG", "onCreate: " + list.size());
                   //     for (ResultsModel resultsModel : results)
                    //    {
                   //         Log.d("TAG", "onComplete: " + resultsModel.getFinalScore());
                    //    }

                    });
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logOut) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
            finish();
        } else if (id == R.id.copyRights) {
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.copy_rights_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //    Log.d("TAG", "mic ");
                EditText tempEditText = findViewById(R.id.searchEditTextResult);
                tempEditText.setText(Objects.requireNonNull(result).get(0));
            }
        }
    }

    private void removeUnCommonLinks(List<InvertedWordModel> invertedWordModelList) {
        // the links that are present in the first word for example must be present at all other words , other wise remove it
        int counter = 0;
       // Log.d("count", "size : " + invertedWordModelList.size());
        if (invertedWordModelList.size() != 0) {
            List<String> neededCommonUrls = new ArrayList<>(invertedWordModelList.get(0).getInvertedIndexModels().keySet());
            for (String urls : neededCommonUrls) {
                for (InvertedWordModel words : invertedWordModelList) {
                    List<String> dummy = new ArrayList<>(words.getInvertedIndexModels().keySet());
                    if (dummy.contains(urls))
                        counter++;
                }
         //       Log.d("count", "removeUnCommonLinks: " + counter + " ---> " + urls);
                if (counter != invertedWordModelList.size()) {
                    // remove this link from all occurrences
                    for (InvertedWordModel wordModel : invertedWordModelList) {
                        //toBeRemoved.add(wordModel.getInvertedIndexModels().get(urls));
                        wordModel.getInvertedIndexModels().remove(urls);
                  //      Log.d("count", urls + " removed");
                    }
                }
                counter = 0;
            }
        }
     /*   else {
                    // check if they are in order
                    // get the indexes of the word in the first link
                    List<Integer> indexes = Objects.requireNonNull(invertedWordModelList.get(0).getInvertedIndexModels().get(urls)).getIndexes();
                    for (Integer integer : indexes) {
                        for (int i = 1; i < invertedWordModelList.size(); i++) {
                            Log.d("counter ", "removeUnCommonLinks: " + i + " => " + invertedWordModelList.size() + " - " + invertedWordModelList.get(i).getIDF() + " --- " + invertedWordModelList.get(i).getWord());
                            //Log.d("TAG", "removeUnCommonLinks: " + Objects.requireNonNull(invertedWordModelList.get(i).getInvertedIndexModels().get(urls)).getIndexes().size());
                            if (invertedWordModelList.get(i).getInvertedIndexModels().get(urls) != null) {
                                List<Integer> dummyIndex = Objects.requireNonNull(invertedWordModelList.get(i).getInvertedIndexModels().get(urls)).getIndexes();
                                // if there is no at least one element common index plus by a number then delete it
                                Log.d("test", "removeUnCommonLinks: " + dummyIndex);
                                Log.d("test", "head: " + indexes);

                                if (!dummyIndex.contains(integer + i)) {
                                    Log.d("deleteLinks", "removeUnCommonLinks: " + indexes + " --> " + dummyIndex);
                                    // remove this link from all occurrences
                                    for (InvertedWordModel wordModel : invertedWordModelList) {
                                        Log.d("removeLinksFrom", "removeUnCommonLinks: " + urls + " --> " + integer + " => " + (integer + i));
                                        //toBeRemoved.add(wordModel.getInvertedIndexModels().get(urls));
                                        wordModel.getInvertedIndexModels().remove(urls);
                                        Log.d("select", urls + " removed");
                                    }
                                    break;
                                } else {
                                    Log.d("saveLinks", "removeUnCommonLinks: " + indexes + " --> " + dummyIndex + " == " + urls);
                                }
                            }

                        }
                    }
                }*/

    }



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addItem(List<ResultsModel> resultsModel) {
        results.addAll(resultsModel);
        counter++;
        if (counter == 1) {
            resultsRecyclerViewAdapter = new ResultsRecyclerViewAdapter(ResultsActivity.this, results, pos,executor);
            resultsRecyclerView.setHasFixedSize(true);
            resultsRecyclerView.setLayoutManager(new LinearLayoutManager(ResultsActivity.this));
            resultsRecyclerView.setAdapter(resultsRecyclerViewAdapter);
        }else if (counter > 1){
            resultsRecyclerViewAdapter.notifyDataSetChanged();
        }



        /*for (ResultsModel resultsModel1 : resultsModel)
            Log.d("resultsDummy", "run: " + resultsModel1.getTitle());*/
    }
    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}