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

import com.example.project.R;
import com.example.project.adapters.UsersFollowedAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersFollowedActivity manages the display of users that the current user follows.
 * Allows the user to unfollow them if desired.
 */
public class UsersFollowedActivity extends AppCompatActivity {

    private RecyclerView recyclerUsersFollowed;
    private UsersFollowedAdapter usersFollowedAdapter;
    private List<String> followedUsersList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_followed);

        db = FirebaseFirestore.getInstance();

        recyclerUsersFollowed = findViewById(R.id.recyclerUsersFollowed);
        recyclerUsersFollowed.setLayoutManager(new LinearLayoutManager(this));

        followedUsersList = new ArrayList<>();
        usersFollowedAdapter = new UsersFollowedAdapter(followedUsersList, position -> {
            // Unfollow logic
            String userName = followedUsersList.get(position);
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentUsername = prefs.getString("username", null);
            if (currentUsername != null) {
                FollowManager.unfollowUser(currentUsername, userName);
                followedUsersList.remove(position);
                usersFollowedAdapter.notifyItemRemoved(position);
                Toast.makeText(this, "Unfollowed " + userName, Toast.LENGTH_SHORT).show();
            }
        });
        recyclerUsersFollowed.setAdapter(usersFollowedAdapter);

        // Load the actual list of followed users from Firestore
        loadFollowedUsers();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_followees);
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // If you have filter buttons, etc. for this screen, keep them as is
        findViewById(R.id.btnShowLastWeek).setOnClickListener(v ->
                Toast.makeText(this, "Filtering by last week (not implemented here)", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btnFilterByMood).setOnClickListener(v ->
                Toast.makeText(this, "Filter by Mood (not implemented)", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btnFilterByKeyword).setOnClickListener(v ->
                Toast.makeText(this, "Filter by Keyword (not implemented)", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnShowPendingRequests).setOnClickListener(v ->
                Toast.makeText(this, "Showing pending follow requests... (not implemented)", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadFollowedUsers() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", null);
        if (currentUsername == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("username", currentUsername)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        List<String> follows = (List<String>) snap.getDocuments().get(0).get("follows");
                        if (follows == null) {
                            follows = new ArrayList<>();
                        }
                        followedUsersList.clear();
                        followedUsersList.addAll(follows);
                        usersFollowedAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading followed users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_followees) {
            startActivity(new Intent(this, FolloweesActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return false;
    }
}
