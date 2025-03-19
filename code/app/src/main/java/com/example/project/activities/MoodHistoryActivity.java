package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This activity displays the user's mood history in a RecyclerView.
 * It allows the user to filter moods by type, reason, or date, and also add, update, or delete moods.
 */
public class MoodHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> filteredList;
    private FirebaseFirestore db;

    /**
     * Initializes the activity. Sets up the RecyclerView, bottom navigation, and event listeners.
     * It also loads the mood history from Firestore.
     *
     * @param savedInstanceState The saved instance state, or null if there is none.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history);

        recyclerView = findViewById(R.id.recyclerMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodHistoryList = new ArrayList<>();
        filteredList = new ArrayList<>();
        // set adapter
        moodHistoryAdapter = new MoodHistoryAdapter(this, filteredList);
        recyclerView.setAdapter(moodHistoryAdapter);

        db = FirebaseFirestore.getInstance();

        // Read data from Firestore
        loadMoodHistoryFromFirestore();

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_mood_history); // Highlight the correct tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(CommonSpaceActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_followees && !isCurrentActivity(FolloweesActivity.class)) {
                // Navigate to FolloweesActivity instead of the old FollowedMoodsActivity
                startActivity(new Intent(this, FolloweesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_my_mood_history) {
                // Already here
                return true;

            } else if (id == R.id.nav_mood_map) {
                // Placeholder if you have a MoodMapActivity
                Toast.makeText(this, "Go to Mood Map", Toast.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.nav_profile && !isCurrentActivity(ProfileActivity.class)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Filter buttons, etc.
        Button btnFilterByMood = findViewById(R.id.btnFilterByType);
        Button btnShowLastWeek = findViewById(R.id.btnShowLastMonth);
        FloatingActionButton btnAddMood = findViewById(R.id.floating_add_mood_button);
        Button btnSearchKeyword = findViewById(R.id.btnSearchKeyword);

        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnSearchKeyword.setOnClickListener(v -> showReasonFilterDialog());

        // Add new mood
        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddingMoodActivity.class);
            startActivityForResult(intent, 1); // use requestCode=1
        });
    }

    /**
     * Loads the mood history from Firestore and updates the RecyclerView.
     */
    private void loadMoodHistoryFromFirestore() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moodHistoryList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        MoodEvent mood = document.toObject(MoodEvent.class);
                        moodHistoryList.add(mood);
                    }
                    // Sort by date descending
                    moodHistoryList.sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));

                    filteredList.clear();
                    filteredList.addAll(moodHistoryList);
                    moodHistoryAdapter.updateList(filteredList); // update RecyclerView
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload mood data failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Updates a mood item in both the local list and Firestore.
     * @param updatedMood The updated mood object to replace the old one.
     */
    private void updateMoodItem(MoodEvent updatedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(updatedMood.getId())) {
                moodHistoryList.set(i, updatedMood);
                // Also update filteredList if it's there
                for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(updatedMood.getId())) {
                        filteredList.set(j, updatedMood);
                        break;
                    }
                }
                moodHistoryAdapter.updateMood(updatedMood);
                Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();

                // Update Firestore
                db.collection("MoodEvents")
                        .whereEqualTo("id", updatedMood.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                db.collection("MoodEvents").document(documentId)
                                        .set(updatedMood)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(this, "updated successfully", Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(this, "No corresponding MoodEvent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                return;
            }
        }
    }

    /**
     * Deletes a mood item both from the local list and Firestore.
     * @param moodId The ID of the mood event to delete.
     */
    private void deleteMood(String moodId) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(moodId)) {
                moodHistoryList.remove(i);
                for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(moodId)) {
                        filteredList.remove(j);
                        break;
                    }
                }
                moodHistoryAdapter.notifyItemRemoved(i);
                Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();

                // Delete from Firestore
                db.collection("MoodEvents")
                        .whereEqualTo("id", moodId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                db.collection("MoodEvents").document(documentId)
                                        .delete()
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(this, "deleted successfully", Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                                loadMoodHistoryFromFirestore();
                            } else {
                                Toast.makeText(this, "No corresponding MoodEvent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                return;
            }
        }
    }

    /**
     * Displays a dialog to filter moods by a selected emotion.
     */
    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER","CONFUSION","DISGUST","FEAR","HAPPINESS",
                "SADNESS","SHAME","SURPRISE","CLEAR FILTER"};

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
     * Displays a dialog to filter moods by a reason keyword.
     */
    private void showReasonFilterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter keyword to filter by reason");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Filter", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                filterByReasonKeyword(keyword);
            } else {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Filters the mood history by a given reason keyword.
     * @param keyword The keyword to filter the reasons by.
     */
    private void filterByReasonKeyword(String keyword) {
        filteredList.clear();
        String lowerKeyword = keyword.toLowerCase();

        for (MoodEvent mood : moodHistoryList) {
            String reason = mood.getReason();
            if (reason != null) {
                String lowerReason = reason.toLowerCase();
                // We can do a simple substring check:
                if (lowerReason.contains(lowerKeyword)) {
                    filteredList.add(mood);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No moods found with reason containing: " + keyword, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Filtered by reason keyword: " + keyword, Toast.LENGTH_SHORT).show();
        }

        moodHistoryAdapter.updateList(filteredList);
    }

    /**
     * Filters the mood history by a specific emotion.
     * @param selectedMood The emotion to filter by.
     */
    private void filterByMood(Emotion selectedMood) {
        filteredList.clear();
        for (MoodEvent mood : moodHistoryList) {
            if (mood.getEmotion() == selectedMood) {
                filteredList.add(mood);
            }
        }
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filtered by " + selectedMood.name(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Filters the mood history to show only the moods from the last 7 days.
     */
    private void filterByLastWeek() {
        filteredList.clear();
        long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        for (MoodEvent mood : moodHistoryList) {
            if (mood.getDate().getTime() >= oneWeekAgo) {
                filteredList.add(mood);
            }
        }
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Showing last week's moods", Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears all applied filters and restores the original mood list.
     */
    private void clearFilters() {
        filteredList.clear();
        filteredList.addAll(moodHistoryList);
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the result from an activity when a new mood is added or an existing one is updated/deleted.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Adding a new mood
            loadMoodHistoryFromFirestore();
        }
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            // Editing or deleting a mood
            if (data.hasExtra("updatedMood")) {
                MoodEvent updatedMood = (MoodEvent) data.getSerializableExtra("updatedMood");
                updateMoodItem(updatedMood);
            } else if (data.hasExtra("deleteMoodId")) {
                String deleteMoodId = data.getStringExtra("deleteMoodId");
                deleteMood(deleteMoodId);
            }
        }
    }

    /**
     * Checks if the current activity is the specified activity.
     * @param activityClass The activity class to check against.
     * @return True if the current activity matches the specified activity, false otherwise.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}
