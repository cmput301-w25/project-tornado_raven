package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.MoodHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodEvent> moodHistoryList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history);

        recyclerView=findViewById(R.id.recyclerMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodHistoryList=loadMoodHistory();
        //set adapter
        moodHistoryAdapter = new MoodHistoryAdapter(this,moodHistoryList);
        recyclerView.setAdapter(moodHistoryAdapter);
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
                startActivity(new Intent(this, UsersFollowedActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    // dummy data
    private List<MoodEvent> loadMoodHistory() {
        List<MoodEvent> list = new ArrayList<>();
        list.add(new MoodEvent(Emotion.HAPPINESS,new Date(), "get money", "home"));
        list.add(new MoodEvent(Emotion.SADNESS, new Date(), "only 5 dollors", "home"));
        list.add(new MoodEvent(Emotion.CONFUSION, new Date(), "lost my money", "home"));

        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EditMoodActivity.updatedMoodEvent != null) {
            updateMoodItem(EditMoodActivity.updatedMoodEvent);
            EditMoodActivity.updatedMoodEvent = null;
        }
    }

    private void updateMoodItem(MoodEvent updatedMood) {
        for (int i = 0; i < moodHistoryList.size(); i++) {
            if (moodHistoryList.get(i).getId().equals(updatedMood.getId())) {
                moodHistoryList.set(i, updatedMood);
                moodHistoryAdapter.notifyItemChanged(i);
                Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }
}
