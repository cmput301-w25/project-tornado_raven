package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
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
    private List<String> originalMoodsList; // Stores unfiltered data


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

        originalMoodsList = new ArrayList<>(followedMoodsList); // Backup for resetting

        // Re-using the same adapter for demonstration
        // so we can see user name + "mood" from the string
        followeesAdapter = new FolloweesAdapter(followedMoodsList, position -> {
            Toast.makeText(this, "Clicked item #"+position, Toast.LENGTH_SHORT).show();
        });
        recyclerFollowedMoods.setAdapter(followeesAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_followed_moods); // Highlight correct tab

        bottomNavigationView.setOnItemSelectedListener(item -> onBottomNavItemSelected(item));

        // ✅ Set up filtering buttons
        Button btnShowLastWeek = findViewById(R.id.btnShowLastWeek);
        Button btnFilterByMood = findViewById(R.id.btnFilterByMood);
        Button btnFilterByKeyword = findViewById(R.id.btnFilterByKeyword);
        //Button btnClearFilters = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnFilterByKeyword.setOnClickListener(v -> showKeywordSearchDialog());
        //btnClearFilters.setOnClickListener(v -> clearFilters());
        /*// Filter button logic
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
        });*/
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
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return false;
    }
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }

    // ✅ Filtering by Last Week
    private void filterByLastWeek() {
        List<String> tempList = new ArrayList<>();

        // Simulate filtering by last week (only keeps recent moods)
        for (String mood : originalMoodsList) {
            if (mood.contains("Feeling Great!") || mood.contains("Stressed about exam")) { // Example condition
                tempList.add(mood);
            }
        }
        updateList(tempList, "Filtered:Last week's moods");
    }

    // ✅ Filtering by Mood Type
    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER","CONFUSION","DISGUST","FEAR","HAPPINESS", "SADNESS","SHAME","SURPRISE","CLEAR FILTER"}; // Add more moods if needed

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Mood to Filter")
                .setItems(moods, (dialog, which) -> {
                    if (moods[which].equals("CLEAR FILTER")) {
                        clearFilters();
                    } else {
                        filterByMood(moods[which]);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void filterByMood(String selectedMood) {
        List<String> tempList = new ArrayList<>();

        for (String mood : originalMoodsList) {
            if (mood.toLowerCase().contains(selectedMood.toLowerCase())) {
                tempList.add(mood);
            }
        }
        updateList(tempList, "Filtered by mood: " + selectedMood);


    }

    // ✅ Filtering by Keyword
    private void showKeywordSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search by Keyword");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            filterByKeyword(keyword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void filterByKeyword(String keyword) {
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Enter a keyword!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> tempList = new ArrayList<>();
        for (String mood : originalMoodsList) {
            if (mood.toLowerCase().contains(keyword.toLowerCase())) {
                tempList.add(mood);
            }
        }
        updateList(tempList, "Filtered by keyword: " + keyword);
    }

    // ✅ Clear Filters and Restore Full List
    private void clearFilters() {
        followeesAdapter.resetList(); // Call the resetList() method in the adapter
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    // ✅ Update List for Filters
    // ✅ Updates the adapter list and shows a toast message
    private void updateList(List<String> newList, String toastMessage) {
        if (newList.isEmpty()) {
            Toast.makeText(this, "No results found!", Toast.LENGTH_SHORT).show();
        } else {
            followeesAdapter.updateList(newList); // Use adapter's method instead of modifying followedMoodsList
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        }
    }
}

