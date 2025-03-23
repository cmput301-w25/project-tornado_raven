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

public class FollowingUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;

    // dummydata
    private List<String> mockFollowees() {
        List<String> users = new ArrayList<>();
        users.add("Alice");
        users.add("Bob");
        users.add("Charlie");
        users.add("Diana");
        users.add("Edward");
        return users;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_users); // 确保 XML 文件名是 activity_following_users.xml

        recyclerFollowees = findViewById(R.id.recyclerFollowees);


        // RecyclerView
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));
        FolloweesAdapter adapter = new FolloweesAdapter(mockFollowees());
        recyclerFollowees.setAdapter(adapter);

        // Set up bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
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
}

