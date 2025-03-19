package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;

import java.util.List;

/**
 * Shows a list of (username + a single mood).
 * We'll combine them so the user sees the 3 most recent moods for each followee.
 */
public class FolloweesMoodsAdapter extends RecyclerView.Adapter<FolloweesMoodsAdapter.ViewHolder> {

    private List<UserMoodItem> userMoodItems;

    public FolloweesMoodsAdapter(List<UserMoodItem> items) {
        this.userMoodItems = items;
    }

    @NonNull
    @Override
    public FolloweesMoodsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the same "item_mood" layout or your own
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolloweesMoodsAdapter.ViewHolder holder, int position) {
        UserMoodItem item = userMoodItems.get(position);
        // item.userId is the user who posted
        // item.moodEvent is the mood
        holder.txtUsername.setText("User: " + item.userId);  // or fetch user name if you have it
        holder.txtEmotion.setText(item.moodEvent.getEmotion().toString());
        holder.txtDate.setText(item.moodEvent.getDate().toString());
        holder.txtReason.setText("Reason: " + item.moodEvent.getReason());
        holder.txtSocial.setText("Social: " + item.moodEvent.getSocialSituation());
    }

    @Override
    public int getItemCount() {
        return userMoodItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtEmotion, txtDate, txtReason, txtSocial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // We'll re-purpose "item_mood" but we need to find or add a username label
            txtUsername = itemView.findViewById(R.id.emotion); // or create a new ID in the layout
            txtEmotion  = itemView.findViewById(R.id.date);
            txtDate     = itemView.findViewById(R.id.reason);
            txtReason   = itemView.findViewById(R.id.socialSituation);
            // implement when location is available: txtSocial   = itemView.findViewById(R.id.location);
        }
    }

    /**
     * Simple struct for storing: which user posted + the MoodEvent itself
     */
    public static class UserMoodItem {
        public String userId;
        public MoodEvent moodEvent;
        public UserMoodItem(String userId, MoodEvent moodEvent) {
            this.userId = userId;
            this.moodEvent = moodEvent;
        }
    }
}

