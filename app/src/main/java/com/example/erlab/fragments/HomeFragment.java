package com.example.erlab.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.erlab.R;
import com.example.erlab.activities.PlayerActivity;
import com.example.erlab.adapters.HomeAdapter;
import com.example.erlab.models.ChannelNode;

import java.util.List;

/**
 * @Class HomeFragment
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public class HomeFragment extends Fragment implements HomeView, HomeAdapter.OnChannelClickListener {

    private RecyclerView recyclerViewChannels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mian_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewChannels = view.findViewById(R.id.recyclerViewChannels);

        HomePresenter homePresenter = new HomeRepository(this);
        homePresenter.getStreamingList(getContext());
    }

    @Override
    public void onStreamingList(List<ChannelNode> channelNodeList) {
        HomeAdapter homeAdapter = new HomeAdapter(channelNodeList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewChannels.setLayoutManager(linearLayoutManager);
        recyclerViewChannels.setAdapter(homeAdapter);
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickedListener(ChannelNode datum) {
        Toast.makeText(getContext(), datum.getVideoURL(), Toast.LENGTH_SHORT).show();

        onSampleSelected(datum);
    }

    private void onSampleSelected(ChannelNode channelNode) {
        startActivity(PlayerActivity.getVideoPlayerIntent(getContext(),
                channelNode.getVideoURL(), channelNode.getVideoTitle(), R.drawable.ic_video_play));
    }
}