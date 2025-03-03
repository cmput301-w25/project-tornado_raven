package com.example.project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;

import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private List<MoodEvent> moodHistoryList;
    private Context context;

    public MoodHistoryAdapter(Context context, List<MoodEvent> moodHistoryList) {
        this.context = context;
        this.moodHistoryList = moodHistoryList;
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
        holder.reason.setText(moodEvent.getTrigger());
        holder.social.setText(moodEvent.getSocialSituation());


        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(moodEvent));
    }

    @Override
    public int getItemCount() {
        return moodHistoryList.size();
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

        // toString
        String message = "Emotion: " + moodEvent.getEmotion().toString() + "\n"
                + "Date: " + moodEvent.getDate().toString() + "\n"
                + "Reason: " + moodEvent.getTrigger() + "\n"
                + "Social Situation: " + moodEvent.getSocialSituation();

        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
