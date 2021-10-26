package com.example.erlab.models;

import android.content.Context;

import com.example.erlab.R;
import com.google.android.exoplayer.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @Class ChannelNode, model class to handle date
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public class ChannelNode {

    String videoTitle;
    String videoURL;
    String videoThumbnail;
    String contentId;
    int type;

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ChannelNode(String videoTitle, String videoURL, String videoThumbnail, int type) {
        this.videoTitle = videoTitle;
        this.videoURL = videoURL;
        this.videoThumbnail = videoThumbnail;
        this.contentId = contentId;
        this.type = type;
        this.contentId = videoTitle.toLowerCase(Locale.US).replaceAll("\\s", "");
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.videoThumbnail = videoThumbnail;
    }

    //will work as a fake data source
    public static List<ChannelNode> getStreamingURLs(Context context) {
        List<ChannelNode> nodeModels = new ArrayList<>();
        nodeModels.add(new ChannelNode("Channel #1", context.getResources().getString(R.string.url1), context.getResources().getString(R.string.thumbnail), Util.TYPE_HLS));
        nodeModels.add(new ChannelNode("Channel #2", context.getResources().getString(R.string.url2), context.getResources().getString(R.string.thumbnail), Util.TYPE_HLS));
        nodeModels.add(new ChannelNode("Channel #3", context.getResources().getString(R.string.url3), context.getResources().getString(R.string.thumbnail), Util.TYPE_HLS));
        nodeModels.add(new ChannelNode("Channel #4", context.getResources().getString(R.string.url4), context.getResources().getString(R.string.thumbnail), Util.TYPE_HLS));
        nodeModels.add(new ChannelNode("Channel #5", context.getResources().getString(R.string.url6), context.getResources().getString(R.string.thumbnail), Util.TYPE_DASH));


        return nodeModels;
    }

}
