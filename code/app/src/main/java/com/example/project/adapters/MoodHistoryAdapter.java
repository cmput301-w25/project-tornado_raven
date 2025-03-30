package com.example.project.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.EmotionData;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.activities.EditMoodActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of mood history events in a RecyclerView.
 * Each item represents a MoodEvent with its emotion, date, reason, social situation, and location.
 * Provides functionality to edit, delete, and view detailed mood information.
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> originalList;

    private boolean isOwnProfile;

    private Context context;

    /**
     *
     * @param context The context where tha adapter is used.
     * @param moodHistoryList The list of mood events to display.
     * @param isOwnProfile Flag indicating whether the displayed moods belong to the logged-in user.
     */
    public MoodHistoryAdapter(Context context, List<MoodEvent> moodHistoryList, boolean isOwnProfile) {
        this.context = context;
        this.moodHistoryList = new ArrayList<>(moodHistoryList);
        this.originalList = new ArrayList<>(moodHistoryList);
        this.isOwnProfile = isOwnProfile;
    }


    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new viewHolder for the mood item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each mood item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(view);
    }

    /**
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent moodEvent = moodHistoryList.get(position);
        holder.emotion.setText(moodEvent.getEmotion().toString());
        holder.date.setText(moodEvent.getDate().toString());
        String reason = moodEvent.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = "null";
        }
        holder.reason.setText("Reason: " + reason);
        String social = moodEvent.getSocialSituation().toString();
        if (social == null || social.trim().isEmpty()) {
            social = "null";
        }
        holder.social.setText("Social Situation: " + social);
        int emotionColor = EmotionData.getEmotionColor(context, moodEvent.getEmotion());
        holder.emotion.setTextColor(emotionColor);
        Drawable emojiDrawable = EmotionData.getEmotionIcon(context, moodEvent.getEmotion());
        holder.emoticon.setImageDrawable(emojiDrawable);
        String location = moodEvent.getLocation();
        if (location == null || location.trim().isEmpty()) {
            location = "null";
        }
        holder.location.setText("Location: " + location);
        if (isOwnProfile) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditMoodActivity.class);
                intent.putExtra("moodEvent", moodEvent);
                ((Activity) context).startActivityForResult(intent, 2); // edit
            });
        } else {
            holder.itemView.setOnClickListener(null); // disable click
            Toast.makeText(context, "You can only edit your own moods.", Toast.LENGTH_SHORT).show();
        }

        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(moodEvent));

        String photoUri = moodEvent.getPhotoUrl();
        if (photoUri != null && !photoUri.trim().isEmpty()) {
            holder.ivPostedImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(photoUri))
                    .into(holder.ivPostedImage);
        } else {
            holder.ivPostedImage.setVisibility(View.GONE);
        }

    }

    /**
     *
     * @return The size of the mood event list.
     */
    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }
    /**
     * Updates the displayed mood event with the provided updated mood event.
     *
     * @param updatedMood The MoodEvent object containing the updated data.
     */
    public void updateMood(MoodEvent updatedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(updatedMood.getId())) {
                moodHistoryList.set(i, updatedMood);
                notifyItemChanged(i);
                break;
            }
        }
    }
    /**
     * Updates the list with a new set of mood events.
     *
     * @param newList The new list of MoodEvent objects to replace the current list.
     */
    public void updateList(List<MoodEvent> newList) {
        moodHistoryList.clear();
        moodHistoryList.addAll(newList);
        notifyDataSetChanged();
    }
    /**
     * Resets the list to show the full, unfiltered list of mood events.
     */
    public void resetList() {
        moodHistoryList.clear();
        moodHistoryList.addAll(originalList);
        notifyDataSetChanged();
    }
    /**
     * Deletes a mood event from the list.
     *
     * @param deletedMood The MoodEvent object to be removed from the list.
     */
    public void deleteMood(MoodEvent deletedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(deletedMood.getId())) {
                moodHistoryList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    /**
     * Adds a new mood event at the top of the list.
     *
     * @param newMood The new MoodEvent to be added.
     */
    public void addMood(MoodEvent newMood) {
        moodHistoryList.add(0, newMood);
        notifyItemInserted(0);
    }

    /**
     * ViewHolder for binding the views in each item of the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView emotion;
        public TextView date;
        public TextView reason;
        public TextView social;
        private TextView location;

        public Button detailsButton;
        public ImageView emoticon , ivPostedImage;


        public ViewHolder(View itemView) {
            super(itemView);
            emotion=itemView.findViewById(R.id.emotion);
            reason=itemView.findViewById(R.id.reason);
            emoticon=itemView.findViewById(R.id.emoticon);
            date = itemView.findViewById(R.id.date);
            social=itemView.findViewById(R.id.postedBy);
            location = itemView.findViewById(R.id.location);
            ivPostedImage=itemView.findViewById(R.id.imageView);
            detailsButton = itemView.findViewById(R.id.btnDetails);
        }
    }

    /**
     * Displays a dialog showing the detailed information of a MoodEvent.
     *
     * @param moodEvent The MoodEvent whose details should be shown in the dialog.
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
