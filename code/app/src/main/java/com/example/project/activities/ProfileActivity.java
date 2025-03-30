package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton addmood_btn;
    private Button logout_btn;

    private List<MoodEvent> filteredList;

    private Button followRequestBtn;
    private TextView followRequestBadge;

    // The username we are displaying
    private String displayedUsername;

    /**
     * Called whn activity starts.Sets up UI,load mood data,
     * config views depending on whether it's current user or other user.
     * @param savedInstanceState If the activity is being re-initiated after previously shut-down
     *                           ,this contains data it most recently supplied.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        db = FirebaseFirestore.getInstance();
        moodHistoryList = new ArrayList<>();

        profileImage     = findViewById(R.id.profileImage);
        profileImage.setImageResource(R.drawable.ic_profile);

        userNameTextView = findViewById(R.id.username);

        recyclerView = findViewById(R.id.recyclerViewRecentMoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMoodHistoryForUser(displayedUsername, displayedUsername.equals(getCurrentUserName()));
        });



        moodHistoryList = new ArrayList<>();
        filteredList = new ArrayList<>();

        userNameTextView = findViewById(R.id.username);

        db = FirebaseFirestore.getInstance();


        // Buttons
        addmood_btn       = findViewById(R.id.add_mood);
        logout_btn        = findViewById(R.id.logout_button);
        followRequestBtn  = findViewById(R.id.follow_request_button);
        followRequestBadge= findViewById(R.id.follow_request_badge);

        //Decide if we’re viewing our own or another user's profile
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

        moodHistoryAdapter = new MoodHistoryAdapter(this, filteredList, displayedUsername.equals(currentUserName));
        recyclerView.setAdapter(moodHistoryAdapter);

        //Load moods
        loadMoodHistoryForUser(displayedUsername, displayedUsername.equals(currentUserName));

        //If it's your own profile, set up button clicks (Logout, Add Mood, FollowRequests)
        logout_btn.setOnClickListener(v -> logoutAndExit());
        addmood_btn.setOnClickListener(v -> {
            Intent addIntent = new Intent(this, AddingMoodActivity.class);
            startActivityForResult(addIntent, 1);
        });
        followRequestBtn.setOnClickListener(v -> {
            Intent reqIntent = new Intent(this, FollowRequest.class);
            startActivity(reqIntent);
        });

        // If it's your own profile then,load follow requests count
        if (displayedUsername.equals(currentUserName)) {
            loadPendingRequestCount();
        }

        //bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followees_moods && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, FolloweesMoodsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_following_users) {
                startActivity(new Intent(this, FollowingUsersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }else if (id == R.id.nav_mood_map) {
                startActivity(new Intent(this, mood_mapActivity.class));
                finish();
                return true;
            }else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        Button btnFilterByMood = findViewById(R.id.btnFilterByType);
        Button btnShowLastWeek = findViewById(R.id.btnShowLastMonth);
        Button btnSearchKeyword = findViewById(R.id.btnSearchKeyword);
        btnSearchKeyword.setOnClickListener(v -> showReasonFilterDialog());


        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());

    }

    /**
     * If displaying moods for user themselves, display all moods
     * If displaying moods for another user,
     * display it based on there following relationship
     * @param username The username of profile being displayed
     * @param isSelf True if the profile belongs to current user.
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
                        // Sort by date desc....
                        moodHistoryList.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                        moodHistoryAdapter.updateList(moodHistoryList);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading your moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);

        } else {
            //Another user's profile,check if currentUser follows them
            String currentUser = getCurrentUserName();

            db.collection("Follows")
                    .whereEqualTo("followerUsername", currentUser)
                    .whereEqualTo("followedUsername", username)
                    .get()
                    .addOnSuccessListener(followSnap -> {
                        boolean isFollowing = !followSnap.isEmpty();

                        if (isFollowing) {
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
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    /**
     * Show how many pending follow requests you have if you're looking at your own profile.
     */
    private void loadPendingRequestCount() {
        String currentUser = getCurrentUserName();
        if (currentUser == null || currentUser.isEmpty()) return;
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
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
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowRequest", "Failed to load request count", e);
                    swipeRefreshLayout.setRefreshing(false);}
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (displayedUsername != null && displayedUsername.equals(getCurrentUserName())) {
            loadPendingRequestCount();
        }
    }

    /**
     * Return the name of the logged-in user from SharedPreferences.
     * @return The username of the current user.
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
     * @param activityClass The activity class to compare against.
     * @return True if the current activity is an instance of the provided class.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }

    private void updateMoodItem(MoodEvent updatedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(updatedMood.getId())) {
                moodHistoryList.set(i, updatedMood);
                for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(updatedMood.getId())) {
                        filteredList.set(j, updatedMood);
                        break;
                    }
                }
                moodHistoryAdapter.updateMood(updatedMood);
                Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();
                db.collection("MoodEvents")
                        .whereEqualTo("id", updatedMood.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                db.collection("MoodEvents").document(documentId)
                                        .set(updatedMood)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "updated successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "updated failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(this, "No corresponding MoodEvent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                return;
            }
        }
    }

    /**
     * Deletes a mood item both from the local list and Firestore.
     *
     * @param moodId The ID of the mood event to delete.
     */
    private void deleteMood(String moodId) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(moodId)) {
                moodHistoryList.remove(i);
                for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(moodId)) {
                        filteredList.remove(j);
                        break;
                    }
                }for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(moodId)) {
                        filteredList.remove(j);
                        break;
                    }
                }
                moodHistoryAdapter.notifyItemRemoved(i);
                Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();

                db.collection("MoodEvents")
                        .whereEqualTo("id", moodId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                db.collection("MoodEvents").document(documentId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "deleted successfully", Toast.LENGTH_SHORT).show();


                                            loadMoodHistoryForUser(displayedUsername, displayedUsername.equals(getCurrentUserName()));
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "deleted failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(this, "No corresponding MoodEvent", Toast.LENGTH_SHORT).show();
                            }
                        });

                return;
            }
        }
    }
    /**
     * Called when returning from another activity, such as adding or editing a mood.
     * Updates the UI or performs deletes based on the result.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An Intent containing any returned data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK && data != null) {
            boolean isSelf = displayedUsername.equals(getCurrentUserName());

            // delete
            if (data.hasExtra("deleteMoodId")) {
                String deleteId = data.getStringExtra("deleteMoodId");
                if (deleteId != null) {
                    deleteMood(deleteId);
                }
            }
            // edit
            else if (data.hasExtra("updatedMood")) {
                loadMoodHistoryForUser(displayedUsername, isSelf);
            }
            // add
            else {
                loadMoodHistoryForUser(displayedUsername, isSelf);
            }
        }
    }


    /**
     * Displays a dialog to let the user filter moods based on emotion type.
     */
    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER","CONFUSION","DISGUST","FEAR","HAPPINESS", "SADNESS","SHAME","SURPRISE","CLEAR FILTER"}; // Add more moods if needed

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Mood to Filter")
                .setItems(moods, (dialog, which) -> {
                    if (moods[which].equals("CLEAR FILTER")) {
                        clearFilters();
                    } else {
                        filterByMood(Emotion.valueOf(moods[which]));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Displays a dialog to filter moods by a reason keyword.
     */
    private void showReasonFilterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter keyword to filter by reason");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Filter", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                filterByReasonKeyword(keyword);
            } else {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Filters the mood history by a given reason keyword.
     *
     * @param keyword The keyword to filter the reasons by.
     */
    private void filterByReasonKeyword(String keyword) {
        filteredList.clear();
        String lowerKeyword = keyword.trim().toLowerCase();

        for (MoodEvent mood : moodHistoryList) {
            String reason = mood.getReason();
            if (reason != null) {
                String lowerReason = reason.toLowerCase();

                // Split the reason into words
                String[] words = lowerReason.split("\\s+");

                boolean matchFound = false;

                for (String word : words) {
                    if (word.startsWith(lowerKeyword) || word.contains(lowerKeyword) || word.endsWith(lowerKeyword)) {
                        matchFound = true;
                        break;
                    }
                }

                if (matchFound) {
                    filteredList.add(mood);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No moods found with reason containing: " + keyword, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Filtered by reason keyword: " + keyword, Toast.LENGTH_SHORT).show();
        }

        moodHistoryAdapter.updateList(filteredList);
    }

    /**
     * Filters the mood history by a specific emotion.
     *
     * @param selectedMood The emotion to filter by.
     */
    private void filterByMood(Emotion selectedMood) {
        filteredList.clear();
        for (MoodEvent mood : moodHistoryList) {
            if (mood.getEmotion() == selectedMood) {
                filteredList.add(mood);
            }
        }
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filtered by " + selectedMood.name(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Filters the mood history to show only the moods from the last 7 days.
     */
    private void filterByLastWeek() {
        filteredList.clear();
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds

        for (MoodEvent mood : moodHistoryList) {
            if (mood.getDate().getTime() >= oneWeekAgo) {
                filteredList.add(mood);
            }
        }

        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Showing last week's moods", Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears all applied filters and restores the original mood list.
     */
    private void clearFilters() {
        filteredList.clear();
        filteredList.addAll(moodHistoryList); // Restore the original list
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

}
