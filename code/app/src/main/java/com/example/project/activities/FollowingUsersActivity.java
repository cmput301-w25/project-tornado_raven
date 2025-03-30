package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.adapters.FolloweesAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * Activity for displaying the list of users that the logged in user is following.
 * Users are retrieved from the "Follows" collection in Firestore.
 */
public class FollowingUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerFollowees;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> followeesList;
    private FolloweesAdapter adapter;
    private AutoCompleteTextView editSearchUserName;
    private List<String> allUsernames;



    /**
     * Called when the activity is created.
     * Initializes views, sets up the RecyclerView and loads followees from Firestore.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
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

        allUsernames = new ArrayList<>();
        editSearchUserName = findViewById(R.id.editTextSearchUserName);
        editSearchUserName.setThreshold(1);
        editSearchUserName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUser = (String) parent.getItemAtPosition(position);
            if (selectedUser != null && !selectedUser.isEmpty()) {
                Intent intent = new Intent(FollowingUsersActivity.this, ProfileActivity.class);
                intent.putExtra("userName", selectedUser);
                startActivity(intent);
            }
        });


        loadFollowees();
        loadAllUsersForSearch();


        // Bottom navbar setup
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_following_users);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }


    /**
     * Loads the list of users that the current user is following from Firestore.
     * Updates the RecyclerView adapter with the data.
     */
    private void loadAllUsersForSearch() {
        db.collection("users")
                .get()
                .addOnSuccessListener(snap -> {
                    allUsernames.clear();
                    for (DocumentSnapshot doc : snap) {
                        String uname = doc.getString("username");
                        if (uname != null && !uname.isEmpty()) {
                            allUsernames.add(uname);
                        }
                    }
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            allUsernames
                    );
                    editSearchUserName.setAdapter(userAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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
     * Handles bottom navigation menu item selection.
     * Navigates to the corresponding activity based on selected item.
     *
     * @param item The selected menu item.
     * @return True if the item was handled, false otherwise.
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
            startActivity(new Intent(this, mood_mapActivity.class));
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

