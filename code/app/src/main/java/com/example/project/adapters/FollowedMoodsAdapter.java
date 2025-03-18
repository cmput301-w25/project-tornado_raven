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
 * For the "Followed Moods" screen.
 * Each item can be "Unfollow"ed individually.
 */
public class FollowedMoodsAdapter extends RecyclerView.Adapter<FollowedMoodsAdapter.MoodViewHolder> {

    private List<String> followedMoods;
    private OnUnfollowListener unfollowListener;

    /**
     * Interface to handle unfollow actions for a mood event.
     */
    public interface OnUnfollowListener {
        void onUnfollow(int position);
    }

    /**
     * Constructor for the adapter.
     *
     * @param followedMoods List of followed mood data, each in the format "Emotion|Date|Reason|Social".
     * @param unfollowListener Listener for unfollow button clicks.
     */
    public FollowedMoodsAdapter(List<String> followedMoods, OnUnfollowListener unfollowListener) {
        this.followedMoods = followedMoods;
        this.unfollowListener = unfollowListener;
    }

    /**
     * Creates a new ViewHolder instance. This is called when a new view item is needed.
     *
     * @param parent The parent ViewGroup that the new item will be added to.
     * @param viewType The view type of the new item.
     * @return A new instance of MoodViewHolder.
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);
    }

    /**
     * Binds the data for the specific position to the corresponding ViewHolder.
     * This is called for each item in the RecyclerView.
     *
     * @param holder The ViewHolder to bind the data to.
     * @param position The position of the item in the followedMoods list.
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        String data = followedMoods.get(position);
        // Format: "Emotion|Date|Reason|Social"
        String[] parts = data.split("\\|");
        String emotion = parts.length > 0 ? parts[0].trim() : "Unknown";
        String date = parts.length > 1 ? parts[1].trim() : "N/A";
        String reason = parts.length > 2 ? parts[2].trim() : "No reason";
        String social = parts.length > 3 ? parts[3].trim() : "No situation";

        holder.emotion.setText(emotion);
        holder.date.setText(date);
        holder.reason.setText(reason);
        holder.socialSituation.setText(social);

        // Button says 'Unfollow'
        holder.btnDetails.setText("Unfollow");
        holder.btnDetails.setOnClickListener(v -> {
            if (unfollowListener != null) {
                unfollowListener.onUnfollow(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followedMoods.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView emotion, date, reason, socialSituation;
        Button btnDetails;

        /**
         * Constructor that initializes the views for each item.
         *
         * @param itemView The view of the item to be bound.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            emotion = itemView.findViewById(R.id.emotion);
            date = itemView.findViewById(R.id.date);
            reason = itemView.findViewById(R.id.reason);
            socialSituation = itemView.findViewById(R.id.socialSituation);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}

