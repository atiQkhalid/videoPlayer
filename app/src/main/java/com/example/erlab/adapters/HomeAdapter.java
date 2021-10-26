package com.example.erlab.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.erlab.R;
import com.example.erlab.models.ChannelNode;
import java.util.List;
import io.pixel.android.Pixel;

/**
 * @Class HomeAdapter to manipulate the list
 * @Author Malik Dawar
 * @Date 08 OCT 2020
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<ChannelNode> datumList;
    private OnChannelClickListener onChannelClickListener;

    public HomeAdapter(List<ChannelNode> datumList, OnChannelClickListener onChannelClickListener) {
        this.datumList = datumList;
        this.onChannelClickListener = onChannelClickListener;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_item_channel, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ChannelNode channelNode = datumList.get(position);
        holder.tvItemTitle.setText(channelNode.getVideoTitle());
        Pixel.load(channelNode.getVideoThumbnail(), holder.imageViewItem, null);
        holder.containerLinearLayout.setOnClickListener(v -> onChannelClickListener.onClickedListener(channelNode));
    }

    @Override
    public int getItemCount() {
        return datumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitle;
        ImageView imageViewItem;
        LinearLayout containerLinearLayout;

        public ViewHolder(View view) {
            super(view);
            tvItemTitle = view.findViewById(R.id.tvItemTitle);
            imageViewItem = view.findViewById(R.id.imageViewItem);
            containerLinearLayout = view.findViewById(R.id.containerLinearLayout);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setItemViewCacheSize(getItemCount());
    }

    public interface OnChannelClickListener {
        void onClickedListener(ChannelNode datum);
    }
}
