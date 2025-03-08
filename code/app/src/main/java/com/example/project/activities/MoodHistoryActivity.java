package com.example.project.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> filteredList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history);

        recyclerView=findViewById(R.id.recyclerMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodHistoryList=new ArrayList<>();
        filteredList = new ArrayList<>();
        //set adapter
        moodHistoryAdapter = new MoodHistoryAdapter(this,filteredList);
        recyclerView.setAdapter(moodHistoryAdapter);

        db = FirebaseFirestore.getInstance();

    // read data from firebase
        loadMoodHistoryFromFirestore();


        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_mood_history); // Highlight the correct tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_followees && !isCurrentActivity(FolloweesActivity.class)) {
                startActivity(new Intent(this, FolloweesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followed_moods && !isCurrentActivity(FollowedMoodsActivity.class)) {
                startActivity(new Intent(this, FollowedMoodsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_my_mood_history) {
                return true; // Already in MoodHistoryActivity
            } else if (id == R.id.nav_profile && !isCurrentActivity(UsersFollowedActivity.class)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
        Button btnFilterByMood = findViewById(R.id.btnFilterByType);
        Button btnShowLastWeek = findViewById(R.id.btnShowLastMonth);
        //Button btnClearFilter = findViewById(R.id.btnClearFilters); // Add a clear filter button
        FloatingActionButton btnAddMood = findViewById(R.id.floating_add_mood_button); // Floating Action Button


        btnFilterByMood.setOnClickListener(v -> showMoodFilterDialog());
        btnShowLastWeek.setOnClickListener(v -> filterByLastWeek());
        //btnClearFilter.setOnClickListener(v -> clearFilters()); // Reset filtering

        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddingMoodActivity.class);
            startActivityForResult(intent, 1); // ✅ Use requestCode 1
        });

    }


    private void loadMoodHistoryFromFirestore() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moodHistoryList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        MoodEvent mood = document.toObject(MoodEvent.class);
                        moodHistoryList.add(mood);
                    }
                    filteredList.clear();
                    filteredList.addAll(moodHistoryList);
                    moodHistoryAdapter.updateList(filteredList); // update RecyclerView
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload mood data failed" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


//    // dummy data
//    private List<MoodEvent> loadMoodHistory() {
//        List<MoodEvent> list = new ArrayList<>();
//        list.add(new MoodEvent(Emotion.HAPPINESS,new Date(), "get money", SocialSituation.ALONE, "home"));
//        list.add(new MoodEvent(Emotion.SADNESS, new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000), "only 5 dollors", SocialSituation.ALONE,"home"));
//        list.add(new MoodEvent(Emotion.CONFUSION, new Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000), "lost my money", SocialSituation.ALONE, "home"));
//
//        return list;
//    }

    @Override
    protected void onResume() {
        super.onResume();
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
                return;
            }
        }
    }
    private void deleteMood(String moodId) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(moodId)) {
                moodHistoryList.remove(i);
                for (int j = 0; j < filteredList.size(); j++) {
                    if (filteredList.get(j).getId().equals(moodId)) {
                        filteredList.remove(j);
                        break;
                    }
                }
                moodHistoryAdapter.notifyItemRemoved(i);
                Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    // ✅ Filter moods by user-selected emotion
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

    // ✅ Show only moods from the last 7 days
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
    // ✅ Clear Filters
    private void clearFilters() {
        filteredList.clear();
        filteredList.addAll(moodHistoryList); // Restore the original list
        moodHistoryAdapter.updateList(filteredList);
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) { // ✅ Check if it's from AddingMoodActivity
//            if (data != null && data.hasExtra("newMood")) {
//                MoodEvent newMood = (MoodEvent) data.getSerializableExtra("newMood");
//                moodHistoryAdapter.addMood(newMood); // ✅ Use the new method to update the list
//                recyclerView.smoothScrollToPosition(0); // Scroll to the top
//            }
            loadMoodHistoryFromFirestore();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.hasExtra("updatedMood")) {
                    MoodEvent updatedMood = (MoodEvent) data.getSerializableExtra("updatedMood");
                    updateMoodItem(updatedMood);
                } else if (data.hasExtra("deleteMoodId")) {
                    String deleteMoodId = data.getStringExtra("deleteMoodId");
                    deleteMood(deleteMoodId);
                }
            }
        }
    }


    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}
