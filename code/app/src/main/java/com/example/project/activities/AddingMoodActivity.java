package com.example.project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;


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
import java.util.List;
import java.util.Map;

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

    private void saveMood() {
        // Get user inputs
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        int socialSituationPosition = socialSituationSpinner.getSelectedItemPosition();
        SocialSituation socialSituation = socialSituationPosition >= 0 ? SocialSituation.fromPosition(socialSituationPosition) : null;
        String location = locationEditText.getText().toString().trim();
        Uri photoUri = selectedImageUri;

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Unknown User");


        // Validate required fields
        if (selectedEmotion.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Emotion and Reason are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert to enum
        Emotion emotion;
        try {
            emotion = Emotion.valueOf(selectedEmotion.toUpperCase());
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid Emotion Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create MoodEvent
        MoodEvent newMood = new MoodEvent(
                username,
                emotion,
                new Date(),
                reason,
                socialSituation,
                location.isEmpty() ? null : location,
                photoUri
        );


        // ✅ Modern Connectivity Check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        if (network != null && connectivityManager.getNetworkCapabilities(network) != null &&
                connectivityManager.getNetworkCapabilities(network).hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            // Online: Save to Firestore
            saveMoodToFirestore(newMood);
            newMood.setSynced(true);
            finishActivityResult(newMood);
        } else {
            // Offline: Save locally
            newMood.setSynced(false);
            newMood.setPendingOperation("ADD");
            saveMoodLocally(newMood);
            Toast.makeText(this, "Offline! Mood saved locally.", Toast.LENGTH_SHORT).show();
            finishActivityResult(newMood);
        }
    }



    private void saveMoodLocally(MoodEvent mood) {
        SharedPreferences prefs = getSharedPreferences("OfflineMoods", MODE_PRIVATE);
        String existing = prefs.getString("moods", "[]");
        Gson gson = new Gson();
        List<MoodEvent> moodList = gson.fromJson(existing, new TypeToken<List<MoodEvent>>(){}.getType());
        moodList.add(mood);
        prefs.edit().putString("moods", gson.toJson(moodList)).apply();
    }

    private void finishActivityResult(MoodEvent mood) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newMood", mood);
        setResult(RESULT_OK, resultIntent);
        finish();
    }


    private void saveMoodToFirestore(MoodEvent moodEvent) {
        // Create a map to store mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author", moodEvent.getAuthor());
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