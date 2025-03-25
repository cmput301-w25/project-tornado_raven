package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.FolloweesMoodsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Displays up to three most recent moods of each user that the current user follows,
 * reading from the "Follows" collection (where 'followerUsername' == current user).
 */
public class FolloweesMoodsActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private FolloweesMoodsAdapter followeesMoodsAdapter;
    private List<FolloweesMoodsAdapter.UserMoodItem> userMoodItems = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followees_moods);

        db = FirebaseFirestore.getInstance();

        recyclerFollowees = findViewById(R.id.recyclerFollowees);
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));

        followeesMoodsAdapter = new FolloweesMoodsAdapter(userMoodItems);
        recyclerFollowees.setAdapter(followeesMoodsAdapter);

        // Load from the "Follows" collection
        loadFolloweesMoods();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_followees_moods);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    /**
     * Finds all docs in "Follows" where followerUsername == currentUser,
     * then loads up to 3 moods for each followedUsername from "MoodEvents".
     */
    private void loadFolloweesMoods() {
        userMoodItems.clear();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", null);
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Follows")
                .whereEqualTo("followerUsername", currentUser)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        for (int i = 0; i < snap.size(); i++) {
                            String followedUser = snap.getDocuments().get(i).getString("followedUsername");
                            if (followedUser != null) {
                                // Now load that user's last 3 moods
                                db.collection("MoodEvents")
                                        .whereEqualTo("author", followedUser)
                                        .whereIn("privacyLevel", Arrays.asList("ALL_USERS", "FOLLOWERS_ONLY"))
                                        .orderBy("date", Query.Direction.DESCENDING)
                                        .limit(3)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            if (!querySnapshot.isEmpty()) {
                                                for (int j = 0; j < querySnapshot.size(); j++) {
                                                    MoodEvent me = querySnapshot.getDocuments().get(j)
                                                            .toObject(MoodEvent.class);
                                                    if (me != null) {
                                                        userMoodItems.add(
                                                                new FolloweesMoodsAdapter.UserMoodItem(followedUser, me)
                                                        );
                                                    }
                                                }
                                                followeesMoodsAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this,
                                                        "Error loading moods for " + followedUser + ": " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show()
                                        );
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading followees: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_followees_moods) {
            // Already here
            return true;
        } else if (id == R.id.nav_following_users) {
            startActivity(new Intent(this, FollowingUsersActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_mood_map) {
            startActivity(new Intent(this, mood_mapActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
