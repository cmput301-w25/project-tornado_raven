package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapters.FolloweesAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of Followees (people the current user follows).
 */
public class FolloweesActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private BottomNavigationView bottomNav;
    private FolloweesAdapter followeesAdapter;
    private List<String> followeesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followees);

        recyclerFollowees = findViewById(R.id.recyclerFollowees);
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        followeesList = new ArrayList<>();
        followeesList.add("Alice");
        followeesList.add("Bob");
        followeesList.add("Carla");

        // Setup adapter
        followeesAdapter = new FolloweesAdapter(followeesList, position -> {
            String userName = followeesList.get(position);
            followeesList.remove(position);
            followeesAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Unfollowed " + userName, Toast.LENGTH_SHORT).show();
        });
        recyclerFollowees.setAdapter(followeesAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_followees); // Highlight correct tab

        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);

    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_followees) {
            return true; // Already in FolloweesActivity
        } else if (id == R.id.nav_followed_moods) {
            startActivity(new Intent(this, FollowedMoodsActivity.class));
            overridePendingTransition(0, 0);
            finish(); // Close current activity
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UsersFollowedActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return false;
    }

}

