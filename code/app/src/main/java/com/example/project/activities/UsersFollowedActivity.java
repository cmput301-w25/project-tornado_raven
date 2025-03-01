package com.example.project.activities;

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

public class UsersFollowedActivity extends AppCompatActivity {

    private RecyclerView recyclerUsersFollowed;
    private BottomNavigationView bottomNav;
    private UsersFollowedAdapter usersFollowedAdapter;
    private List<String> followedUsersList;

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

        // Adapter
        usersFollowedAdapter = new UsersFollowedAdapter(followedUsersList, position -> {
            String userName = followedUsersList.get(position);
            followedUsersList.remove(position);
            usersFollowedAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Unfollowed " + userName, Toast.LENGTH_SHORT).show();
        });
        recyclerUsersFollowed.setAdapter(usersFollowedAdapter);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> onBottomNavItemSelected(item));

        // Filter button clicks
        findViewById(R.id.btnShowLastWeek).setOnClickListener(v -> showLastWeekFilter());
        findViewById(R.id.btnFilterByMood).setOnClickListener(v -> showMoodFilterDialog());
        findViewById(R.id.btnFilterByKeyword).setOnClickListener(v -> showKeywordFilterDialog());

        // Pending requests
        findViewById(R.id.btnShowPendingRequests).setOnClickListener(v -> {
            // Example: show a Toast or navigate to a "Pending Requests" screen
            Toast.makeText(this, "Showing pending follow requests...", Toast.LENGTH_SHORT).show();
        });

        // Optionally highlight the 'Mood Followees' tab
        bottomNav.setSelectedItemId(R.id.nav_mood_followees);
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_followers) {
            // Switch to FollowersActivity
            finish(); // close this activity
            // or use an intent to open FollowersActivity
            // or just do:
            // startActivity(new Intent(this, FollowersActivity.class));
            return true;
        } else if (id == R.id.nav_mood_followees) {
            // Already here
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            Toast.makeText(this, "Go to My Mood History screen", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_mood_map) {
            Toast.makeText(this, "Go to Mood Map screen", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Go to Profile screen", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void showLastWeekFilter() {
        // Example: filter data by last week's timeline
        Toast.makeText(this, "Filtering by last week...", Toast.LENGTH_SHORT).show();
    }

    private void showMoodFilterDialog() {
        DialogFragment dialog = new FilterMoodDialog();
        dialog.show(getSupportFragmentManager(), "FilterMoodDialog");
    }

    private void showKeywordFilterDialog() {
        DialogFragment dialog = new FilterKeywordDialog();
        dialog.show(getSupportFragmentManager(), "FilterKeywordDialog");
    }
}
