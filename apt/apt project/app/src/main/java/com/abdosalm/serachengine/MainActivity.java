package com.abdosalm.serachengine;

import static com.abdosalm.serachengine.Constants.Constants.CHILD_WEBPAGE_MODEL_ID;
import static com.abdosalm.serachengine.Constants.Constants.CHILD_WEBPAGE_MODEL_RANK;
import static com.abdosalm.serachengine.Constants.Constants.FLAGS_NUM_OF_AR_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.FLAGS_NUM_OF_EN_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.HINTS_LIST;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_IS_ADMIN;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_KEYWORD;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_LANGUAGE;
import static com.abdosalm.serachengine.Constants.Constants.SHARED_PREFERENCE_NAME;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ARABIC_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_ENGLISH_LINKS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FINISHED_CRAWL_FLAG;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FINISHED_INDEX_FLAG;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_FIREBASE_FLAGS;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_IMPORTANT;
import static com.abdosalm.serachengine.Constants.Constants.WEBPAGES_LINKS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abdosalm.serachengine.Crawler.Crawler;
import com.abdosalm.serachengine.Indexer.Indexer;
import com.abdosalm.serachengine.Models.FlagsModel;
import com.abdosalm.serachengine.Models.InvertedIndexModel;
import com.abdosalm.serachengine.Models.InvertedWordModel;
import com.abdosalm.serachengine.Models.WebPageJointsModel;
import com.abdosalm.serachengine.Models.WebPagesModel;
import com.abdosalm.serachengine.Util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView searchEditText;
    private ImageView micImageView;
    private Button searchButton;
    private Button crawlButton;
    private Spinner languageSpinner;
    private TextView numOfThreadsTextView;
    private SeekBar NumOfThreadsSeekbar;
    private CardView numOfThreadsCardView;
    private Button indexButton;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    String language = "ar";
    private int numberOfThreads = 1;
    private String[] historyItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        searchButton = findViewById(R.id.SearchButton);
        micImageView = findViewById(R.id.micImageView);
        searchEditText = findViewById(R.id.searchEditText);
        languageSpinner = findViewById(R.id.languageSpinner);
        crawlButton = findViewById(R.id.crawlButton);
        numOfThreadsTextView = findViewById(R.id.numOfThreadsTextView);
        NumOfThreadsSeekbar = findViewById(R.id.NumOfThreadsSeekbar);
        indexButton = findViewById(R.id.indexButton);
        numOfThreadsCardView = findViewById(R.id.numOfThreadsCardView);


        // get the words that that user have searched for before
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        historyItems = sharedPreferences.getString(HINTS_LIST,"").split("#");
        ArrayAdapter<String> historyAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, historyItems);
        searchEditText.setAdapter(historyAdapter);


        // gives the admin ability to crawl and index
        Intent isAdminIntent = getIntent();
        boolean isAdmin = isAdminIntent.getBooleanExtra(INTENT_IS_ADMIN,false);
        if (isAdmin){
            crawlButton.setVisibility(View.VISIBLE);
            indexButton.setVisibility(View.VISIBLE);
            numOfThreadsCardView.setVisibility(View.VISIBLE);
        }else{
            crawlButton.setVisibility(View.GONE);
            indexButton.setVisibility(View.GONE);
            numOfThreadsCardView.setVisibility(View.GONE);
        }


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    language = "en-us";
                } else if (position == 0) {
                    language = "ar";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        micImageView.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        searchButton.setOnClickListener(v -> {
            String word = searchEditText.getText().toString().trim();
            if (!word.equals("")) {
                // store that key in both shared preference and the string array
                boolean isItThere = false;
                for (String s : historyItems){
                    if (s.equals(word)){
                        isItThere = true;
                        break;
                    }
                }
                if (!isItThere){
                    String[] dummy = new String[historyItems.length + 1];
                    int i = 0;
                    for (; i < historyItems.length; i++) {
                        dummy[i] = historyItems[i];
                    }
                    historyItems = dummy;
                    historyItems[i] = word;

                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < historyItems.length; j++) {
                        sb.append(historyItems[j]).append("#");
                    }
                    historyAdapter.add(word);
                    sharedPreferences.edit().putString(HINTS_LIST, sb.toString()).apply();
                    historyAdapter.notifyDataSetChanged();
                }


                // go the results activity
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra(INTENT_KEYWORD, word.trim());
                intent.putExtra(INTENT_LANGUAGE, languageSpinner.getSelectedItem().toString());
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "please enter keyword", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!searchEditText.getText().toString().trim().equals("")) {
                    Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                    intent.putExtra(INTENT_KEYWORD, searchEditText.getText().toString().trim());
                    intent.putExtra(INTENT_LANGUAGE, languageSpinner.getSelectedItem().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "please enter keyword", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        NumOfThreadsSeekbar.setProgress(0);
        numOfThreadsTextView.setText(String.valueOf(numberOfThreads));
        NumOfThreadsSeekbar.setMax(100);
        NumOfThreadsSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
           //     Log.d("TAG", "onProgressChanged: " + progress);

                if (progress == 0) {
                    NumOfThreadsSeekbar.setProgress(1);
                } else {
                    numberOfThreads = progress;
                    numOfThreadsTextView.setText(String.valueOf(numberOfThreads));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).addSnapshotListener((value, error) -> {
            if (value != null){
                FlagsModel flagsModel = value.toObject(FlagsModel.class);
                if (flagsModel != null && isAdmin){
                    if (flagsModel.isFinishedCrawling()){
                        indexButton.setVisibility(View.VISIBLE);
                    }else{
                        indexButton.setVisibility(View.GONE);
                    }

                    if (flagsModel.isFinishedIndexing() && isAdmin){
                        crawlButton.setVisibility(View.VISIBLE);
                    }else{
                        crawlButton.setVisibility(View.GONE);
                    }
                }
            }
        });


        indexButton.setOnClickListener(v -> {
           FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(WEBPAGES_FIREBASE_FINISHED_INDEX_FLAG,false);

            Indexer indexer = new Indexer(numberOfThreads, getApplicationContext());
            indexer.index();

        });

        crawlButton.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection(WEBPAGES_IMPORTANT).document(WEBPAGES_FIREBASE_FLAGS).update(WEBPAGES_FIREBASE_FINISHED_CRAWL_FLAG,false);

            Crawler c = new Crawler(numberOfThreads,getApplicationContext());
            c.crawl();
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                searchEditText.setText(Objects.requireNonNull(result).get(0));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_view_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),ActivityLogin.class));
            finish();
        }else if (id == R.id.copyRights){
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(){
        View view = getLayoutInflater().inflate(R.layout.copy_rights_layout,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.create().show();
    }
}