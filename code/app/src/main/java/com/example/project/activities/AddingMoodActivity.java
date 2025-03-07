package com.example.project.activities;

import android.content.Intent;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class AddingMoodActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addingmood);

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

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

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

    // Method 2: Compress the image
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

    // Method 3: Save the compressed image to a file
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

    private void saveMood() {
        // Get user inputs
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        int socialSituationPosition = socialSituationSpinner.getSelectedItemPosition(); // Get selected position
        SocialSituation socialSituation = SocialSituation.fromPosition(socialSituationPosition); // Convert to enum
        String location = locationEditText.getText().toString().trim(); // Optional

        // Validate required fields
        if (selectedEmotion.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Emotion and Reason are required", Toast.LENGTH_SHORT).show();
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

        // Handle image size validation and compression (if applicable)
        Uri finalImageUri = null;
        if (selectedImageUri != null) {
            long sizeLimit = 65536; // 64 KB in bytes
            if (!isImageUnderSizeLimit(selectedImageUri, sizeLimit)) {
                // Compress the image if it exceeds the size limit
                Bitmap compressedBitmap = compressImage(selectedImageUri);
                if (compressedBitmap != null) {
                    finalImageUri = saveCompressedImage(compressedBitmap);
                } else {
                    Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                finalImageUri = selectedImageUri; // Use the original image if it's under the size limit
            }
        }

        // Create new MoodEvent with optional fields
        MoodEvent newMood = new MoodEvent(
                emotion,
                new Date(),
                reason,
                socialSituation, // Selected social situation (enum)
                location.isEmpty() ? null : location, // Optional
                finalImageUri // Optional (can be null if no image is selected)
        );

        // Send data back to MoodHistoryActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newMood", newMood);
        setResult(RESULT_OK, resultIntent);
        finish(); // Close the activity
    }
}