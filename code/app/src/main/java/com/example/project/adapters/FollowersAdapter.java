package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import java.util.List;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.FollowerViewHolder> {

    private List<String> followersList;
    private OnRemoveClickListener onRemoveClickListener;

    public FollowersAdapter(List<String> followersList, OnRemoveClickListener listener) {
        this.followersList = followersList;
        this.onRemoveClickListener = listener;
    }

    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follower, parent, false);
        return new FollowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerViewHolder holder, int position) {
        String followerName = followersList.get(position);

        // For demonstration, just set name and a dummy mood
        holder.textFollowerName.setText(followerName);
        holder.textFollowerMood.setText("Happy Day!");

        holder.btnRemoveFollower.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followersList.size();
    }

    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        TextView textFollowerName, textFollowerMood;
        Button btnRemoveFollower;

        public FollowerViewHolder(@NonNull View itemView) {
            super(itemView);
            textFollowerName = itemView.findViewById(R.id.textFollowerName);
            textFollowerMood = itemView.findViewById(R.id.textFollowerMood);
            btnRemoveFollower = itemView.findViewById(R.id.btnRemoveFollower);
        }
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
