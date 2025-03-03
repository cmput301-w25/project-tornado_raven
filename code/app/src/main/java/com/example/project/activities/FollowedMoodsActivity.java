package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapters.FolloweesAdapter; // We can re-use or create a new "FollowedMoodsAdapter"
import com.example.project.dialogs.FilterKeywordDialog;
import com.example.project.dialogs.FilterMoodDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the mood history of all followees.
 */
public class FollowedMoodsActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowedMoods;
    private BottomNavigationView bottomNav;
    private FolloweesAdapter followeesAdapter;
    // Optionally create a new adapter "FollowedMoodsAdapter" if you want different logic

    private List<String> followedMoodsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_moods);

        recyclerFollowedMoods = findViewById(R.id.recyclerFollowedMoods);
        recyclerFollowedMoods.setLayoutManager(new LinearLayoutManager(this));

        // Example data: each string could represent "username: mood"
        followedMoodsList = new ArrayList<>();
        followedMoodsList.add("Alice: Feeling Great!");
        followedMoodsList.add("Bob: Stressed about exam");
        followedMoodsList.add("Carla: Enjoying the sunshine");

        // Re-using the same adapter for demonstration
        // so we can see user name + "mood" from the string
        followeesAdapter = new FolloweesAdapter(followedMoodsList, position -> {
            Toast.makeText(this, "Clicked item #"+position, Toast.LENGTH_SHORT).show();
        });
        recyclerFollowedMoods.setAdapter(followeesAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_followed_moods); // Highlight correct tab

        bottomNavigationView.setOnItemSelectedListener(item -> onBottomNavItemSelected(item));

        // Filter button logic
        findViewById(R.id.btnShowLastWeek).setOnClickListener(v -> {
            Toast.makeText(this, "Filtering by last week...", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnFilterByMood).setOnClickListener(v -> {
            DialogFragment dialog = new FilterMoodDialog();
            dialog.show(getSupportFragmentManager(), "FilterMoodDialog");
        });
        findViewById(R.id.btnFilterByKeyword).setOnClickListener(v -> {
            DialogFragment dialog = new FilterKeywordDialog();
            dialog.show(getSupportFragmentManager(), "FilterKeywordDialog");
        });
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_followees && !isCurrentActivity(FolloweesActivity.class)) {
            startActivity(new Intent(this, FolloweesActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_followed_moods) {
            return true; // Already in FollowedMoodsActivity
        } else if (id == R.id.nav_my_mood_history && !isCurrentActivity(MoodHistoryActivity.class)) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_profile && !isCurrentActivity(UsersFollowedActivity.class)) {
            startActivity(new Intent(this, UsersFollowedActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return false;
    }
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}

