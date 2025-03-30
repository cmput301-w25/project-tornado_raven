package com.example.project.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Activity for editing an existing mood event.
 * Allows users to modify mood details, delete the event, or save changes.
 * Users can also view the associated photo in full screen and delete or save changes to the mood.
 */
public class EditMoodActivity extends AppCompatActivity {

    private Spinner moodSpinner;
    private Spinner privacySpinner;
    private EditText reasonEditText, locationEditText;
    private Button saveButton, deleteButton, changePhotoBtn;
    private ImageView photoImageView;

    private Spinner socialSituationSpinner;
    private ImageButton backButton;
    private MoodEvent currentMood;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editingmood);

        privacySpinner = findViewById(R.id.privacySpinner);
        moodSpinner = findViewById(R.id.moodSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);
        locationEditText = findViewById(R.id.locationEditText);
        locationEditText.setEnabled(false);
        locationEditText.setFocusable(false);
        locationEditText.setClickable(false);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);
        changePhotoBtn=findViewById(R.id.changePhotoBtn);
        changePhotoBtn.setVisibility(View.GONE);
        photoImageView=findViewById(R.id.photoImageView);
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

        ArrayAdapter<CharSequence> privacyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.privacy_levels,
                android.R.layout.simple_spinner_item
        );
        privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(privacyAdapter);
    }

    /**
     * Initializes the UI fields with existing mood event data.
     *
     * @param mood The mood event to display.
     */
    private void initData(MoodEvent mood) {
        TextView photoUrlText = findViewById(R.id.photoUrlText);

        reasonEditText.setText(mood.getReason());

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

        String moodPrivacy = mood.getPrivacyLevel();
        if (moodPrivacy != null && !moodPrivacy.isEmpty()) {
            String[] privacyValues = getResources().getStringArray(R.array.privacy_values);
            for (int i = 0; i < privacyValues.length; i++) {
                if (privacyValues[i].equalsIgnoreCase(moodPrivacy)) {
                    privacySpinner.setSelection(i);
                    break;
                }
            }
        }
        // displaying photo
        String photoUrl = mood.getPhotoUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            Glide.with(EditMoodActivity.this)
                    .load(photoUrl)
                    .into(photoImageView);
            photoImageView.setVisibility(View.VISIBLE);
            photoImageView.setOnClickListener(v -> showFullImageDialog(EditMoodActivity.this, photoUrl));
        } else {
            photoImageView.setVisibility(View.GONE);
        }
        photoUrlText.setText("Image URL: " + mood.getPhotoUrl());

    }

    /**
     * Opens a dialog displaying the selected image in full screen.
     *
     * @param context  The context to use for displaying the dialog.
     * @param imageUrl The URL of the image to display.
     */
    private void showFullImageDialog(Context context, String imageUrl) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.fullscreeen_image);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView imageView = dialog.findViewById(R.id.enlargedImageView);

        Glide.with(context)
                .load(imageUrl)
                .into(imageView);

        dialog.setCanceledOnTouchOutside(true);
        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Saves the modified mood event details and returns the updated event to the calling activity.
     */
    private void saveChanges() {
        String trigger = reasonEditText.getText().toString().trim();

        currentMood.setReason(trigger);

        String selectedEmotion = moodSpinner.getSelectedItem().toString().toUpperCase();
        currentMood.setEmotion(Emotion.valueOf(selectedEmotion));

        SocialSituation selectedSocialSituation = (SocialSituation) socialSituationSpinner.getSelectedItem();
        currentMood.setSocialSituation(selectedSocialSituation);

        int privacyIndex = privacySpinner.getSelectedItemPosition();
        String[] privacyValues = getResources().getStringArray(R.array.privacy_values);
        String selectedPrivacyLevel = privacyValues[privacyIndex];
        currentMood.setPrivacyLevel(selectedPrivacyLevel);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedMood", currentMood);
        setResult(RESULT_OK, resultIntent);
        finish();

        db.collection("MoodEvents")
                .whereEqualTo("id", currentMood.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("MoodEvents").document(documentId)
                                .set(currentMood)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });

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