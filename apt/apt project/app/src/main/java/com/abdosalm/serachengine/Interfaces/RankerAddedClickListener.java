package com.abdosalm.serachengine.Interfaces;

import com.abdosalm.serachengine.Models.ResultsModel;

import java.util.List;

public interface RankerAddedClickListener {
    public void addItem(List<ResultsModel> resultsModel);
}
