package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Activity for adding a new mood event.
 * Allows users to select an emotion, provide a reason, pick an image, and submit the mood event.
 */
public class AddingMoodActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Spinner emotionSpinner;
    private Spinner socialSituationSpinner;
    private EditText reasonEditText;
    private EditText socialSituationEditText;
    private EditText locationEditText;
    private Button submitButton;
    private Button btnPickImage;
    private ImageView imageview;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Uri selectedImageUri; // Add this variable

    /**
     * Called when the activity is first created.
     * Initializes UI elements and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addingmood);
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emotionSpinner = findViewById(R.id.emotionSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);

        // Populate the social situation Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.social_situations, // Reference to the string array
                android.R.layout.simple_spinner_item
        );
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);
        locationEditText = findViewById(R.id.locationEditText);
        submitButton = findViewById(R.id.submitButton);
        ImageButton backButton = findViewById(R.id.backButton);
        btnPickImage = findViewById(R.id.addPhotoButton);
        imageview = findViewById(R.id.photoImageView);

        // Register the result launcher
        registerResult();

        btnPickImage.setOnClickListener(view -> pickImage());

        // Populate Spinner with emotion options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.choices,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(adapter);

        // Handle back button click
        backButton.setOnClickListener(v -> finish());

        // Set OnClickListener for submit button
        submitButton.setOnClickListener(v -> saveMood());
    }

    /**
     * Opens the image picker to select an image.
     */
    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    /**
     * Registers the activity result launcher for image selection.
     */
    private void registerResult() {
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            selectedImageUri = o.getData().getData(); // Store the selected image URI
                            imageview.setImageURI(selectedImageUri);
                        } catch (Exception e) {
                            Toast.makeText(AddingMoodActivity.this, "No image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Checks if the selected image is within the allowed size limit.
     *
     * @param imageUri The URI of the selected image.
     * @param sizeLimit The size limit in bytes.
     * @return True if the image is within the size limit, otherwise false.
     */
    private boolean isImageUnderSizeLimit(Uri imageUri, long sizeLimit) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            int imageSize = inputStream.available(); // Get the size of the image in bytes
            inputStream.close();
            return imageSize <= sizeLimit; // Return true if the image is under the size limit
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Compresses the selected image to reduce its size.
     *
     * @param imageUri The URI of the selected image.
     * @return A compressed Bitmap image.
     */
    private Bitmap compressImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Compress the image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust quality (0-100)
            byte[] compressedImageBytes = outputStream.toByteArray();

            return BitmapFactory.decodeByteArray(compressedImageBytes, 0, compressedImageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Uri saveCompressedImage(Bitmap compressedBitmap) {
        try {
            File file = new File(getFilesDir(), "compressed_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust quality (0-100)
            outputStream.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * saves the updated attributes of the mood and update the firebase
     */
    private void saveMood() {
        // Get user inputs
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        int socialSituationPosition = socialSituationSpinner.getSelectedItemPosition();
        SocialSituation socialSituation = socialSituationPosition >= 0 ? SocialSituation.fromPosition(socialSituationPosition) : null; // Optional
        String location = locationEditText.getText().toString().trim(); // Optional
        Uri photoUri = selectedImageUri; // Optional

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username","Unknown User");

        // Validate required fields
        if (selectedEmotion.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Emotion and Reason are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert emotion string to Emotion enum
        Emotion emotion;
        try {
            emotion = Emotion.valueOf(selectedEmotion.toUpperCase());
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid Emotion Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new MoodEvent with optional fields
        MoodEvent newMood = new MoodEvent(
                username,
                emotion,
                new Date(),
                reason,
                socialSituation, // Optional
                location.isEmpty() ? null : location, // Optional
                photoUri // Optional
        );

        // Save to Firestore
        saveMoodToFirestore(newMood);

        // Send data back to MoodHistoryActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newMood", newMood);
        setResult(RESULT_OK, resultIntent);
        finish(); // Close the activity
    }

    /**
     * Saves the mood event to Firestore.
     *
     * @param moodEvent The mood event to be saved.
     */
    private void saveMoodToFirestore(MoodEvent moodEvent) {
        // Create a map to store mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author",moodEvent.getAuthor());
        moodData.put("emotion", moodEvent.getEmotion().toString());
        moodData.put("date", moodEvent.getDate());
        moodData.put("reason", moodEvent.getReason());
        moodData.put("id",moodEvent.getId());

        // Add optional fields if they are not null
        if (moodEvent.getSocialSituation() != null) {
            moodData.put("socialSituation", moodEvent.getSocialSituation().toString());
        }
        if (moodEvent.getLocation() != null) {
            moodData.put("location", moodEvent.getLocation());
        }
        if (moodEvent.getPhotoUri() != null) {
            moodData.put("photoUrl", moodEvent.getPhotoUri().toString());
        }

        // Add a new document with an auto-generated ID
        db.collection("MoodEvents")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the auto-generated document ID
                    String documentId = documentReference.getId();

                    // Optionally, save the document ID in your MoodEvent object
                    moodEvent.setDocumentId(documentId);

                    Toast.makeText(this, "Mood saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save mood to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}