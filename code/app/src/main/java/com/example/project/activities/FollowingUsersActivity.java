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
import com.example.project.adapters.FolloweesAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowingUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> followeesList;
    private FolloweesAdapter adapter;


//    // dummydata
//    private List<String> mockFollowees() {
//        List<String> users = new ArrayList<>();
//        users.add("Alice");
//        users.add("Bob");
//        users.add("Charlie");
//        users.add("Diana");
//        users.add("Edward");
//        return users;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_users);

        recyclerFollowees = findViewById(R.id.recyclerFollowees);
        recyclerFollowees.setLayoutManager(new LinearLayoutManager(this));

        followeesList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", null);
        adapter = new FolloweesAdapter(this,followeesList,currentUser);
        recyclerFollowees.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFollowees();

        // Bottom nav setup
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    private void loadFollowees() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", null);

        db.collection("Follows")
                .whereEqualTo("followerUsername", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    followeesList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String followeeUsername = doc.getString("followedUsername");
                        if (followeeUsername != null) {
                            followeesList.add(followeeUsername);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No following users", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "load failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Bottom navigation
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            startActivity(new Intent(this, CommonSpaceActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_followees_moods) {
            startActivity(new Intent(this, FolloweesMoodsActivity.class));
            finish();
            return true;
        } else if (id == R.id.nav_following_users) {
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

