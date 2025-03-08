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

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The "home" page: user sees their own mood history.
 * The bottom nav has 'Common Space' -> CommonSpaceActivity
 *                'Followed Moods' -> FollowedMoodsActivity, etc.
 */
public class MoodHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history);

        recyclerView = findViewById(R.id.recyclerMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodHistoryList = loadMoodHistory();
        filteredList = new ArrayList<>(moodHistoryList);

        moodHistoryAdapter = new MoodHistoryAdapter(this, filteredList);
        recyclerView.setAdapter(moodHistoryAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_my_mood_history);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        Button btnFilterByMood = findViewById(R.id.btnFilterByType);
        Button btnShowLastWeek = findViewById(R.id.btnShowLastMonth);
        Button btnClearFilters = findViewById(R.id.btnClearFilters);
        FloatingActionButton btnAddMood = findViewById(R.id.floating_add_mood_button);

        btnFilterByMood.setOnClickListener(v -> filterByMood());
        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        btnAddMood.setOnClickListener(v -> {
            // Example: open an Add Mood screen
            Toast.makeText(this, "Add new mood clicked", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, AddingMoodActivity.class));
        });
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_followed_moods) {
            startActivity(new Intent(this, FollowedMoodsActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            return true; // Already here
        } else if (id == R.id.nav_mood_map) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            overridePendingTransition(0,0);
            finish();
            return true;
        }
        return false;
    }

    private List<MoodEvent> loadMoodHistory() {
        List<MoodEvent> list = new ArrayList<>();
        // Example data
        list.add(new MoodEvent(Emotion.HAPPINESS, new Date(), "Got money", "home", "home"));
        list.add(new MoodEvent(Emotion.SADNESS, new Date(System.currentTimeMillis() - 3 * 86400000), "Only 5 dollars", "alone", "home"));
        list.add(new MoodEvent(Emotion.CONFUSION, new Date(System.currentTimeMillis() - 10 * 86400000), "Lost money", "alone", "home"));
        return list;
    }

    private void filterByMood() {
        Toast.makeText(this, "Filtering by mood...", Toast.LENGTH_SHORT).show();
        // Example: filter by "HAPPINESS" only
        List<MoodEvent> newList = new ArrayList<>();
        for (MoodEvent me : moodHistoryList) {
            if (me.getEmotion() == Emotion.HAPPINESS) {
                newList.add(me);
            }
        }
        filteredList.clear();
        filteredList.addAll(newList);
        moodHistoryAdapter.updateList(filteredList);
    }

    private void filterByLastWeek() {
        Toast.makeText(this, "Filtering last week...", Toast.LENGTH_SHORT).show();
        long cutoff = System.currentTimeMillis() - (7L * 86400000);
        List<MoodEvent> newList = new ArrayList<>();
        for (MoodEvent me : moodHistoryList) {
            if (me.getDate().getTime() >= cutoff) {
                newList.add(me);
            }
        }
        filteredList.clear();
        filteredList.addAll(newList);
        moodHistoryAdapter.updateList(filteredList);
    }

    private void clearFilters() {
        filteredList.clear();
        filteredList.addAll(moodHistoryList);
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }
}
