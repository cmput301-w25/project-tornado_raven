package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a simple list of followees or followed mood data.
 * Each item can show "Username" + "Unfollow" (or other logic).
 */
public class FolloweesAdapter extends RecyclerView.Adapter<FolloweesAdapter.FolloweeViewHolder> {

    private List<String> originalList;
    private List<String> displayedList; // Filtered data

    private OnItemActionListener itemActionListener;

    public FolloweesAdapter(List<String> dataList, OnItemActionListener listener) {
        /*this.dataList = dataList;*/
        this.originalList = new ArrayList<>(dataList); // Keep a copy of all moods
        this.displayedList = new ArrayList<>(dataList); // This is the list that changes on filtering
        this.itemActionListener = listener;

    }

    @NonNull
    @Override
    public FolloweeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse item_user_followed.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_followed, parent, false);
        return new FolloweeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolloweeViewHolder holder, int position) {
        String data = originalList.get(position);

        // If data is "Username: Mood", let's parse it
        // or we simply show "Username" as first word
        // This is just a simple demonstration:
        if (data.contains(":")) {
            String[] parts = data.split(":");
            holder.textFollowedUserName.setText(parts[0].trim());

            if (parts.length > 1) {
                holder.textFollowedUserMood.setText(parts[1].trim());
            } else {
                holder.textFollowedUserMood.setText("(No mood provided)");
            }
        } else {
            // If there's no colon, treat entire string as a username
            holder.textFollowedUserName.setText(data);
            holder.textFollowedUserMood.setText("(No mood provided)");
        }

        // "Unfollow" or "Remove" button
        holder.btnUnfollow.setOnClickListener(v -> {
            if (itemActionListener != null) {
                itemActionListener.onItemAction(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    /**
     * Updates the list dynamically when filtering by Mood or Keyword.
     */
    public void updateList(List<String> newList) {
        displayedList.clear();
        displayedList.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * Resets the list to show all moods.
     */
    public void resetList() {
        displayedList.clear();
        displayedList.addAll(originalList);
        notifyDataSetChanged();
    }
    public static class FolloweeViewHolder extends RecyclerView.ViewHolder {
        TextView textFollowedUserName, textFollowedUserMood;
        Button btnUnfollow;

        public FolloweeViewHolder(@NonNull View itemView) {
            super(itemView);
            textFollowedUserName = itemView.findViewById(R.id.textFollowedUserName);
            textFollowedUserMood = itemView.findViewById(R.id.textFollowedUserMood);
            btnUnfollow = itemView.findViewById(R.id.btnUnfollow);
        }
    }

    public interface OnItemActionListener {
        void onItemAction(int position);
    }
}
