// EditingMoodActivity.java
package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;

import java.util.Date;

public class EditMoodActivity extends AppCompatActivity {

    private Spinner moodSpinner;
    private EditText reasonEditText, socialSituationEditText, locationEditText;
    private Button saveButton, deleteMoodButton;
    private ImageButton backButton;

    private MoodEvent currentMood;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editingmood);
        moodSpinner = findViewById(R.id.moodSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationEditText = findViewById(R.id.socialSituationEditText);
        locationEditText = findViewById(R.id.locationEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteMoodButton = findViewById(R.id.deleteMoodButton);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("moodEvent")) {
            currentMood = (MoodEvent) intent.getSerializableExtra("moodEvent");
            if (currentMood != null) {
                populateUI(currentMood);
            }
        }

        // buttons
        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveChanges());
        deleteMoodButton.setOnClickListener(v -> deleteMood());
    }

    // set original data
    private void populateUI(MoodEvent mood) {
        reasonEditText.setText(mood.getTrigger());
        socialSituationEditText.setText(mood.getSocialSituation());
        locationEditText.setText(mood.getLocation());

        // spinner selection
        String[] emotions = getResources().getStringArray(R.array.choices);
        for (int i = 0; i < emotions.length; i++) {
            if (mood.getEmotion().name().equalsIgnoreCase(emotions[i])) {
                moodSpinner.setSelection(i);
                break;
            }
        }
    }

    private void saveChanges() {
        String trigger = reasonEditText.getText().toString().trim();
        String socialSituation = socialSituationEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (trigger.isEmpty() || socialSituation.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "errors!please check again", Toast.LENGTH_SHORT).show();
            return;
        }

        //set update
        currentMood.setTrigger(trigger);
        currentMood.setSocialSituation(socialSituation);
        currentMood.setLocation(location);
        String selectedEmotion = moodSpinner.getSelectedItem().toString().toUpperCase();
        currentMood.setEmotion(Emotion.valueOf(selectedEmotion));

        // return data to mood history
        Intent intent = new Intent(this, MoodHistoryActivity.class);
        intent.putExtra("updatedMood", currentMood);
        startActivity(intent);
        finish();


    }

    private void deleteMood() {
        Intent intent = new Intent(this, MoodHistoryActivity.class);
        intent.putExtra("deleteMood", currentMood);
        startActivity(intent);
        finish();
    }
}