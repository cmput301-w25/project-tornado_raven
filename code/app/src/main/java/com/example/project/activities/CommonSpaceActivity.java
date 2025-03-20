package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.GlobalData;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
import com.example.project.adapters.CommonSpaceAdapter;
import com.example.project.adapters.MoodHistoryAdapter;
import com.example.project.dialogs.FilterMoodDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays public moods. Users can view and "Follow" individual moods.
 */
public class CommonSpaceActivity extends AppCompatActivity {

    private RecyclerView recyclerCommonSpace;
    private CommonSpaceAdapter adapter;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;
    private List<String> moodList;
    private List<String> originalList;
    private List<String> userList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_space);

        recyclerCommonSpace = findViewById(R.id.recyclerCommonSpace);
        recyclerCommonSpace.setLayoutManager(new LinearLayoutManager(this));

        originalList = new ArrayList<>();
        moodList = new ArrayList<>();
        userList = new ArrayList<>();

        adapter = new CommonSpaceAdapter(moodList, position -> {
            // follow
            String selectedMood = moodList.get(position);
            GlobalData.followedMoods.add(selectedMood);
            Toast.makeText(this, "Followed mood: " + selectedMood, Toast.LENGTH_SHORT).show();
        });
        recyclerCommonSpace.setAdapter(adapter);

        loadUsersFromFirebase();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_common_space);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);

        Button btnShowLastWeek = findViewById(R.id.btnShowLastWeek);
        Button btnFilterByMood = findViewById(R.id.btnFilterByMood);
        Button btnFilterByKeyword = findViewById(R.id.btnFilterByKeyword);
        Button btnClearFilters = findViewById(R.id.btnClearFilters);

        btnShowLastWeek.setOnClickListener(v -> filterLastWeek());
        btnFilterByMood.setOnClickListener(v -> filterByMood());
        btnFilterByKeyword.setOnClickListener(v -> filterByKeyword());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        EditText searchBox = findViewById(R.id.editSearchName);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

        });
    }

    private void filterByMood() {
        Emotion[] allEmotions = Emotion.values();
        String[] emotionNames = new String[allEmotions.length];
        for (int i = 0; i < allEmotions.length; i++) {
            emotionNames[i] = allEmotions[i].name();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("select emotion")
                .setMultiChoiceItems(emotionNames, null, (dialog, which, isChecked) -> {

                })
                .setPositiveButton("save", (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    List<Integer> selectedPositions = new ArrayList<>();
                    SparseBooleanArray checkedItems = alertDialog.getListView().getCheckedItemPositions();
                    for (int i = 0; i < emotionNames.length; i++) {
                        if (checkedItems.get(i)) {
                            selectedPositions.add(i);
                        }
                    }
                    performMoodFilter(selectedPositions, emotionNames);
                })
                .setNegativeButton("cancel", null)
                .show();
    }

    private void performMoodFilter(List<Integer> selectedPositions, String[] emotionNames) {
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "please select an emotion", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> selectedMoods = new ArrayList<>();
        for (int pos : selectedPositions) {
            selectedMoods.add(emotionNames[pos]);
        }

        List<String> filteredList = new ArrayList<>();
        for (String moodEntry : originalList) {
            String[] parts = moodEntry.split("\\|");
            if (parts.length < 1) continue;

            String entryEmotion = parts[0].trim().toUpperCase();
            if (selectedMoods.contains(entryEmotion)) {
                filteredList.add(moodEntry);
            }
        }

        updateList(filteredList, "selected emotion: " + String.join(", ", selectedMoods));
    }

    /**
     * Filters the user list based on the search query.
     */
    private void filterUsers(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String moodString : originalList) {
            String[] parts = moodString.split("\\|");
            String author = parts[0].trim(); // author
            if (author.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(moodString);
            }
        }
        updateList(filteredList, "User found!");
    }

    /**
     * Loads all users mood info from Firebase Firestore and updates the list.
     */
    private void loadUsersFromFirebase() {
        db.collection("MoodEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    moodList.clear();
                    originalList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String author = document.getString("author");
                        Emotion emotion = Emotion.valueOf(document.getString("emotion"));
                        Date date = document.getDate("date");
                        String reason = document.getString("reason");
                        SocialSituation social = SocialSituation.valueOf(document.getString("socialSituation"));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedd = sdf.format(date);
                        String moodString = String.format(
                                "%s|%s|%s|%s",
                                emotion.name(),
                                formattedd,
                                reason,
                                social.name()
                        );

                        moodList.add(moodString);
                        originalList.add(moodString);
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Loaded " + moodList.size() + " mood events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Handles bottom navigation item selections.
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_common_space) {
            return true;
        } else if (id == R.id.nav_followed_moods) {
            startActivity(new Intent(this, FollowedMoodsActivity.class));
        } else if (id == R.id.nav_my_mood_history) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else if (id == R.id.nav_mood_map) {
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        finish();
        return true;
    }

    /**
     * Filters the mood list to display only moods from the last week.
     */
    private void filterLastWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date lastWeekDate = calendar.getTime();

        List<String> filteredList = new ArrayList<>();
        for (String moodString : originalList) {
            String[] parts = moodString.split("\\|");
            String dateStr = parts[1].trim();
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                if (date.after(lastWeekDate)) {
                    filteredList.add(moodString);
                }
            } catch (ParseException e) {
                Log.e("CommonSpace", "Error parsing date: " + dateStr);
            }
        }
        updateList(filteredList, "Filtered last week");
    }

    /**
     * Filters the mood list to display only moods with "Happy".
     */
//    private void filterByMood() {
//        List<String> temp = new ArrayList<>();
//        for (String s : originalList) {
//            if (s.toLowerCase().contains("happy")) {
//                temp.add(s);
//            }
//        }
//        updateList(temp, "Filtered: happy moods");
//    }


    /**
     * Filters the mood list based on specific keywords (e.g., "job" or "jam").
     */
    private void filterByKeyword() {
        List<String> temp = new ArrayList<>();
        for (String s : originalList) {
            if (s.toLowerCase().contains("job") || s.toLowerCase().contains("jam")) {
                temp.add(s);
            }
        }
        updateList(temp, "Filtered by keywords job/jam");
    }

    /**
     * Clears all applied filters and restores the original mood list.
     */
    private void clearFilters() {
        updateList(originalList, "Cleared filters");
    }

    /**
     * Updates the displayed mood list and shows a toast message.
     */
    private void updateList(List<String> newList, String toast) {
        moodList.clear();
        moodList.addAll(newList);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        if (moodList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }
    }
}
