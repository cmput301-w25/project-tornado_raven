package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.GlobalData;
import com.example.project.R;
import com.example.project.adapters.FollowedMoodsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows only the moods user has specifically followed.
 * No references to "followed users" logic here.
 */
public class FollowedMoodsActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowedMoods;
    private FollowedMoodsAdapter adapter;
    private List<String> followedList;
    private List<String> backupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_moods);

        recyclerFollowedMoods = findViewById(R.id.recyclerFollowedMoods);
        recyclerFollowedMoods.setLayoutManager(new LinearLayoutManager(this));

        // The user's stored followed moods
        followedList = new ArrayList<>(GlobalData.followedMoods);
        backupList = new ArrayList<>(followedList);

        adapter = new FollowedMoodsAdapter(followedList, position -> {
            String moodData = followedList.get(position);
            followedList.remove(position);
            adapter.notifyItemRemoved(position);
            GlobalData.followedMoods.remove(moodData);
            Toast.makeText(this, "Unfollowed mood: " + moodData, Toast.LENGTH_SHORT).show();
        });
        recyclerFollowedMoods.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_followees);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        Button btnShowLastWeek = findViewById(R.id.btnShowLastWeek);
        Button btnFilterByMood = findViewById(R.id.btnFilterByMood);
        Button btnFilterByKeyword = findViewById(R.id.btnFilterByKeyword);
        Button btnClearFilters = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterLastWeek());
        btnFilterByMood.setOnClickListener(v -> filterByMood());
        btnFilterByKeyword.setOnClickListener(v -> filterByKeyword());
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_followees) {
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_mood_map) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void filterLastWeek() {
        List<String> newList = new ArrayList<>();
        for (String mood : backupList) {
            if (mood.contains("2023-03-20") || mood.contains("2023-03-19")) {
                newList.add(mood);
            }
        }
        updateList(newList, "Filtered: Last Week");
    }

    private void filterByMood() {
        List<String> newList = new ArrayList<>();
        for (String mood : backupList) {
            if (mood.toLowerCase().contains("happy")) {
                newList.add(mood);
            }
        }
        updateList(newList, "Filtered by mood: happy");
    }

    private void filterByKeyword() {
        List<String> newList = new ArrayList<>();
        for (String mood : backupList) {
            if (mood.toLowerCase().contains("job") || mood.toLowerCase().contains("sunshine")) {
                newList.add(mood);
            }
        }
        updateList(newList, "Filtered by keyword job/sunshine");
    }

    private void clearFilters() {
        updateList(backupList, "Cleared filters");
    }

    private void updateList(List<String> newList, String toast) {
        followedList.clear();
        followedList.addAll(newList);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        if (followedList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }
    }
}
