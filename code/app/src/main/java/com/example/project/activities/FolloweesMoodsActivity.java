package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
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

    private Button btnShowLastWeek, btnFilterByMood, btnFilterByKeyword, btnClearFilters;
    private List<FolloweesMoodsAdapter.UserMoodItem> originalUserMoodItems = new ArrayList<>();


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

        btnShowLastWeek    = findViewById(R.id.btnShowLastWeekFlw);
        btnFilterByMood    = findViewById(R.id.btnFilterByMoodFlw);
        btnFilterByKeyword = findViewById(R.id.btnFilterByKeywordFlw);
        btnClearFilters    = findViewById(R.id.btnClearFiltersFlw);

// Set listeners
        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showKeywordFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());


        // Load from the "Follows" collection
        loadFolloweesMoods();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_followees_moods);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    private void filterByLastWeek() {
        userMoodItems.clear();
        long oneWeekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        for (FolloweesMoodsAdapter.UserMoodItem item : originalUserMoodItems) {
            if (item.moodEvent.getDate() != null &&
                    item.moodEvent.getDate().getTime() >= oneWeekAgo) {
                userMoodItems.add(item);
            }
        }
        followeesMoodsAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Filtered: last week", Toast.LENGTH_SHORT).show();
    }

    private void filterByMood(Emotion selectedEmotion) {
        userMoodItems.clear();
        for (FolloweesMoodsAdapter.UserMoodItem item : originalUserMoodItems) {
            if (item.moodEvent.getEmotion() == selectedEmotion) {
                userMoodItems.add(item);
            }
        }
        followeesMoodsAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Filtered by mood: " + selectedEmotion, Toast.LENGTH_SHORT).show();
    }

    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER", "CONFUSION", "DISGUST", "FEAR", "HAPPINESS", "SADNESS", "SHAME", "SURPRISE", "CLEAR FILTER"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Mood to Filter")
                .setItems(moods, (dialog, which) -> {
                    if (moods[which].equals("CLEAR FILTER")) {
                        clearFilters();
                    } else {
                        filterByMood(Emotion.valueOf(moods[which]));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showKeywordFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter keyword for reason");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Filter", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                filterByKeyword(keyword);
            } else {
                Toast.makeText(this, "Enter a keyword", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void filterByKeyword(String keyword) {
        userMoodItems.clear();
        String lowerKeyword = keyword.toLowerCase();
        for (FolloweesMoodsAdapter.UserMoodItem item : originalUserMoodItems) {
            String reason = item.moodEvent.getReason();
            if (reason != null && reason.toLowerCase().contains(lowerKeyword)) {
                userMoodItems.add(item);
            }
        }
        followeesMoodsAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Filtered by keyword: " + keyword, Toast.LENGTH_SHORT).show();
    }

    private void clearFilters() {
        userMoodItems.clear();
        userMoodItems.addAll(originalUserMoodItems);
        followeesMoodsAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Cleared all filters", Toast.LENGTH_SHORT).show();
    }


    /**
     * Finds all docs in "Follows" where followerUsername == currentUser,
     * then loads up to 3 moods for each followedUsername from "MoodEvents".
     */
    private void loadFolloweesMoods() {
        userMoodItems.clear();
        originalUserMoodItems.clear();


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
                            Toast.makeText(this,followedUser,Toast.LENGTH_SHORT);
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
                                                        FolloweesMoodsAdapter.UserMoodItem item = new FolloweesMoodsAdapter.UserMoodItem(followedUser, me);
                                                        userMoodItems.add(item);
                                                        originalUserMoodItems.add(item);
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
