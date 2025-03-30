package com.example.project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    /**
     * represent a mood posted by a user that the current user follow.
     */
    public static class UserMoodItem {
        public String userName;
        public MoodEvent moodEvent;

        /**
         *
         * @param userName The username of the mood poster.
         * @param moodEvent The mood posted.
         */
        public UserMoodItem(String userName, MoodEvent moodEvent) {
            this.userName = userName;
            this.moodEvent = moodEvent;
        }
    }

    private List<UserMoodItem> userMoodItems;
    private Context context;


    /**
     *
     * @param context The context where the adapter is used.
     * @param items The list of userMoodsItems to display.
     */
    public FolloweesMoodsAdapter(Context context,List<UserMoodItem> items) {
        this.userMoodItems = items;
        this.context = context;
    }


    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder for the mood item.
     */
    @NonNull
    @Override
    public FolloweesMoodsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(view);
    }


    /**
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FolloweesMoodsAdapter.ViewHolder holder, int position) {
        UserMoodItem item = userMoodItems.get(position);
        MoodEvent mood = item.moodEvent;

        // Set username.
        holder.txtUsername.setText("User: " + item.userName);

        // set emotion & color.
        holder.txtEmotion.setText(mood.getEmotion().toString());
        int color = EmotionData.getEmotionColor(holder.itemView.getContext(), mood.getEmotion());
        holder.txtEmotion.setTextColor(color);

        // set emotion Icon
        Drawable icon = EmotionData.getEmotionIcon(holder.itemView.getContext(), mood.getEmotion());
        holder.imgIcon.setImageDrawable(icon);

        // set date
        if (mood.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.txtDate.setText(sdf.format(mood.getDate()));
        } else {
            holder.txtDate.setText("");
        }

        // set reason
        if (mood.getReason() != null) {
            holder.txtReason.setText("Reason: " + mood.getReason());
        } else {
            holder.txtReason.setText("");
        }
        // set location
        String location = mood.getLocation();
        if (location == null || location.trim().isEmpty()) {
            location = "null";
        }
        holder.tvLocation.setText("Location: " + location);

        // load image if available.
        String photoUri = mood.getPhotoUrl();
        if (photoUri != null && !photoUri.trim().isEmpty()) {
            holder.ivPostedImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(photoUri))
                    .into(holder.ivPostedImage);
        } else {
            holder.ivPostedImage.setVisibility(View.GONE);
        }
        holder.detailsbtn.setOnClickListener(v -> showDetailsDialog(mood));
    }


    /**
     *
     * @return number of items in the list.
     */
    @Override
    public int getItemCount() {
        return userMoodItems.size();
    }


    /**
     * ViewHolder class for holding views in each mood item layout.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtEmotion, txtDate, txtReason, tvLocation;
        ImageView imgIcon, ivPostedImage;
        Button detailsbtn;

        /**
         *
         * @param itemView the item view layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.postedBy);
            txtEmotion  = itemView.findViewById(R.id.emotion);
            txtDate     = itemView.findViewById(R.id.date);
            txtReason   = itemView.findViewById(R.id.reason);
            tvLocation=itemView.findViewById(R.id.location);
            ivPostedImage=itemView.findViewById(R.id.imageView);
            imgIcon     = itemView.findViewById(R.id.emoticon);
            detailsbtn=itemView.findViewById(R.id.btnDetails);


        }
    }

    /**
     * display detailed dialog with mood information like emotion, date, reason
     * location (if any) and social situation.
     *
     * @param moodEvent the moodEvent to show details of.
     */
    private void showDetailsDialog(MoodEvent moodEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Mood Details");
        StringBuilder message = new StringBuilder();
        String location = moodEvent.getLocation();
        message.append("Emotion: ").append(moodEvent.getEmotion().toString()).append("\n")
                .append("Date: ").append(moodEvent.getDate().toString()).append("\n")
                .append("Reason: ")
                .append(moodEvent.getReason() != null && !moodEvent.getReason().isEmpty() ? moodEvent.getReason() : "null")
                .append("\n");
        if (location == null || location.trim().isEmpty()) {
            message.append("Location: null\n");
        } else {
            message.append("Location: ").append(location).append("\n");
        }
        message.append("Social Situation: ").append(moodEvent.getSocialSituation());


        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
