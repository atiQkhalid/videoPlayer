package com.example.erlab.fragments;

import android.content.Context;
import com.example.erlab.R;
import com.example.erlab.models.ChannelNode;
import java.util.List;

/**
 * @Class HomeRepository, API level handler class to deal with the business logic
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public class HomeRepository implements HomePresenter {

    private HomeView homeView;

    HomeRepository(HomeView homeView) {
        this.homeView = homeView;
    }

    @Override
    public void getStreamingList(Context context) {

        //mocked the fake data to complete the API flow
        List<ChannelNode> channelNodeList = ChannelNode.getStreamingURLs(context);

        if (channelNodeList.size()!= 0){
            homeView.onStreamingList(channelNodeList);
        }else{
            homeView.onError(context.getResources().getString(R.string.something_went_wrong));
        }
    }
}
