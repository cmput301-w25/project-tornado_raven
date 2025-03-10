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
/**
 * Adapter for displaying a list of users that the current user is following in a RecyclerView.
 * Each item represents a followed user with their name, mood, and an option to unfollow them.
 */
public class UsersFollowedAdapter extends RecyclerView.Adapter<UsersFollowedAdapter.UserFollowedViewHolder> {

    private List<String> followedUsersList;
    private OnUnfollowClickListener onUnfollowClickListener;
    /**
     * Constructor for the UsersFollowedAdapter.
     *
     * @param followedUsersList The list of followed users to be displayed.
     * @param listener The listener to handle unfollow actions.
     */
    public UsersFollowedAdapter(List<String> followedUsersList, OnUnfollowClickListener listener) {
        this.followedUsersList = followedUsersList;
        this.onUnfollowClickListener = listener;
    }

    @NonNull
    @Override
    public UserFollowedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each followed user item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_followed, parent, false);
        return new UserFollowedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserFollowedViewHolder holder, int position) {
        String userName = followedUsersList.get(position);
        // Set the user name and mood text
        holder.textFollowedUserName.setText(userName);
        holder.textFollowedUserMood.setText("Feeling Great!");
        // Set the OnClickListener for the unfollow button
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
    /**
     * ViewHolder for binding the views in each item of the RecyclerView.
     */
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
    /**
     * Interface for handling the unfollow action when the user clicks the unfollow button.
     */
    public interface OnUnfollowClickListener {
        /**
         * Called when the unfollow button is clicked.
         *
         * @param position The position of the item in the list.
         */
        void onUnfollowClick(int position);
    }
}

