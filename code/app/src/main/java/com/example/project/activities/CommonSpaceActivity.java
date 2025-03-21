package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
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
 * CommonSpaceActivity with:
 *  - advanced filtering (mood, reason, last week)
 *  - text-based author search
 *  - "Request Follow" button logic
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;

    // Full list of MoodEvents from Firestore
    private List<MoodEvent> allMoods;
    // Currently displayed moods (after filters)
    private List<MoodEvent> filteredMoods;

    // Tracks authors for which the user has already sent a follow request
    // so we can show "Requested" instead of "Request Follow"
    private Set<String> pendingAuthors;

    // Filter buttons
    private Button btnShowLastWeek, btnFilterByMood, btnFilterByKeyword, btnClearFilters;
    // Searching authors
    private EditText editSearchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

        // Firestore init
        db = FirebaseFirestore.getInstance();

        // Initialize lists
        allMoods      = new ArrayList<>();
        filteredMoods = new ArrayList<>();
        pendingAuthors= new HashSet<>();

        // Set up RecyclerView
        recyclerCommonSpace = findViewById(R.id.recyclerCommonSpace);
        recyclerCommonSpace.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter with "Request Follow" logic
        adapter = new CommonSpaceAdapter(
                filteredMoods,
                (mood, button) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String currentUser = prefs.getString("username", null);
                    if (currentUser == null) {
                        Toast.makeText(CommonSpaceActivity.this, "Please log in first", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String author = mood.getAuthor();
                    if (author == null || author.equals(currentUser)) {
                        Toast.makeText(CommonSpaceActivity.this, "Invalid target user for follow request", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Send follow request
                    FollowManager.sendFollowRequest(currentUser, author);
                    Toast.makeText(CommonSpaceActivity.this, "Sent request to " + author, Toast.LENGTH_SHORT).show();

                    // Mark that author as "requested"
                    pendingAuthors.add(author);
                    adapter.notifyDataSetChanged();
                },
                pendingAuthors
        );
        recyclerCommonSpace.setAdapter(adapter);

        // Load all moods
        loadAllMoodsFromFirestore();

        // Set up bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Filter Buttons
        btnShowLastWeek   = findViewById(R.id.btnShowLastWeek);
        btnFilterByMood   = findViewById(R.id.btnFilterByMood);
        btnFilterByKeyword= findViewById(R.id.btnFilterByKeyword);
        btnClearFilters   = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showReasonFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Text-based author search
        editSearchName = findViewById(R.id.editTextSearchUser);
        editSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByAuthor(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * Load all MoodEvents from Firestore, store them in 'allMoods' and display them.
     */
    private void loadAllMoodsFromFirestore() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(snap -> {
                    allMoods.clear();
                    filteredMoods.clear();

                    for (DocumentSnapshot doc : snap) {
                        MoodEvent mood = doc.toObject(MoodEvent.class);
                        if (mood != null) {
                            allMoods.add(mood);
                        }
                    }
                    // By default, show them all
                    filteredMoods.addAll(allMoods);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Loaded " + allMoods.size() + " mood events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Bottom navigation
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

    // ------------------------------------------------------------------------
    // Searching by author (username)
    // ------------------------------------------------------------------------
    private void filterByAuthor(String query) {
        filteredMoods.clear();
        String lowerQ = query.toLowerCase();

        for (MoodEvent mood : allMoods) {
            String author = mood.getAuthor();
            if (author != null && author.toLowerCase().contains(lowerQ)) {
                filteredMoods.add(mood);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------------
    // MOOD FILTER (like MoodHistory)
    // ------------------------------------------------------------------------
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

    // Filter by emotion
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

    // ------------------------------------------------------------------------
    // REASON KEYWORD FILTER
    // ------------------------------------------------------------------------
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

    private void filterByReasonKeyword(String keyword) {
        filteredMoods.clear();
        String lowerKeyword = keyword.toLowerCase();

        for (MoodEvent mood : allMoods) {
            String reason = mood.getReason();
            if (reason != null && reason.toLowerCase().contains(lowerKeyword)) {
                filteredMoods.add(mood);
            }
        }
        if (filteredMoods.isEmpty()) {
            Toast.makeText(this, "No moods found containing: " + keyword, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Filtered by: " + keyword, Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------------
    // LAST-WEEK FILTER
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // CLEAR FILTERS
    // ------------------------------------------------------------------------
    private void clearFilters() {
        filteredMoods.clear();
        filteredMoods.addAll(allMoods);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }
}

