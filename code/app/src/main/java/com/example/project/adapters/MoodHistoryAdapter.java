package com.example.project.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.activities.EditMoodActivity;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> originalList; // Stores full list for filtering

    private Context context;

    public MoodHistoryAdapter(Context context, List<MoodEvent> moodHistoryList) {
        this.context = context;
        this.moodHistoryList = new ArrayList<>(moodHistoryList);
        this.originalList = new ArrayList<>(moodHistoryList); // Preserve full list

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditMoodActivity.class);
            intent.putExtra("moodEvent", moodEvent);
            ((Activity) context).startActivityForResult(intent, 2); //edit
        });
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(moodEvent));
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
    }
    // updating event
    public void updateMood(MoodEvent updatedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(updatedMood.getId())) {
                moodHistoryList.set(i, updatedMood);
                notifyItemChanged(i);
                break;
            }
        }
    }
    // ✅ Add method to update list
    public void updateList(List<MoodEvent> newList) {
        moodHistoryList.clear();
        moodHistoryList.addAll(newList);
        notifyDataSetChanged();
    }
    // ✅ Restore full unfiltered list when needed
    public void resetList() {
        moodHistoryList.clear();
        moodHistoryList.addAll(originalList);
        notifyDataSetChanged();
    }
    //deleting event
    public void deleteMood(MoodEvent deletedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(deletedMood.getId())) {
                moodHistoryList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    // ✅ Add new mood at the top of the list
    public void addMood(MoodEvent newMood) {
        moodHistoryList.add(0, newMood); // Add new mood at the top
        notifyItemInserted(0); // Notify RecyclerView to refresh UI
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView emotion;
        public TextView date;
        public TextView reason;
        public TextView social;
        public Button detailsButton;

        public ViewHolder(View itemView) {
            super(itemView);
            emotion=itemView.findViewById(R.id.emotion);
            reason=itemView.findViewById(R.id.reason);
            date = itemView.findViewById(R.id.date);
            social=itemView.findViewById(R.id.socialSituation);
            detailsButton = itemView.findViewById(R.id.btnDetails);
        }
    }


    private void showDetailsDialog(MoodEvent moodEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Mood Details");
        StringBuilder message = new StringBuilder();

        message.append("Emotion: ").append(moodEvent.getEmotion().toString()).append("\n")
                .append("Date: ").append(moodEvent.getDate().toString()).append("\n")
                .append("Reason: ").append(moodEvent.getReason()).append("\n")
                .append("Social Situation: ").append(moodEvent.getSocialSituation());

        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
