package com.example.restaurantapp.Callback;

import com.example.restaurantapp.Model.BestDealModel;

import java.util.List;

public interface IBestDealCallBackListener {
    void onBestDealILoadSuccess(List<BestDealModel> bestDealModelList);

    void onBestDealILoadFail(String message);
}
