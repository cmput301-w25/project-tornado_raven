package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.project.R;
import com.google.firebase.FirebaseApp;

/**
 * Main activity of the application. This activity is responsible for handling
 * the bottom navigation menu and checking if the user is logged in.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. Initializes Firebase, checks login status,
     * and sets up the bottom navigation menu.
     *
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Check login status before setting up Bottom Navigation
        if (!isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Close MainActivity so the user can't go back without logging in
            return;
        }

        // Initialize and set up the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_following_users); // Highlight the correct tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(CommonSpaceActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followees_moods && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, FolloweesMoodsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_following_users && !isCurrentActivity(FollowingUsersActivity.class)) {
                startActivity(new Intent(this, FollowingUsersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    /**
     * Checks if the user is logged in by reading the shared preferences.
     *
     * @return true if the user is logged in, false otherwise.
     */
    private boolean isUserLoggedIn() {
//        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
//        return prefs.getBoolean("is_logged_in", false);
        return false;
    }

    /**
     * Checks if the current activity is the specified activity class.
     *
     * @param activityClass The class of the activity to check.
     * @return true if the current activity is of the specified class, false otherwise.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}
