package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapters.FollowRequestAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * "FollowRequest" activity: lists all PENDING requests to the current user.
 * The user can accept or reject each.
 */
public class FollowRequest extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private FollowRequestAdapter requestAdapter;
    private List<FollowRequestAdapter.RequestItem> pendingRequests;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_requests);

        db = FirebaseFirestore.getInstance();

        recyclerRequests = findViewById(R.id.recyclerFollowRequests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        pendingRequests = new ArrayList<>();

        requestAdapter = new FollowRequestAdapter(pendingRequests, new FollowRequestAdapter.DecisionListener() {
            @Override
            public void onAccept(String fromUser) {
                String currentUser = getCurrentUsername();
                FollowManager.acceptFollowRequest(fromUser, currentUser);
                Toast.makeText(FollowRequest.this, "Accepted: " + fromUser, Toast.LENGTH_SHORT).show();
//                reloadRequests();
                removeRequest(fromUser);
            }

            @Override
            public void onReject(String fromUser) {
                String currentUser = getCurrentUsername();
                FollowManager.rejectFollowRequest(fromUser, currentUser);
                Toast.makeText(FollowRequest.this, "Rejected: " + fromUser, Toast.LENGTH_SHORT).show();
//                reloadRequests();
                removeRequest(fromUser);
            }
        });
        recyclerRequests.setAdapter(requestAdapter);

        // Initial load
        reloadRequests();

        // If you have bottom nav here, set it up
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(FolloweesActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followees && !isCurrentActivity(FolloweesActivity.class)) {
                startActivity(new Intent(this, FolloweesActivity.class));
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
        });
    }

    private void removeRequest(String fromUser) {
        for (int i = 0; i < pendingRequests.size(); i++) {
            if (pendingRequests.get(i).fromUser.equals(fromUser)) {
                pendingRequests.remove(i);
                requestAdapter.notifyItemRemoved(i);
                return;
            }
        }
    }


    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }

    private void reloadRequests() {
        pendingRequests.clear();
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            Toast.makeText(this, "No current user found. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find all PENDING requests "toUser = currentUser"
        db.collection("FollowRequests")
                .whereEqualTo("toUser", currentUser)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        for (int i = 0; i < snap.size(); i++) {
                            Map<String,Object> data = snap.getDocuments().get(i).getData();
                            if (data != null) {
                                String fromUser = (String) data.get("fromUser");
                                if (fromUser != null) {
                                    pendingRequests.add(new FollowRequestAdapter.RequestItem(fromUser));
                                }
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", null);
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_followees) {
            return true;
        } else if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_mood_map) {
            Toast.makeText(this, "Go to Mood Map", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        return false;
    }
}