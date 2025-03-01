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
import com.example.project.adapters.FollowersAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowers;
    private BottomNavigationView bottomNav;
    private FollowersAdapter followersAdapter;
    private List<String> followersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        recyclerFollowers = findViewById(R.id.recyclerFollowers);
        recyclerFollowers.setLayoutManager(new LinearLayoutManager(this));

        // Demo data
        followersList = new ArrayList<>();
        followersList.add("Alice");
        followersList.add("Bob");
        followersList.add("Charlie");
        followersList.add("Debbie");
        followersList.add("Ethan");

        // Adapter
        followersAdapter = new FollowersAdapter(followersList, position -> {
            String removedFollower = followersList.get(position);
            followersList.remove(position);
            followersAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Removed " + removedFollower, Toast.LENGTH_SHORT).show();
        });
        recyclerFollowers.setAdapter(followersAdapter);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> onBottomNavItemSelected(item));

        // Optionally highlight the 'Followers' tab
        bottomNav.setSelectedItemId(R.id.nav_followers);
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_followers) {
            // Already here
            return true;
        } else if (id == R.id.nav_mood_followees) {
            Intent intent = new Intent(this, UsersFollowedActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            // Example: navigate to MyMoodHistoryActivity
            Toast.makeText(this, "Go to My Mood History screen", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_mood_map) {
            // Example: navigate to MoodMapActivity
            Toast.makeText(this, "Go to Mood Map screen", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_profile) {
            // Example: navigate to ProfileActivity
            Toast.makeText(this, "Go to Profile screen", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}
