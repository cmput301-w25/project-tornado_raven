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


/**
 * adapter for displaying the users that the current user is following.
 * provide functionality to view their profile and also unfollow them.
 */
public class FolloweesAdapter extends RecyclerView.Adapter<FolloweesAdapter.FolloweeViewHolder> {

    private List<String> followees;
    private Context context;
    private String currentUsername;

    /**
     *
     * @param context The context where the adapter is used.
     * @param followees The list of usernames the user is following.
     * @param currentUsername The username of the currently logged-in user.
     */
    public FolloweesAdapter(Context context, List<String> followees, String currentUsername) {
        this.context = context;
        this.followees = followees;
        this.currentUsername = currentUsername;
    }


    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new instance of FolloweeViewHolder.
     */
    @NonNull
    @Override
    public FolloweeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_following_user, parent, false);
        return new FolloweeViewHolder(view);
    }

    /**
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(@NonNull FolloweeViewHolder holder, int position) {
        String followee = followees.get(position);
        holder.textView.setText(followee);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.project.activities.ProfileActivity.class);
            intent.putExtra("userName", followee);
            context.startActivity(intent);
        });
        // unfollow btn logic.
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

                        followees.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Unfollowed " + followee, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Unfollow failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }


    /**
     *
     * @return the number of followees.
     */
    @Override
    public int getItemCount() {
        return followees.size();
    }

    /**
     * ViewHolder for holding views related to a followee item.
     */
    static class FolloweeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button btnUnfollow;

        /**
         *
         * @param itemView the item view representing a single followee.
         */
        FolloweeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textFolloweeName);
            btnUnfollow = itemView.findViewById(R.id.btnRemoveFollowee);
        }
    }
}

