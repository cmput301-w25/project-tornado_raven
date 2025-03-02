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

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> onBottomNavItemSelected(item));
        // Highlight the first item by default
        bottomNav.setSelectedItemId(R.id.nav_followees);
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_followees) {
            // Already here
            return true;
        } else if (id == R.id.nav_followed_moods) {
            // Go to the Followed Moods screen
            startActivity(new Intent(this, FollowedMoodsActivity.class));
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            Toast.makeText(this, "Go to My Mood History", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_mood_map) {
            Toast.makeText(this, "Go to Mood Map", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Go to My Profile", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}

