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
import com.example.project.adapters.UsersFollowedAdapter;
import com.example.project.dialogs.FilterKeywordDialog;
import com.example.project.dialogs.FilterMoodDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersFollowedActivity manages the display of users that the current user follows.
 * It allows users to unfollow people, apply filters, and navigate between sections of the app.
 */
public class UsersFollowedActivity extends AppCompatActivity {

    private RecyclerView recyclerUsersFollowed;
    private BottomNavigationView bottomNav;
    private UsersFollowedAdapter usersFollowedAdapter;
    private List<String> followedUsersList;

    /**
     * Initializes the activity, sets up the RecyclerView and bottom navigation,
     * and loads the list of followed users.
     *
     * @param savedInstanceState The saved instance state, or null if there is none.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_followed);

        recyclerUsersFollowed = findViewById(R.id.recyclerUsersFollowed);
        recyclerUsersFollowed.setLayoutManager(new LinearLayoutManager(this));

        // Demo data
        followedUsersList = new ArrayList<>();
        followedUsersList.add("Frank");
        followedUsersList.add("Gina");
        followedUsersList.add("Hannah");
        followedUsersList.add("Ivan");
        followedUsersList.add("Jessica");

        // Adapter setup
        usersFollowedAdapter = new UsersFollowedAdapter(followedUsersList, position -> {
            String userName = followedUsersList.get(position);
            followedUsersList.remove(position);
            usersFollowedAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Unfollowed " + userName, Toast.LENGTH_SHORT).show();
        });
        recyclerUsersFollowed.setAdapter(usersFollowedAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_followees);
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Filter buttons
        findViewById(R.id.btnShowLastWeek).setOnClickListener(v -> showLastWeekFilter());
        findViewById(R.id.btnFilterByMood).setOnClickListener(v -> showMoodFilterDialog());
        findViewById(R.id.btnFilterByKeyword).setOnClickListener(v -> showKeywordFilterDialog());

        // Pending requests button
        findViewById(R.id.btnShowPendingRequests).setOnClickListener(v ->
                Toast.makeText(this, "Showing pending follow requests...", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Handles bottom navigation item selection and switches between different activities.
     *
     * @param item The selected menu item.
     * @return True if the navigation item selection is handled, false otherwise.
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_common_space) {
            return true; // Already in FolloweesActivity
        } else if (id == R.id.nav_followees) {
            startActivity(new Intent(this, FollowedMoodsActivity.class));
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

    /**
     * Displays a Toast message indicating that the data is filtered by the last week.
     */
    private void showLastWeekFilter() {
        Toast.makeText(this, "Filtering by last week...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a dialog to filter moods by a selected emotion.
     */
    private void showMoodFilterDialog() {
        DialogFragment dialog = new FilterMoodDialog();
        dialog.show(getSupportFragmentManager(), "FilterMoodDialog");
    }

    /**
     * Displays a dialog to filter moods by a keyword.
     */
    private void showKeywordFilterDialog() {
        DialogFragment dialog = new FilterKeywordDialog();
        dialog.show(getSupportFragmentManager(), "FilterKeywordDialog");
    }
}
