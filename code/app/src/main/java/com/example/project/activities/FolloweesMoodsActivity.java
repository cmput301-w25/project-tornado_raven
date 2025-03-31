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

import com.example.project.models.Emotion;
import com.example.project.models.MoodEvent;
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
 * from each user the current user is following.
 * It supports filtering moods by emotion, reason keyword, and date.
 */
public class FolloweesMoodsActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private FolloweesMoodsAdapter followeesMoodsAdapter;
    private List<FolloweesMoodsAdapter.UserMoodItem> userMoodItems = new ArrayList<>();

    private Button btnShowLastWeek, btnFilterByMood, btnFilterByKeyword, btnClearFilters;
    private List<FolloweesMoodsAdapter.UserMoodItem> originalUserMoodItems = new ArrayList<>();


    private FirebaseFirestore db;
    /**
     * Initializes the activity, sets up UI components and listeners,
     * and loads followed users' mood data from Firestore.
     *
     * @param savedInstanceState Previously saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followees_moods);

        db = FirebaseFirestore.getInstance();

        recyclerFollowees = findViewById(R.id.recyclerFollowees);
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));

        followeesMoodsAdapter = new FolloweesMoodsAdapter(FolloweesMoodsActivity.this, userMoodItems);
        recyclerFollowees.setAdapter(followeesMoodsAdapter);

        btnShowLastWeek    = findViewById(R.id.btnShowLastWeekFlw);
        btnFilterByMood    = findViewById(R.id.btnFilterByMoodFlw);
        btnFilterByKeyword = findViewById(R.id.btnFilterByKeywordFlw);
        btnClearFilters    = findViewById(R.id.btnClearFiltersFlw);

        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showKeywordFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());


        loadFolloweesMoods();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_followees_moods);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }
    /**
     * Filters the displayed moods to show only those from the last 7 days.
     */
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

    /**
     * Filters the moods by the selected emotion.
     *
     * @param selectedEmotion The emotion to filter by.
     */
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


    /**
     * Displays a dialog with mood options to filter moods by emotion.
     */
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

    /**
     * Displays a dialog prompting the user to input a keyword for filtering moods by reason.
     */
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


    /**
     * Filters the moods to show only those whose reason contains the specified keyword.
     *
     * @param keyword The keyword to search for.
     */
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

    /**
     * Clears all active filters and resets the mood list to original.
     */
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
                                                userMoodItems.sort((i1, i2) -> {
                                                    if (i1.moodEvent.getDate() == null || i2.moodEvent.getDate() == null) return 0;
                                                    return i2.moodEvent.getDate().compareTo(i1.moodEvent.getDate());
                                                });
                                                originalUserMoodItems.sort((i1, i2) -> {
                                                    if (i1.moodEvent.getDate() == null || i2.moodEvent.getDate() == null) return 0;
                                                    return i2.moodEvent.getDate().compareTo(i1.moodEvent.getDate());
                                                });
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


    /**
     * Handles navigation from the bottom navigation bar.
     *
     * @param item The selected navigation item.
     * @return true if the item was handled.
     */
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
