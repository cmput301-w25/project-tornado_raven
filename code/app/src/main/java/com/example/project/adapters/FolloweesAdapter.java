package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class FolloweesAdapter extends RecyclerView.Adapter<FolloweesAdapter.FolloweeViewHolder> {

    private List<String> followees;

    public FolloweesAdapter(List<String> followees) {
        this.followees = followees;
    }

    @NonNull
    @Override
    public FolloweeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_following_user, parent, false);
        return new FolloweeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolloweeViewHolder holder, int position) {
        holder.textView.setText(followees.get(position));
    }

    @Override
    public int getItemCount() {
        return followees.size();
    }

    static class FolloweeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        FolloweeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textFolloweeName);
        }
    }
}

