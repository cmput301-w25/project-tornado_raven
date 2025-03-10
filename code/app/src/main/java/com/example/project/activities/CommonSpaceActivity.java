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
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.CommonSpaceAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays public moods. Users can view and "Follow" individual moods.
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;
    private List<String> moodList;
    private List<String> originalList;

    /**
     * Initializes the activity, sets up UI components, and loads initial mood data.
     *
     * @param savedInstanceState If the activity is being re-initialized after being shut down,
     *                           this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

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
            // "Follow" logic
            String selectedMood = moodList.get(position);
            GlobalData.followedMoods.add(selectedMood);
            Toast.makeText(this, "Followed mood: " + selectedMood, Toast.LENGTH_SHORT).show();
        });
        recyclerCommonSpace.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
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

    /**
     * Handles bottom navigation item selections.
     *
     * @param item The selected menu item.
     * @return true if handled, false otherwise.
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            return true; // Already in this activity
        } else if (id == R.id.nav_followed_moods) {
            startActivity(new Intent(this, FollowedMoodsActivity.class));
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else if (id == R.id.nav_mood_map) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        finish();
        return true;
    }

    /**
     * Filters the mood list to display only moods from the last week.
     */
    private void filterLastWeek() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.contains("2023-03-20") || s.contains("2023-03-19") || s.contains("2023-03-18")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered last week");
    }

    /**
     * Filters the mood list to display only moods with "Happy".
     */
    private void filterByMood() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.toLowerCase().contains("happy")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered: happy moods");
    }

    /**
     * Filters the mood list based on specific keywords (e.g., "job" or "jam").
     */
    private void filterByKeyword() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.toLowerCase().contains("job") || s.toLowerCase().contains("jam")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered by keywords job/jam");
    }

    /**
     * Clears all applied filters and restores the original mood list.
     */
    private void clearFilters() {
        updateList(originalList, "Cleared filters");
    }

    /**
     * Updates the displayed mood list and shows a toast message.
     *
     * @param newList The new list of moods to display.
     * @param toast   A message to be shown in a toast notification.
     */
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
