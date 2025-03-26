package com.example.project.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.EmotionData;
import com.example.project.MoodEvent;
import com.example.project.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying the up to 3 most recent moods for each user that I follow.
 */
public class FolloweesMoodsAdapter extends RecyclerView.Adapter<FolloweesMoodsAdapter.ViewHolder> {

    public static class UserMoodItem {
        public String userName;     // The user who posted
        public MoodEvent moodEvent; // The mood
        public UserMoodItem(String userName, MoodEvent moodEvent) {
            this.userName = userName;
            this.moodEvent = moodEvent;
        }
    }

    private List<UserMoodItem> userMoodItems;

    public FolloweesMoodsAdapter(List<UserMoodItem> items) {
        this.userMoodItems = items;
    }

    @NonNull
    @Override
    public FolloweesMoodsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolloweesMoodsAdapter.ViewHolder holder, int position) {
        UserMoodItem item = userMoodItems.get(position);
        MoodEvent mood = item.moodEvent;

        // Show the user who posted
        holder.txtUsername.setText("User: " + item.userName);

        // Show emotion
        holder.txtEmotion.setText(mood.getEmotion().toString());
        int color = EmotionData.getEmotionColor(holder.itemView.getContext(), mood.getEmotion());
        holder.txtEmotion.setTextColor(color);

        // Icon
        Drawable icon = EmotionData.getEmotionIcon(holder.itemView.getContext(), mood.getEmotion());
        holder.imgIcon.setImageDrawable(icon);

        // Show date
        if (mood.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.txtDate.setText(sdf.format(mood.getDate()));
        } else {
            holder.txtDate.setText("");
        }

        // Show reason
        if (mood.getReason() != null) {
            holder.txtReason.setText("Reason: " + mood.getReason());
        } else {
            holder.txtReason.setText("");
        }
        // show location
        String location = mood.getLocation();
        if (location == null || location.trim().isEmpty()) {
            location = "null";
        }
        holder.tvLocation.setText("Location: " + location);

//        // Show social situation
//        if (mood.getSocialSituation() != null) {
//            holder.txtSocial.setText("Social: " + mood.getSocialSituation());
//        } else {
//            holder.txtSocial.setText("");
//        }
    }

    @Override
    public int getItemCount() {
        return userMoodItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtEmotion, txtDate, txtReason, tvLocation;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Because item_mood.xml has fields: emotion, date, reason, socialSituation, emoticon
            txtUsername = itemView.findViewById(R.id.postedBy);
            txtEmotion  = itemView.findViewById(R.id.emotion);
            txtDate     = itemView.findViewById(R.id.date);
            txtReason   = itemView.findViewById(R.id.reason);
            tvLocation=itemView.findViewById(R.id.location);
            imgIcon     = itemView.findViewById(R.id.emoticon);


        }
    }
}
