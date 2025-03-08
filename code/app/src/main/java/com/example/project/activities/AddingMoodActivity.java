package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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

public class AddingMoodActivity extends AppCompatActivity {


    private Spinner emotionSpinner;
    private EditText reasonEditText;
    private EditText socialSituationEditText;
    private EditText locationEditText;
    private Button submitButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addingmood);

        // Initialize UI elements
        emotionSpinner = findViewById(R.id.emotionSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationEditText = findViewById(R.id.socialSituationEditText);
        locationEditText = findViewById(R.id.locationEditText);
        submitButton = findViewById(R.id.submitButton);
        ImageButton backButton = findViewById(R.id.backButton); // Back button

        // ✅ Populate Spinner with emotion options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.choices,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(adapter);

        // ✅ Handle back button click
        backButton.setOnClickListener(v -> finish());
        // Set OnClickListener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMood();
            }
        });
    }

    private void saveMood() {
        // Get user inputs
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        String socialSituation = socialSituationEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        // Validate input
        if (selectedEmotion.isEmpty() || reason.isEmpty() || socialSituation.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert emotion string to Emotion enum
        Emotion emotion;
        try {
            emotion = Emotion.valueOf(selectedEmotion.toUpperCase()); // Ensure it matches enum names
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid Emotion Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new MoodEvent
        MoodEvent newMood = new MoodEvent(emotion, new Date(), reason, socialSituation, location);

        // Send data back to MoodHistoryActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newMood", newMood);
        setResult(RESULT_OK, resultIntent);
        finish(); // Close the activity
    }
}
