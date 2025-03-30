package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.project.R;
import com.google.firebase.FirebaseApp;

/**
 * Main activity of the application. This activity is responsible for handling
 * <ul>
 *     <li>Firebase initialization</li>
 *     <li>Checking if a user is logged in</li>
 *     <li>Navigation to other activities using the bottom navigation bar</li>
 * </ul> */
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
        //redirect to login if not logged in.
        if (!isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); //prevent back navigation.
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
     * Checks if the user is logged.
     * @return true if the user is logged in, false otherwise.
     */
    private boolean isUserLoggedIn() {
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
