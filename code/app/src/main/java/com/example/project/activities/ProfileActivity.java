package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProfileActivity can show:
 *  - Your own profile (with buttons to logout, add mood, see follow-requests)
 *  - Another user's profile (hide those buttons).
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;

    private ImageView profileImage;
    private TextView userNameTextView;

    // Buttons from my_profile.xml
    private Button addmood_btn;
    private Button logout_btn;
    private Button followRequestBtn;
    private TextView followRequestBadge;

    // The username we are displaying
    private String displayedUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        // 1) Initialize Firestore and other fields
        db = FirebaseFirestore.getInstance();
        moodHistoryList = new ArrayList<>();

        // 2) Get references to layout elements
        profileImage     = findViewById(R.id.profileImage);
        profileImage.setImageResource(R.drawable.ic_profile);

        userNameTextView = findViewById(R.id.username);

        recyclerView = findViewById(R.id.recyclerViewRecentMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the adapter
        moodHistoryAdapter = new MoodHistoryAdapter(this, moodHistoryList);
        recyclerView.setAdapter(moodHistoryAdapter);

        // Buttons
        addmood_btn       = findViewById(R.id.add_mood);
        logout_btn        = findViewById(R.id.logout_button);
        followRequestBtn  = findViewById(R.id.follow_request_button);
        followRequestBadge= findViewById(R.id.follow_request_badge);

        // 3) Decide if we’re viewing our own or another user's profile
        // userNameFromIntent is the target username we want to view.
        Intent intent = getIntent();
        String userNameFromIntent = intent.getStringExtra("userName"); // Could be null
        String currentUserName    = getCurrentUserName();

        // If userNameFromIntent is non-null, not empty, and not the same as current => it's "someone else's" profile
        if (userNameFromIntent != null && !userNameFromIntent.isEmpty() && !userNameFromIntent.equals(currentUserName)) {
            displayedUsername = userNameFromIntent;
            // Hide the "Add Mood," "Logout," "Requests" for another user's profile
            addmood_btn.setVisibility(View.GONE);
            logout_btn.setVisibility(View.GONE);
            followRequestBtn.setVisibility(View.GONE);
            followRequestBadge.setVisibility(View.GONE);

        } else {
            // It's your own profile
            displayedUsername = currentUserName;
        }

        // If we have no username => can't load
        if (displayedUsername == null || displayedUsername.isEmpty()) {
            userNameTextView.setText("No user found");
        } else {
            userNameTextView.setText(displayedUsername);
        }

        // 4) Load moods
        loadMoodHistoryForUser(displayedUsername, displayedUsername.equals(currentUserName));

        // 5) If it's your own profile, set up those button clicks (Logout, Add Mood, FollowRequests)
        logout_btn.setOnClickListener(v -> logoutAndExit());
        addmood_btn.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, AddingMoodActivity.class);
            startActivityForResult(addIntent, 1);
        });
        followRequestBtn.setOnClickListener(v -> {
            Intent reqIntent = new Intent(this, FollowRequest.class);
            startActivity(reqIntent);
        });

        // If it's your own profile => load follow requests count
        if (displayedUsername.equals(currentUserName)) {
            loadPendingRequestCount();
        }

        // 6) Setup bottom navigation
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
                return true;
            }
            return false;
        });
    }

    /**
     * If displaying moods for user themselves, display all moods
     * If displaying moods for another user,
     * display it based on there following relationship
     */
    private void loadMoodHistoryForUser(String username, boolean isSelf) {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSelf) {
            db.collection("MoodEvents")
                    .whereEqualTo("author", username)
                    .get()
                    .addOnSuccessListener(snap -> {
                        moodHistoryList.clear();
                        for (DocumentSnapshot doc : snap) {
                            MoodEvent me = doc.toObject(MoodEvent.class);
                            if (me != null) {
                                moodHistoryList.add(me);
                            }
                        }
                        // Sort by date desc
                        moodHistoryList.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                        moodHistoryAdapter.updateList(moodHistoryList);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading your moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } else {
            // 2) Another user's profile => check if currentUser follows them
            String currentUser = getCurrentUserName();

            // Step A: Query "Follows" to see if we have a doc where (followerUsername=currentUser, followedUsername=displayedUsername)
            db.collection("Follows")
                    .whereEqualTo("followerUsername", currentUser)
                    .whereEqualTo("followedUsername", username)
                    .get()
                    .addOnSuccessListener(followSnap -> {
                        boolean isFollowing = !followSnap.isEmpty();  // If doc found => isFollowing = true

                        // Step B: Then decide which privacy levels to load from "MoodEvents"
                        if (isFollowing) {
                            // If following => load "ALL_USERS" + "FOLLOWERS_ONLY"
                            db.collection("MoodEvents")
                                    .whereEqualTo("author", username)
                                    .whereIn("privacyLevel", Arrays.asList("ALL_USERS", "FOLLOWERS_ONLY"))
                                    .get()
                                    .addOnSuccessListener(moodSnap -> {
                                        moodHistoryList.clear();
                                        for (DocumentSnapshot doc : moodSnap) {
                                            MoodEvent me = doc.toObject(MoodEvent.class);
                                            if (me != null) {
                                                moodHistoryList.add(me);
                                            }
                                        }
                                        moodHistoryList.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                                        moodHistoryAdapter.updateList(moodHistoryList);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error loading follow-only moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );

                        } else {
                            // If NOT following => load only "ALL_USERS"
                            db.collection("MoodEvents")
                                    .whereEqualTo("author", username)
                                    .whereEqualTo("privacyLevel", "ALL_USERS")
                                    .get()
                                    .addOnSuccessListener(moodSnap -> {
                                        moodHistoryList.clear();
                                        for (DocumentSnapshot doc : moodSnap) {
                                            MoodEvent me = doc.toObject(MoodEvent.class);
                                            if (me != null) {
                                                moodHistoryList.add(me);
                                            }
                                        }
                                        moodHistoryList.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                                        moodHistoryAdapter.updateList(moodHistoryList);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error loading public moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error checking follow status: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }


    /**
     * Show how many pending follow requests you have if you're looking at your own profile.
     */
    private void loadPendingRequestCount() {
        String currentUser = getCurrentUserName();
        if (currentUser == null || currentUser.isEmpty()) return;

        db.collection("FollowRequests")
                .whereEqualTo("toUser", currentUser)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    if (count > 0) {
                        followRequestBadge.setVisibility(View.VISIBLE);
                        followRequestBadge.setText(String.valueOf(count));
                    } else {
                        followRequestBadge.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FollowRequest", "Failed to load request count", e)
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If it’s your own profile, reload request count in case something changed
        if (displayedUsername != null && displayedUsername.equals(getCurrentUserName())) {
            loadPendingRequestCount();
        }
    }

    /**
     * Return the name of the logged-in user from SharedPreferences.
     */
    private String getCurrentUserName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "");
    }

    /**
     * Logs the current user out, clearing SharedPreferences, and returns to MainActivity.
     */
    private void logoutAndExit() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Checks if the current activity is the specified activity class.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If we added a mood, reload
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // reload after adding a new mood
            loadMoodHistoryForUser(displayedUsername, displayedUsername.equals(getCurrentUserName()));
        }
    }


}
