package com.example.project.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.project.SocialSituation;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for editing an existing mood event.
 * Allows users to modify mood details, delete the event, or save changes.
 */
public class EditMoodActivity extends AppCompatActivity {

    private Spinner moodSpinner;
    private EditText reasonEditText, locationEditText;
    private Button saveButton, deleteButton;
    private Spinner socialSituationSpinner;
    private ImageButton backButton;

    private MoodEvent currentMood;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created. Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editingmood);

        // Initialize UI components
        moodSpinner = findViewById(R.id.moodSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);
        locationEditText = findViewById(R.id.locationEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        setupSpinners();

        // Retrieve and initialize mood data
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("moodEvent")) {
            currentMood = (MoodEvent) intent.getSerializableExtra("moodEvent");
            if (currentMood != null) {
                initData(currentMood);
            }
        }

        backButton.setOnClickListener(v -> finish());
        deleteButton.setOnClickListener(v -> deleteMood());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    /**
     * Sets up the mood and social situation spinners with appropriate data.
     */
    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.choices,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        ArrayAdapter<SocialSituation> socialSituationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SocialSituation.values()
        );
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);
    }

    /**
     * Initializes the UI fields with existing mood event data.
     *
     * @param mood The mood event to display.
     */
    private void initData(MoodEvent mood) {
        reasonEditText.setText(mood.getReason());
        locationEditText.setText(mood.getLocation());

        String[] emotions = getResources().getStringArray(R.array.choices);
        for (int i = 0; i < emotions.length; i++) {
            if (mood.getEmotion().name().equalsIgnoreCase(emotions[i])) {
                moodSpinner.setSelection(i);
                break;
            }
        }
        SocialSituation socialSituation = mood.getSocialSituation();
        if (socialSituation != null) {
            socialSituationSpinner.setSelection(socialSituation.ordinal());
        }
    }

    /**
     * Saves the modified mood event details and returns the updated event to the calling activity.
     */
    private void saveChanges() {
        String trigger = reasonEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        // Update mood event data
        currentMood.setReason(trigger);
        currentMood.setLocation(location);

        String selectedEmotion = moodSpinner.getSelectedItem().toString().toUpperCase();
        currentMood.setEmotion(Emotion.valueOf(selectedEmotion));

        SocialSituation selectedSocialSituation = (SocialSituation) socialSituationSpinner.getSelectedItem();
        currentMood.setSocialSituation(selectedSocialSituation);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedMood", currentMood);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Deletes the mood event and returns the deleted mood event ID to the calling activity.
     */
    private void deleteMood() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteMoodId", currentMood.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}