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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ProfileActivity displays the user's profile information along with recent mood events.
 * Users can add new moods and navigate to different sections of the application.
 */
public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;
    private FirebaseFirestore db;
    private TextView userNameTextView;
    private ImageView profileImage;
    private Button addmood_btn;
    private Button logout_btn;

    /**
     * Initializes the activity. Loads the user profile and recent mood history,
     * sets up the RecyclerView and bottom navigation menu.
     *
     * @param savedInstanceState The saved instance state, or null if there is none.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        profileImage = findViewById(R.id.profileImage);
        profileImage.setImageResource(R.drawable.ic_profile);


        recyclerView = findViewById(R.id.recyclerViewRecentMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodHistoryList = new ArrayList<>();
        moodHistoryAdapter = new MoodHistoryAdapter(this, moodHistoryList);
        recyclerView.setAdapter(moodHistoryAdapter);
        userNameTextView = findViewById(R.id.username);

        db = FirebaseFirestore.getInstance();

        loadUserProfile();
        loadMoodHistoryFromFirestore();

        logout_btn = findViewById(R.id.logout_button);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // clear all user msg
                editor.apply();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // close current Activity

            }
        });

        addmood_btn = findViewById(R.id.add_mood);
        addmood_btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddingMoodActivity.class);
            startActivityForResult(intent, 1);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(FolloweesActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followed_moods && !isCurrentActivity(FollowedMoodsActivity.class)) {
                startActivity(new Intent(this, FollowedMoodsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_my_mood_history) {
                startActivity(new Intent(this, MoodHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    /**
     * Loads the user's mood history from Firestore and updates the RecyclerView.
     */
    private void loadMoodHistoryFromFirestore() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moodHistoryList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        MoodEvent mood = document.toObject(MoodEvent.class);
                        moodHistoryList.add(mood);
                    }
                    moodHistoryList.sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));
                    moodHistoryAdapter.updateList(moodHistoryList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload mood data failed" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Loads the user's profile information from shared preferences and Firestore.
     */
    private void loadUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", null);
        if (userName == null) {
            userNameTextView.setText("No User Found");
            return;
        }

        userNameTextView.setText(userName);
//        db.collection("users").document(userName)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String username = documentSnapshot.getString("username");
//                        if (username != null && !username.isEmpty()) {
//                            userNameTextView.setText(username);
//                        } else {
//                            userNameTextView.setText("Unknown User");
//                        }
//                    } else {
//                        userNameTextView.setText("No Profile Found");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    userNameTextView.setText("Error Loading Name");
//                    Log.e("FirestoreError", "Failed to load user profile", e);
//                });
    }

    /**
     * Checks if the current activity matches the specified activity class.
     *
     * @param activityClass The activity class to check against.
     * @return True if the current activity matches the specified class, false otherwise.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}
