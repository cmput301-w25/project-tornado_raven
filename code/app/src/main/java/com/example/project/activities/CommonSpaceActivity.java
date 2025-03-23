package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;

import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.CommonSpaceAdapter;
import com.example.project.adapters.FollowRequestAdapter;
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
    private List<MoodEvent> allMoods;
    private List<String> allUsernames;
    private List<MoodEvent> filteredMoods;
    private Set<String> pendingAuthors;
    private Set<String> followedAuthors = new HashSet<>(); // the current following users of this user

    // Filter buttons
    private Button btnShowLastWeek, btnFilterByMood, btnFilterByKeyword, btnClearFilters;
    // Searching authors
    private AutoCompleteTextView editSearchUserName;

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
        allUsernames = new ArrayList<>();


        String currentUser1 = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("username", null);
        if (currentUser1 != null) {
            db.collection("Follows")
                    .whereEqualTo("followerUsername", currentUser1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DocumentSnapshot doc : snapshot) {
                            String followed = doc.getString("followedUsername");
                            if (followed != null) {
                                followedAuthors.add(followed);
                            }
                        }
                        adapter.setFollowedAuthors(followedAuthors); // send this list to adapter
                        adapter.notifyDataSetChanged(); // refresh
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load followed users", Toast.LENGTH_SHORT).show();
                    });
        }



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
        //Load all users form Firebase for searching
        loadAllUsersFromFirestore();

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

        // Text-based user search
        editSearchUserName = findViewById(R.id.editTextSearchUserName);
        // Making sure we'll show suggestions after typing 1 character
        editSearchUserName.setThreshold(1);

        //Implemented auto complete suggestion so that user can pick and select from the
        //drop down list.
        editSearchUserName.setOnItemClickListener((parent, view, position, id) -> {
            // User picked a username from the dropdown
            String pickedUser = (String) parent.getItemAtPosition(position);
            if (pickedUser != null && !pickedUser.isEmpty()) {
                Toast.makeText(this, "Selected user: " + pickedUser,
                        Toast.LENGTH_SHORT).show();
                // e.g., you can jump directly to their profile, or do something else
                // For instance:
                searchUserByUsername(pickedUser);
            }
        });

    }


    /**
     * ADDED: Load *all* user docs from Firestore into 'allUsernames', so we can auto-complete them.
     */
    private void loadAllUsersFromFirestore() {
        db.collection("users")
                .get()
                .addOnSuccessListener(snap -> {
                    allUsernames.clear();
                    for (DocumentSnapshot doc : snap) {
                        if (doc.exists()) {
                            String name = doc.getString("username");
                            if (name != null && !name.isEmpty()) {
                                allUsernames.add(name);
                            }
                        }
                    }
                    // Provide a dropdown suggestion
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            allUsernames
                    );
                    // We set the adapter for the AutoComplete
                    editSearchUserName.setAdapter(userAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    /**
     * Load all MoodEvents from Firestore where the privacy level is All Users,
     * store them in 'allMoods' and display them.
     */
    private void loadAllMoodsFromFirestore() {
        db.collection("MoodEvents")
                .whereEqualTo("privacyLevel", "ALL_USERS")
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
        } else if (id == R.id.nav_followees_moods) {
            startActivity(new Intent(this, FolloweesMoodsActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_following_users) {
            startActivity(new Intent(this, FollowingUsersActivity.class));
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


    //Filter by keyword
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


    //Filter by last week
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

    //Clear Filter
    private void clearFilters() {
        filteredMoods.clear();
        filteredMoods.addAll(allMoods);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }


    /**
     * If the user picks from the auto-complete or types something,
     * we can look them up in Firestore.
     */
    private void searchUserByUsername(String userSelected) {
        db.collection("users")
                .whereEqualTo("username", userSelected)
                .get()
                .addOnSuccessListener(snap -> {
                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    String uName = doc.getString("username");
                    Toast.makeText(this, "Found user: " + uName, Toast.LENGTH_SHORT).show();

                    // Pass the username field to the ProfileActivity
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("userName", uName);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error searching user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


}