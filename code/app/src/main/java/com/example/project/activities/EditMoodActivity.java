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

import java.io.FileReader;

public class EditMoodActivity extends AppCompatActivity {

    private Spinner moodSpinner;
    private EditText reasonEditText, socialSituationEditText, locationEditText;
    private Button saveButton, deleteButton;
    private Spinner socialSituationSpinner;
    private ImageButton backButton;

    private MoodEvent currentMood;
    private FirebaseFirestore db;
    private int position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editingmood);

        // load components
        moodSpinner = findViewById(R.id.moodSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner=findViewById(R.id.socialSituationSpinner);
        locationEditText = findViewById(R.id.locationEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        setupSpinners();

        //data
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

    private void saveChanges() {
        String trigger = reasonEditText.getText().toString().trim();
//        String socialSituation = socialSituationEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (trigger.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "errors! cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // set data
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
    private void deleteMood() {
        //return deleted id
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleteMoodId", currentMood.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}