package com.example.project.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<MoodEvent> originalList; // Stores full list for filtering

    private Context context;
    /**
     * Constructor for the MoodHistoryAdapter.
     *
     * @param context The context for the adapter, typically an Activity or Fragment.
     * @param moodHistoryList The list of MoodEvent objects to be displayed.
     */
    public MoodHistoryAdapter(Context context, List<MoodEvent> moodHistoryList) {
        this.context = context;
        this.moodHistoryList = new ArrayList<>(moodHistoryList);
        this.originalList = new ArrayList<>(moodHistoryList); // Preserve full list

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each mood item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent moodEvent = moodHistoryList.get(position);
        holder.emotion.setText(moodEvent.getEmotion().toString());
        holder.date.setText(moodEvent.getDate().toString());
        holder.reason.setText(moodEvent.getReason());
        holder.social.setText(moodEvent.getSocialSituation().toString());
        int emotionColor = EmotionData.getEmotionColor(context, moodEvent.getEmotion());
        holder.emotion.setTextColor(emotionColor);
        Drawable emojiDrawable = EmotionData.getEmotionIcon(context, moodEvent.getEmotion());
        holder.emoticon.setImageDrawable(emojiDrawable);
        holder.location.setText(moodEvent.getLocation());
        // Set up the onClickListener for editing mood event
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditMoodActivity.class);
            intent.putExtra("moodEvent", moodEvent);
            ((Activity) context).startActivityForResult(intent, 2); //edit
        });
        // Set up the button to show detailed mood event information in a dialog
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(moodEvent));
    }

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
        moodHistoryList.add(0, newMood); // Add new mood at the top
        notifyItemInserted(0); // Notify RecyclerView to refresh UI
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
        public ImageView emoticon;


        public ViewHolder(View itemView) {
            super(itemView);
            emotion=itemView.findViewById(R.id.emotion);
            reason=itemView.findViewById(R.id.reason);
            emoticon=itemView.findViewById(R.id.emoticon);
            date = itemView.findViewById(R.id.date);
            social=itemView.findViewById(R.id.postedBy);
            location = itemView.findViewById(R.id.location);
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

        message.append("Emotion: ").append(moodEvent.getEmotion().toString()).append("\n")
                .append("Date: ").append(moodEvent.getDate().toString()).append("\n")
                .append("Reason: ").append(moodEvent.getReason()).append("\n")
                .append("Location:").append(moodEvent.getLocation()).append("\n")
                .append("Social Situation: ").append(moodEvent.getSocialSituation());


        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
