package com.example.project.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.adapters.MoodHistoryAdapter;

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
    }

    // dummy data
    private List<MoodEvent> loadMoodHistory() {
        List<MoodEvent> list = new ArrayList<>();
        moodHistoryList.add(new MoodEvent(Emotion.HAPPINESS,new Date(), "get money", "home"));
        moodHistoryList.add(new MoodEvent(Emotion.SADNESS, new Date(), "only 5 dollors", "home"));
        moodHistoryList.add(new MoodEvent(Emotion.CONFUSION, new Date(), "lost my money", "home"));

        return list;
    }
}
