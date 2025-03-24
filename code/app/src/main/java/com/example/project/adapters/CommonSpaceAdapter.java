package com.example.project.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.EmotionData;
import com.example.project.MoodEvent;
import com.example.project.R;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Updated to handle "requested" state for authors in 'pendingAuthors'.
 */
public class CommonSpaceAdapter extends RecyclerView.Adapter<CommonSpaceAdapter.ViewHolder> {

    public interface OnRequestFollowListener {
        // We'll pass the MoodEvent + the button reference, so you can do logic
        void onRequestFollow(MoodEvent mood, Button button);
    }
    private String currentUsername;
    private List<MoodEvent> moodList;
    private OnRequestFollowListener followListener;
    // We'll store a set of authors for which a follow request is already sent
    private Set<String> pendingAuthors;
    private Set<String> followedAuthors = new HashSet<>();

    public CommonSpaceAdapter(List<MoodEvent> moodList,
                              OnRequestFollowListener followListener,
                              Set<String> pendingAuthors) {
        this.moodList = moodList;
        this.followListener = followListener;
        this.pendingAuthors = pendingAuthors;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @NonNull
    @Override
    public CommonSpaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonSpaceAdapter.ViewHolder holder, int position) {
        MoodEvent mood = moodList.get(position);

        holder.tvEmotion.setText(mood.getEmotion().toString());
        int color = EmotionData.getEmotionColor(holder.itemView.getContext(), mood.getEmotion());
        holder.tvEmotion.setTextColor(color);

        Drawable icon = EmotionData.getEmotionIcon(holder.itemView.getContext(), mood.getEmotion());
        holder.ivEmoticon.setImageDrawable(icon);

        if (mood.getReason() != null) {
            holder.tvReason.setText(mood.getReason());
        } else {
            holder.tvReason.setText("");
        }

        if (mood.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(mood.getDate()));
        } else {
            holder.tvDate.setText("");
        }

        String author = mood.getAuthor();
        holder.tvSocial.setText("Posted by: " + (author == null ? "Unknown" : author));

        if (author == null || author.equals(currentUsername)) {
            holder.btnFollow.setVisibility(View.GONE);
            return;
        }

        // check button situation：
        if (author == null) {
            holder.btnFollow.setVisibility(View.GONE);
            return;
        }

        if (followedAuthors.contains(author)) {
            // following
            holder.btnFollow.setText("Following");
            holder.btnFollow.setEnabled(true);
            holder.btnFollow.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "You are following this user!", Toast.LENGTH_SHORT).show();
            });
        } else if (pendingAuthors.contains(author)) {
            // requested
            holder.btnFollow.setText("Requested");
            holder.btnFollow.setEnabled(false);
        } else {
            // can request
            holder.btnFollow.setText("Request Follow");
            holder.btnFollow.setEnabled(true);
            holder.btnFollow.setOnClickListener(v -> {
                if (followListener != null) {
                    followListener.onRequestFollow(mood, holder.btnFollow);
                }
            });
        }

        // click the item to show details
        holder.itemView.setOnClickListener(v -> showDetailsDialog(v.getContext(), mood));

    }

    private void showDetailsDialog(Context context, MoodEvent moodEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Mood Details");

        StringBuilder message = new StringBuilder();
        message.append("Emotion: ").append(moodEvent.getEmotion().toString()).append("\n")
                .append("Date: ").append(moodEvent.getDate()).append("\n")
                .append("Reason: ").append(moodEvent.getReason()).append("\n")
                .append("Location: ").append(moodEvent.getLocation()).append("\n")
                .append("Social Situation: ").append(moodEvent.getSocialSituation());

        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public void setFollowedAuthors(Set<String> followedAuthors) {
        this.followedAuthors = followedAuthors;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmotion, tvDate, tvReason, tvSocial;
        ImageView ivEmoticon;
        Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmotion  = itemView.findViewById(R.id.emotion);
            tvDate     = itemView.findViewById(R.id.date);
            tvReason   = itemView.findViewById(R.id.reason);
            tvSocial   = itemView.findViewById(R.id.socialSituation);
            ivEmoticon = itemView.findViewById(R.id.emoticon);

            // Reuse the existing button ID or add a new one
            btnFollow = itemView.findViewById(R.id.btnDetails);
        }
    }
}
