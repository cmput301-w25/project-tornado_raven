package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.FolloweesMoodsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the three most recent moods of each user that the user follows.
 */
public class FolloweesActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private BottomNavigationView bottomNav;
    private FolloweesMoodsAdapter followeesMoodsAdapter;

    // Example: we store the IDs of the users I follow
    private List<String> followedUserIds;
    // We'll combine all mood events from them
    private List<FolloweesMoodsAdapter.UserMoodItem> userMoodItems = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followees);

        db = FirebaseFirestore.getInstance();

        // Suppose you have a method or stored references. Hardcode for demonstration:
        followedUserIds = new ArrayList<>();
        followedUserIds.add("userIdAlice");
        followedUserIds.add("userIdBob");
        followedUserIds.add("userIdCarla");

        recyclerFollowees = findViewById(R.id.recyclerFollowees);
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));

        // Our custom adapter that displays username + up to 3 moods
        followeesMoodsAdapter = new FolloweesMoodsAdapter(userMoodItems);
        recyclerFollowees.setAdapter(followeesMoodsAdapter);

        // Now load the 3 moods for each followee
        loadFolloweesMoods();

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space); // or whichever you prefer
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    /**
     * For each user ID we follow, fetch last 3 MoodEvents from Firestore and
     * add them to the userMoodItems list for display.
     */
    private void loadFolloweesMoods() {
        userMoodItems.clear();

        for (String userId : followedUserIds) {
            db.collection("MoodEvents")
                    .whereEqualTo("ownerId", userId) // or however you store ownership
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (int i = 0; i < querySnapshot.size(); i++) {
                                MoodEvent me = querySnapshot.getDocuments().get(i).toObject(MoodEvent.class);
                                // Create an item that includes the userId and the mood
                                userMoodItems.add(
                                        new FolloweesMoodsAdapter.UserMoodItem(userId, me)
                                );
                            }
                            followeesMoodsAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading moods for " + userId + ": " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            overridePendingTransition(0,0);
            finish();
            return true;
        } else if (id == R.id.nav_followees) {
            // Not used, we can ignore or jump to some other
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
}
