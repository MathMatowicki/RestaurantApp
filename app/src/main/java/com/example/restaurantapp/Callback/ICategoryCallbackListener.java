package com.example.restaurantapp.Callback;

import com.example.restaurantapp.Model.BestDealModel;
import com.example.restaurantapp.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);

    void onCategoryLoadFail(String message);
}
