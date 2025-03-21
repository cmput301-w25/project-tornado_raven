package com.example.project.adapters;

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

import java.text.SimpleDateFormat;
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

    private List<MoodEvent> moodList;
    private OnRequestFollowListener followListener;
    // We'll store a set of authors for which a follow request is already sent
    private Set<String> pendingAuthors;

    public CommonSpaceAdapter(List<MoodEvent> moodList,
                              OnRequestFollowListener followListener,
                              Set<String> pendingAuthors) {
        this.moodList = moodList;
        this.followListener = followListener;
        this.pendingAuthors = pendingAuthors;
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

        // If we've already requested this author, show "Requested"
        if (author != null && pendingAuthors.contains(author)) {
            holder.btnFollow.setText("Requested");
            holder.btnFollow.setEnabled(false);
        } else {
            holder.btnFollow.setText("Request Follow");
            holder.btnFollow.setEnabled(true);
            holder.btnFollow.setOnClickListener(v -> {
                if (followListener != null) {
                    followListener.onRequestFollow(mood, holder.btnFollow);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
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
