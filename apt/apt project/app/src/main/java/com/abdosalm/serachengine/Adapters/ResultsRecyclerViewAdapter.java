package com.abdosalm.serachengine.Adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.abdosalm.serachengine.Constants.Constants.HINTS_LIST;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_KEYWORD;
import static com.abdosalm.serachengine.Constants.Constants.INTENT_LANGUAGE;
import static com.abdosalm.serachengine.Constants.Constants.NORMAL_CELL;
import static com.abdosalm.serachengine.Constants.Constants.SEARCH_BAR;
import static com.abdosalm.serachengine.Constants.Constants.SEE_MORE_BUTTON;
import static com.abdosalm.serachengine.Constants.Constants.SEE_MORE_RESULTS_NUM;
import static com.abdosalm.serachengine.Constants.Constants.SHARED_PREFERENCE_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abdosalm.serachengine.Interfaces.RankerAddedClickListener;
import com.abdosalm.serachengine.Models.ResultsModel;
import com.abdosalm.serachengine.R;
import com.abdosalm.serachengine.ResultsActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ResultsRecyclerViewAdapter extends RecyclerView.Adapter<ResultsRecyclerViewAdapter.ViewModel>{
    private final Context context;
    private final List<ResultsModel>resultsModels;
    private int size;
    private String[] historyItems;
    private int pos;
    String language;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private ExecutorService executorService;

    public ResultsRecyclerViewAdapter(Context context, List<ResultsModel> resultsModels, int pos,ExecutorService executorService) {
        this.context = context;
        this.resultsModels = resultsModels;
        size = SEE_MORE_RESULTS_NUM;
        if (size > resultsModels.size())
            size = resultsModels.size();

        this.pos = pos;
        this.executorService = executorService;
    }

    @NonNull
    @Override
    public ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SEARCH_BAR){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.results_search_bar,parent,false);
        }else if(viewType == SEE_MORE_BUTTON){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.results_row_see_more,parent,false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.results_row,parent,false);
        }

        return new ViewModel(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ResultsRecyclerViewAdapter.ViewModel holder, int pose) {
  //      Log.d("TAG", "onBindViewHolder: " + resultsModels.size());
        //for (ResultsModel resultsModel : resultsModels)
   //         Log.d("resultsMode", "onBindViewHolder: " + resultsModel.getTitle());
        if (pose == 0){
            // get the words that that user have searched for before
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE);
            historyItems = sharedPreferences.getString(HINTS_LIST,"").split("#");
            ArrayAdapter<String> historyAdapter = new ArrayAdapter<String> (context, android.R.layout.simple_list_item_1, historyItems);
            holder.searchEditText.setAdapter(historyAdapter);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.spinner_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
            holder.languageSpinner.setAdapter(adapter);
            holder.languageSpinner.setSelection(pos);
            holder.languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 1) {
                        pos = 1;
                        language = "en-us";
                    } else if (position == 0) {
                        language = "ar";
                        pos = 0;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            holder.micImageView.setOnClickListener(v -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
                try {
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast.makeText(context, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.searchButton.setOnClickListener(v->{
                String word = holder.searchEditText.getText().toString().trim();
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

                    // stop background tasks
                    executorService.shutdownNow();

                    // go the results activity
                    Intent intent = new Intent(context, ResultsActivity.class);
                    intent.putExtra(INTENT_KEYWORD, word.trim());
                    intent.putExtra(INTENT_LANGUAGE, holder.languageSpinner.getSelectedItem().toString());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                } else {
                    Toast.makeText(context, "please enter keyword", Toast.LENGTH_SHORT).show();
                }
            });


        }else if(pose == size + 1){
            holder.seeMoreButton.setOnClickListener(v->{
                if ((size + SEE_MORE_RESULTS_NUM )< resultsModels.size())
                    size += SEE_MORE_RESULTS_NUM;
                else{
                    size = resultsModels.size();
                    holder.seeMoreButton.setVisibility(View.GONE);
                }

                notifyDataSetChanged();
            });
        }else{
     //       Log.d("TAG", "onBindViewHolder: " + resultsModels.toString());
            holder.titleTextView.setText(resultsModels.get(pose-1).getTitle());
            holder.bodyTextView.setText(resultsModels.get(pose-1).getBody());
            holder.linkTitleTextView.setText(resultsModels.get(pose-1).getUri().toString());
            holder.view.setOnClickListener(v->{
                Intent intent = new Intent(Intent.ACTION_VIEW,resultsModels.get(pose-1).getUri());
                context.startActivity(intent);
       //         Log.d("pos", "onBindViewHolder: " + position + " --- " + resultsModels.get(position-1).getUri());
            });
        }
    }

    @Override
    public int getItemCount() {
        return size + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return SEARCH_BAR;
        }else if (position == size + 1){
            return SEE_MORE_BUTTON;
        }else{
            return NORMAL_CELL;
        }
    }

    public static class ViewModel extends RecyclerView.ViewHolder{

        // specified to results cells
        public TextView titleTextView;
        public TextView bodyTextView;
        public TextView linkTitleTextView;
        public View view;

        // specified to loading more links
        public Button seeMoreButton;

        // specified to searching
        public AutoCompleteTextView searchEditText;
        public ImageView micImageView;
        public Button searchButton;
        private final Spinner languageSpinner;

        public ViewModel(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.rowTitleTextView);
            bodyTextView = itemView.findViewById(R.id.rowBodyTextView);
            linkTitleTextView = itemView.findViewById(R.id.linkTitleTextView);
            seeMoreButton = itemView.findViewById(R.id.seeMoreButton);
            searchButton = itemView.findViewById(R.id.SearchButtonResult);
            micImageView = itemView.findViewById(R.id.micImageViewResult);
            searchEditText = itemView.findViewById(R.id.searchEditTextResult);
            languageSpinner = itemView.findViewById(R.id.languageSpinnerResult);

            this.view = itemView;
        }
    }

}
