package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.CommonSpaceAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CommonSpaceActivity now has full filtering logic (similar to MoodHistory)
 * and once a follow request is sent to a user, the "Request Follow" button
 * becomes "Requested" for all moods by that user.
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;

    // The full mood list from Firestore
    private List<MoodEvent> allMoods;
    // The currently displayed / filtered list
    private List<MoodEvent> filteredMoods;

    // For implementing "once request is sent, button becomes Requested"
    // We track a set of authors for which the current user has sent a request.
    private Set<String> pendingAuthors;

    private FirebaseFirestore db;

    // Filter Buttons
    private Button btnShowLastWeek;
    private Button btnFilterByMood;
    private Button btnFilterByKeyword;
    private Button btnClearFilters;
    private EditText editSearchUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

        db = FirebaseFirestore.getInstance();

        recyclerCommonSpace = findViewById(R.id.recyclerCommonSpace);
        recyclerCommonSpace.setLayoutManager(new LinearLayoutManager(this));

        allMoods = new ArrayList<>();
        filteredMoods = new ArrayList<>();
        pendingAuthors = new HashSet<>();

        adapter = new CommonSpaceAdapter(filteredMoods, (mood, button) -> {
            // "Request Follow" logic
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentUser = prefs.getString("username", null);
            if (currentUser == null) {
                Toast.makeText(this, "Please log in before sending a follow request.", Toast.LENGTH_SHORT).show();
                return;
            }
            String author = mood.getAuthor();
            if (author == null || author.equals(currentUser)) {
                Toast.makeText(this, "Invalid target user for follow request.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send follow request
            FollowManager.sendFollowRequest(currentUser, author);
            Toast.makeText(this, "Follow request sent to " + author, Toast.LENGTH_SHORT).show();

            // Mark that author as "requested"
            pendingAuthors.add(author);

            // Refresh the adapter so that all moods by that author show "Requested"
            adapter.notifyDataSetChanged();
        }, pendingAuthors);

        recyclerCommonSpace.setAdapter(adapter);

        // Load all moods from Firestore
        loadAllMoods();

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Setup filter buttons
        btnShowLastWeek    = findViewById(R.id.btnShowLastWeek);
        btnFilterByMood    = findViewById(R.id.btnFilterByMood);
        btnFilterByKeyword = findViewById(R.id.btnFilterByKeyword);
        btnClearFilters    = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showReasonFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Searching other users
        editSearchUser = findViewById(R.id.editTextSearchUser);
        editSearchUser.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = editSearchUser.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchUserByUsername(query);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Loads all moods (from all users) from Firestore.
     */
    private void loadAllMoods() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(snap -> {
                    allMoods.clear();
                    filteredMoods.clear();

                    for (DocumentSnapshot doc : snap) {
                        MoodEvent me = doc.toObject(MoodEvent.class);
                        if (me != null) {
                            allMoods.add(me);
                        }
                    }
                    // By default, show them all
                    filteredMoods.addAll(allMoods);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * bottom navigation
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            return true;
        } else if (id == R.id.nav_followees) {
            startActivity(new Intent(this, FolloweesActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_mood_map) {
            Toast.makeText(this, "Go to Mood Map", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        return false;
    }

    /**
     * Searching user by username
     */
    private void searchUserByUsername(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "No user found: " + username, Toast.LENGTH_SHORT).show();
                    } else {
                        String userId = snap.getDocuments().get(0).getId();
                        Toast.makeText(this, "Found user: " + username, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // -------------------------------------------------------------------
    // FILTERS: replicate your MoodHistory approach
    // -------------------------------------------------------------------

    // Show a dialog for choosing an emotion filter
    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER","CONFUSION","DISGUST","FEAR","HAPPINESS","SADNESS","SHAME","SURPRISE","CLEAR FILTER"};

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

    // Show a dialog for filtering by reason keyword
    private void showReasonFilterDialog() {
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

    // Filter by reason text, similar to your MoodHistory logic
    private void filterByReasonKeyword(String keyword) {
        filteredMoods.clear();
        String lowerKeyword = keyword.toLowerCase();

        for (MoodEvent mood : allMoods) {
            String reason = mood.getReason();
            if (reason != null) {
                String lowerReason = reason.toLowerCase();
                String[] words = lowerReason.split("\\s+");
                boolean matchFound = false;
                for (String w : words) {
                    if (w.contains(lowerKeyword)) {
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound) {
                    filteredMoods.add(mood);
                }
            }
        }
        if (filteredMoods.isEmpty()) {
            Toast.makeText(this, "No moods found with reason containing: " + keyword, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Filtered by reason keyword: " + keyword, Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    // Filter by a chosen emotion
    private void filterByMood(Emotion selectedMood) {
        filteredMoods.clear();
        for (MoodEvent mood : allMoods) {
            if (mood.getEmotion() == selectedMood) {
                filteredMoods.add(mood);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Filtered by " + selectedMood.name(), Toast.LENGTH_SHORT).show();
    }

    // Filter by last 7 days
    private void filterByLastWeek() {
        filteredMoods.clear();
        long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        for (MoodEvent mood : allMoods) {
            if (mood.getDate() != null && mood.getDate().getTime() >= oneWeekAgo) {
                filteredMoods.add(mood);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Showing last week's moods", Toast.LENGTH_SHORT).show();
    }

    // Clears filters
    private void clearFilters() {
        filteredMoods.clear();
        filteredMoods.addAll(allMoods);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }
}

