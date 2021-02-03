package com.example.restaurantapp.Callback;

import com.example.restaurantapp.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);

    void onPopularLoadFailed(String message);
}
