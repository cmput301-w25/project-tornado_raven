package com.example.project.adapters;

import static java.util.logging.Level.parse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.project.EmotionData;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.activities.CommentActivity;
import com.example.project.activities.EditMoodActivity;

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
        String author = mood.getAuthor();

        holder.tvEmotion.setText(mood.getEmotion().toString());
        int color = EmotionData.getEmotionColor(holder.itemView.getContext(), mood.getEmotion());
        holder.tvEmotion.setTextColor(color);

        Drawable icon = EmotionData.getEmotionIcon(holder.itemView.getContext(), mood.getEmotion());
        holder.ivEmoticon.setImageDrawable(icon);

        //holder.tvReason.setText(mood.getReason() != null ? mood.getReason() : "");
        String r = mood.getReason();
        if (r == null || r.trim().isEmpty()) {
            r = "null";
        }
        holder.tvReason.setText("Reason: " + r);
        holder.tvDate.setText(mood.getDate() != null ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(mood.getDate())
                : "");

        holder.tvSocial.setText("Posted by: " + (author == null ? "Unknown" : author));
        String location = mood.getLocation();
        if (location == null || location.trim().isEmpty()) {
            location = "null";
        }
        holder.tvLocation.setText("Location: " + location);
        // goto default button situation firstly
        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.btnFollow.setEnabled(true);
        holder.btnFollow.setText("Request Follow");
        holder.btnFollow.setOnClickListener(null); // clear old listener

        // button situation
        if (author == null || author.equals(currentUsername)) {
            holder.btnFollow.setVisibility(View.GONE);
        } else if (followedAuthors.contains(author)) {
            holder.btnFollow.setText("Following");
            holder.btnFollow.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(),
                        "You are following this user!", Toast.LENGTH_SHORT).show();
            });
        } else if (pendingAuthors.contains(author)) {
            holder.btnFollow.setText("Requested");
            holder.btnFollow.setEnabled(false);
        } else {
            // can send request
            holder.btnFollow.setText("Request Follow");
            holder.btnFollow.setEnabled(true);
            holder.btnFollow.setOnClickListener(v -> {
                if (followListener != null) {
                    followListener.onRequestFollow(mood, holder.btnFollow);
                }
            });
        }
        // details or comments if click on item
        holder.itemView.setOnClickListener(v -> showDetailsDialog(v.getContext(), mood));//details
        // view/post comments
        holder.itemView.setOnLongClickListener(v -> {
            if (author != null && author.equals(currentUsername)) {
                Intent intent = new Intent(holder.itemView.getContext(), EditMoodActivity.class);
                intent.putExtra("moodEvent", mood);
                ((Activity) holder.itemView.getContext()).startActivityForResult(intent, 100);
            }
            return true;
        });
        String photoUrl = mood.getPhotoUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            holder.ivPostedImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(photoUrl)
                    .into(holder.ivPostedImage);
        } else {
            holder.ivPostedImage.setVisibility(View.GONE);
        }



    }


    private void showDetailsDialog(Context context, MoodEvent moodEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Mood Details");
        String location = moodEvent.getLocation();

        StringBuilder message = new StringBuilder();
        message.append("Emotion: ").append(moodEvent.getEmotion().toString()).append("\n")
                .append("Date: ").append(moodEvent.getDate()).append("\n")
                //.append("Reason: ").append(moodEvent.getReason()).append("\n")
                .append("Reason: ")
                .append(moodEvent.getReason() != null && !moodEvent.getReason().isEmpty() ? moodEvent.getReason() : "null")
                .append("\n");
                //.append("Location: ").append(moodEvent.getLocation()).append("\n")
        if (location == null || location.trim().isEmpty()) {
            message.append("Location: null\n");
        } else {
            message.append("Location: ").append(location).append("\n");
        }
        message.append("Social Situation: ").append(moodEvent.getSocialSituation());

        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Comments", (dialog, which) -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("MOOD_EVENT_ID", moodEvent.getId());
            context.startActivity(intent);
        });
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
        TextView tvEmotion, tvDate, tvReason, tvSocial,tvLocation;
        ImageView ivEmoticon, ivPostedImage;
        Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmotion  = itemView.findViewById(R.id.emotion);
            tvDate     = itemView.findViewById(R.id.date);
            tvReason   = itemView.findViewById(R.id.reason);
            tvSocial   = itemView.findViewById(R.id.postedBy);
            ivEmoticon = itemView.findViewById(R.id.emoticon);
            tvLocation=itemView.findViewById(R.id.location);
            ivPostedImage=itemView.findViewById(R.id.imageView);
            // Reuse the existing button ID or add a new one
            btnFollow = itemView.findViewById(R.id.btnDetails);
        }
    }
}
