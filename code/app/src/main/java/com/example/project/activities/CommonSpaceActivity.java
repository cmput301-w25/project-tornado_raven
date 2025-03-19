package com.example.project.activities;

import android.content.Intent;
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

import com.example.project.GlobalData;
import com.example.project.R;
import com.example.project.adapters.CommonSpaceAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays public moods. Users can view and "Follow" individual moods.
 * Also has a search bar to find a user by username and jump to their profile.
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;
    private List<String> moodList;
    private List<String> originalList;

    // For searching user
    private EditText editSearchUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

        db = FirebaseFirestore.getInstance();

        recyclerCommonSpace = findViewById(R.id.recyclerCommonSpace);
        recyclerCommonSpace.setLayoutManager(new LinearLayoutManager(this));

        // Example mood data: "Emotion|Date|Reason|Social"
        originalList = new ArrayList<>();
        originalList.add("Happy|2024-03-20|Got a new job|With a crowd");
        originalList.add("Sad|2023-03-19|Bad news|Alone");
        originalList.add("Angry|2023-03-18|Traffic jam|Two to several");
        originalList.add("Surprised|2023-03-17|Found $10|With one person");

        moodList = new ArrayList<>(originalList);

        adapter = new CommonSpaceAdapter(moodList, position -> {
            // "Follow" logic (dummy)
            String selectedMood = moodList.get(position);
            GlobalData.followedMoods.add(selectedMood);
            Toast.makeText(this, "Followed mood: " + selectedMood, Toast.LENGTH_SHORT).show();
        });
        recyclerCommonSpace.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Filter buttons
        Button btnShowLastWeek = findViewById(R.id.btnShowLastWeek);
        Button btnFilterByMood = findViewById(R.id.btnFilterByMood);
        Button btnFilterByKeyword = findViewById(R.id.btnFilterByKeyword);
        Button btnClearFilters = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterLastWeek());
        btnFilterByMood.setOnClickListener(v -> filterByMood());
        btnFilterByKeyword.setOnClickListener(v -> filterByKeyword());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        // Set up the search bar
        editSearchUser = findViewById(R.id.editTextSearchUser);
        // When user presses "Enter"/"Search" on the keyboard
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

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            return true; // Already in this activity
        } else if (id == R.id.nav_followees) {
            startActivity(new Intent(this, FolloweesActivity.class));
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else if (id == R.id.nav_mood_map) {
            // Not implemented
            Toast.makeText(this, "Go to Mood Map", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        finish();
        return true;
    }

    // Very basic "search user" using Firestore
    private void searchUserByUsername(String username) {
        CollectionReference userRef = db.collection("users");
        userRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "No user found: " + username, Toast.LENGTH_SHORT).show();
                    } else {
                        // In a real app, you might get multiple results. We'll just go to the first one
                        String userId = snap.getDocuments().get(0).getId();
                        Toast.makeText(this, "Found user " + username + ", jumping to profile...", Toast.LENGTH_SHORT).show();

                        // Jump to profile
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.putExtra("userId", userId); // pass userId to ProfileActivity
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterLastWeek() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.contains("2023-03-20") || s.contains("2023-03-19") || s.contains("2023-03-18")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered last week");
    }

    private void filterByMood() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.toLowerCase().contains("happy")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered: happy moods");
    }

    private void filterByKeyword() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.toLowerCase().contains("job") || s.toLowerCase().contains("jam")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered by keywords job/jam");
    }

    private void clearFilters() {
        updateList(originalList, "Cleared filters");
    }

    private void updateList(List<String> newList, String toast) {
        moodList.clear();
        moodList.addAll(newList);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        if (moodList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }
    }
}
