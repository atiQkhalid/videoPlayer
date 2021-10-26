package com.example.erlab.fragments;

import com.example.erlab.models.ChannelNode;
import java.util.List;

/**
 * @Class HomeView, interface to update the view(Screen)
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public interface HomeView {

    void onStreamingList(List<ChannelNode> channelNodeList);

    void onError(String errorMessage);
}
