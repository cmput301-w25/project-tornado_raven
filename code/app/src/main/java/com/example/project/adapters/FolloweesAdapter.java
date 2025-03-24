package com.example.project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FolloweesAdapter extends RecyclerView.Adapter<FolloweesAdapter.FolloweeViewHolder> {

    private List<String> followees;
    private Context context;
    private String currentUsername;

    public FolloweesAdapter(Context context, List<String> followees, String currentUsername) {
        this.context = context;
        this.followees = followees;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public FolloweeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_following_user, parent, false);
        return new FolloweeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolloweeViewHolder holder, int position) {
        String followee = followees.get(position);
        holder.textView.setText(followee);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.project.activities.ProfileActivity.class);
            intent.putExtra("userName", followee);
            context.startActivity(intent);
        });

        holder.btnUnfollow.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Follows")
                    .whereEqualTo("followerUsername", currentUsername)
                    .whereEqualTo("followedUsername", followee)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            db.collection("Follows").document(doc.getId()).delete();
                        }

                        // delete followee in local list
                        followees.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Unfollowed " + followee, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Unfollow failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }


    @Override
    public int getItemCount() {
        return followees.size();
    }

    static class FolloweeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button btnUnfollow;

        FolloweeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textFolloweeName);
            btnUnfollow = itemView.findViewById(R.id.btnRemoveFollowee);
        }
    }
}

