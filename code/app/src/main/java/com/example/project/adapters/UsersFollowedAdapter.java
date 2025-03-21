package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

/**
 * Adapter for listing the users that the current user follows.
 * Each item has an "Unfollow" button.
 */
public class UsersFollowedAdapter extends RecyclerView.Adapter<UsersFollowedAdapter.ViewHolder> {

    public interface OnUnfollowClick {
        void onUnfollow(int position);
    }

    private List<String> followedUsers;
    private OnUnfollowClick unfollowListener;

    public UsersFollowedAdapter(List<String> followedUsers, OnUnfollowClick unfollowListener) {
        this.followedUsers = followedUsers;
        this.unfollowListener = unfollowListener;
    }

    @NonNull
    @Override
    public UsersFollowedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_followed, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersFollowedAdapter.ViewHolder holder, int position) {
        String username = followedUsers.get(position);
        holder.TextUsername.setText(username);

        holder.btnUnfollow.setOnClickListener(v -> {
            if (unfollowListener != null) {
                unfollowListener.onUnfollow(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followedUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TextUsername;
        Button btnUnfollow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            TextUsername = itemView.findViewById(R.id.textFollowedUserName);
            btnUnfollow = itemView.findViewById(R.id.btnUnfollow);
        }
    }
}
