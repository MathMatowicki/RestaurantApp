package com.example.restaurantapp.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.restaurantapp.Callback.IBestDealCallBackListener;
import com.example.restaurantapp.Callback.IPopularCallbackListener;
import com.example.restaurantapp.Model.BestDealModel;
import com.example.restaurantapp.Common.Common;
import com.example.restaurantapp.Model.PopularCategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealCallBackListener {

    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealModel>> bestDealList;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;
    private IBestDealCallBackListener bestDealCallBackListener;


    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealCallBackListener = this;
    }

    public MutableLiveData<List<BestDealModel>> getBestDealList() {
        if (bestDealList == null) {
            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDealList();
        }
        return bestDealList;
    }

    private void loadBestDealList() {
        List<BestDealModel> tempList = new ArrayList<>();
        DatabaseReference bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapShot : snapshot.getChildren()) {
                    BestDealModel model = itemSnapShot.getValue(BestDealModel.class);
                    tempList.add(model);
                }
                bestDealCallBackListener.onBestDealILoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public HomeViewModel(MutableLiveData<List<PopularCategoryModel>> popularList, MutableLiveData<String> messageError) {
        this.popularList = popularList;
        this.messageError = messageError;
    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList() {
        if (popularList == null) {
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularList();
        }
        return popularList;
    }

    private void loadPopularList() {
        List<PopularCategoryModel> tempList = new ArrayList<>();
        DatabaseReference popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REF);
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapShot : snapshot.getChildren()) {
                    PopularCategoryModel model = itemSnapShot.getValue(PopularCategoryModel.class);
                    tempList.add(model);
                }
                popularCallbackListener.onPopularLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popularCallbackListener.onPopularLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {
        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {
        messageError.setValue(message);
    }

    @Override
    public void onBestDealILoadSuccess(List<BestDealModel> bestDealModelList) {
        bestDealList.setValue(bestDealModelList);
    }

    @Override
    public void onBestDealILoadFail(String message) {
        messageError.setValue(message);
    }
}