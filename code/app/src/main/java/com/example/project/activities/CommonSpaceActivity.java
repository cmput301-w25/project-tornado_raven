package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.models.Emotion;

import com.example.project.models.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.CommonSpaceAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 *  - Swipe-to-refresh support.
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;
    private List<MoodEvent> allMoods;
    private List<String> allUsernames;
    private List<MoodEvent> filteredMoods;
    private Set<String> pendingAuthors;
    private Set<String> followedAuthors = new HashSet<>();

    private Button btnShowLastWeek, btnFilterByMood, btnFilterByKeyword, btnClearFilters;
    private AutoCompleteTextView editSearchUserName;
    private ActivityResultLauncher<Intent> addMoodLauncher;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

        db = FirebaseFirestore.getInstance();

        allMoods      = new ArrayList<>();
        filteredMoods = new ArrayList<>();
        pendingAuthors= new HashSet<>();
        allUsernames = new ArrayList<>();

        addMoodLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAllMoodsFromFirestore();
                    }
                }
        );
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadAllMoodsFromFirestore();
        });



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
                        adapter.setFollowedAuthors(followedAuthors);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load followed users", Toast.LENGTH_SHORT).show();
                    });
        }
        if (currentUser1 != null) {
            db.collection("FollowRequests")
                    .whereEqualTo("fromUser", currentUser1)
                    .whereEqualTo("status", "PENDING")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DocumentSnapshot doc : snapshot) {
                            String toUser = doc.getString("toUser");
                            if (toUser != null) {
                                pendingAuthors.add(toUser);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load pending requests", Toast.LENGTH_SHORT).show();
                    });
        }




        recyclerCommonSpace = findViewById(R.id.recyclerCommonSpace);
        recyclerCommonSpace.setLayoutManager(new LinearLayoutManager(this));

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
        adapter.setCurrentUsername(currentUser1);
        recyclerCommonSpace.setAdapter(adapter);

        loadAllMoodsFromFirestore();
        loadAllUsersFromFirestore();

        // Set up bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        btnShowLastWeek   = findViewById(R.id.btnShowLastWeek);
        btnFilterByMood   = findViewById(R.id.btnFilterByMood);
        btnFilterByKeyword= findViewById(R.id.btnFilterByKeyword);
        btnClearFilters   = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showReasonFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        editSearchUserName = findViewById(R.id.editTextSearchUserName);
        editSearchUserName.setThreshold(1);

        editSearchUserName.setOnItemClickListener((parent, view, position, id) -> {
            String pickedUser = (String) parent.getItemAtPosition(position);
            if (pickedUser != null && !pickedUser.isEmpty()) {
                Toast.makeText(this, "Selected user: " + pickedUser,
                        Toast.LENGTH_SHORT).show();
                searchUserByUsername(pickedUser);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_mood);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(CommonSpaceActivity.this, AddingMoodActivity.class);
            addMoodLauncher.launch(intent);
        });

    }

    /**
     * Deletes a mood document from Firestore based on its ID.
     * @param moodId the ID of the mood to delete
     */
    private void deleteMoodFromFirestore(String moodId) {
        db.collection("MoodEvents")
                .whereEqualTo("id", moodId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String docId = snapshot.getDocuments().get(0).getId();
                        db.collection("MoodEvents").document(docId)
                                .delete()
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this, "Mood deleted from Firestore", Toast.LENGTH_SHORT).show();
                                    deleteMoodFromList(moodId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Mood not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Query failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }



    /**
     * Loads all usernames from Firestore for auto-complete text suggestion.
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
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            allUsernames
                    );
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
        SwipeRefreshLayout swipe = findViewById(R.id.swipeRefreshLayout);

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
                    allMoods.sort((m1, m2) -> {
                        if (m1.getDate() == null || m2.getDate() == null) return 0;
                        return m2.getDate().compareTo(m1.getDate()); // Descending order
                    });

                    filteredMoods.addAll(allMoods);
                    adapter.notifyDataSetChanged();
                    swipe.setRefreshing(false);

                    Toast.makeText(this, "Loaded " + allMoods.size() + " mood events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    swipe.setRefreshing(false);
                    Toast.makeText(this, "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("deleteMoodId")) {
                String deleteId = data.getStringExtra("deleteMoodId");
                if (deleteId != null) {
                    deleteMoodFromFirestore(deleteId);
                }
            }
            else if (data.hasExtra("updatedMood")) {
                MoodEvent updated = (MoodEvent) data.getSerializableExtra("updatedMood");
                if (updated != null) {
                    updateMoodInList(updated);
                }
            }
            else {
                loadAllMoodsFromFirestore();
            }
        }
    }

    /**
     * Removes a deleted mood from the list and updates the UI.
     * @param deleteId ID of the mood to remove
     */
    private void deleteMoodFromList(String deleteId) {
        boolean changed = false;
        for (int i = 0; i < allMoods.size(); i++) {
            if (allMoods.get(i).getId().equals(deleteId)) {
                allMoods.remove(i);
                changed = true;
                break;
            }
        }

        for (int i = 0; i < filteredMoods.size(); i++) {
            if (filteredMoods.get(i).getId().equals(deleteId)) {
                filteredMoods.remove(i);
                changed = true;
                break;
            }
        }

        if (changed) adapter.notifyDataSetChanged();
    }

    /**
     * Updates a mood from the filtered and all moods list.
     * @param updated the updated MoodEvent
     */
    private void updateMoodInList(MoodEvent updated) {
        boolean changed = false;

        for (int i = 0; i < allMoods.size(); i++) {
            if (allMoods.get(i).getId().equals(updated.getId())) {
                allMoods.set(i, updated);
                changed = true;
                break;
            }
        }

        for (int i = 0; i < filteredMoods.size(); i++) {
            if (filteredMoods.get(i).getId().equals(updated.getId())) {
                filteredMoods.set(i, updated);
                changed = true;
                break;
            }
        }

        if (changed) adapter.notifyDataSetChanged();
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

    /**
     * Filters mood events by the given author's name.
     * @param query The partial or full username to search for
     */
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


    /**
     * Displays a dialog with mood options for filtering.
     */
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

    /**
     *      Filter by emotion
     */
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

    /**
     * Displays a dialog to filter moods by a reason keyword.
     */
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


    /**
     *     Filter by keyword
     */
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


    /**
     *     Filter by last week
     */
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


    /**
     *     Clear Filter
     */
    private void clearFilters() {
        filteredMoods.clear();
        filteredMoods.addAll(allMoods);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }


    /**
     * If the user picks from the auto-complete or types something,
     * we can look them up in Firestore.
     @param userSelected the username selected from auto-complete
     */
    private void searchUserByUsername(String userSelected) {
        db.collection("users")
                .whereEqualTo("username", userSelected)
                .get()
                .addOnSuccessListener(snap -> {
                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    String uName = doc.getString("username");
                    Toast.makeText(this, "Found user: " + uName, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("userName", uName);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error searching user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


}