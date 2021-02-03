package com.example.restaurantapp.ui.fooddetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.restaurantapp.Common.Common;
import com.example.restaurantapp.Model.CommentModel;
import com.example.restaurantapp.Model.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveFood;
    private MutableLiveData<CommentModel> mutableLiveComment;


    public FoodDetailViewModel() {
        mutableLiveComment = new MutableLiveData<>();
    }

    public MutableLiveData<FoodModel> getMutableLiveFood() {
        if (mutableLiveFood == null) {
            mutableLiveFood = new MutableLiveData<>();
        }
        mutableLiveFood.setValue(Common.selectedFood);
        return mutableLiveFood;
    }

    public void setCommentModel(CommentModel commentModel) {
        if (mutableLiveComment != null) {
            mutableLiveComment.setValue(commentModel);
        }
    }

    public MutableLiveData<CommentModel> getMutableLiveComment() {
        return mutableLiveComment;
    }

    public void setFoodModel(FoodModel foodModel) {
        if(mutableLiveFood!=null){
            mutableLiveFood.setValue(foodModel);
        }
    }
}