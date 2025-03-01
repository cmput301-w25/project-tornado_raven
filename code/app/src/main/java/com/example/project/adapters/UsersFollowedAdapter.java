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

public class UsersFollowedAdapter extends RecyclerView.Adapter<UsersFollowedAdapter.UserFollowedViewHolder> {

    private List<String> followedUsersList;
    private OnUnfollowClickListener onUnfollowClickListener;

    public UsersFollowedAdapter(List<String> followedUsersList, OnUnfollowClickListener listener) {
        this.followedUsersList = followedUsersList;
        this.onUnfollowClickListener = listener;
    }

    @NonNull
    @Override
    public UserFollowedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_followed, parent, false);
        return new UserFollowedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserFollowedViewHolder holder, int position) {
        String userName = followedUsersList.get(position);

        holder.textFollowedUserName.setText(userName);
        holder.textFollowedUserMood.setText("Feeling Great!");

        holder.btnUnfollow.setOnClickListener(v -> {
            if (onUnfollowClickListener != null) {
                onUnfollowClickListener.onUnfollowClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followedUsersList.size();
    }

    public static class UserFollowedViewHolder extends RecyclerView.ViewHolder {
        TextView textFollowedUserName, textFollowedUserMood;
        Button btnUnfollow;

        public UserFollowedViewHolder(@NonNull View itemView) {
            super(itemView);
            textFollowedUserName = itemView.findViewById(R.id.textFollowedUserName);
            textFollowedUserMood = itemView.findViewById(R.id.textFollowedUserMood);
            btnUnfollow = itemView.findViewById(R.id.btnUnfollow);
        }
    }

    public interface OnUnfollowClickListener {
        void onUnfollowClick(int position);
    }
}

