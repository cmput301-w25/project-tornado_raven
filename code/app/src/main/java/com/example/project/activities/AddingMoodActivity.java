package com.example.project.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.project.R;
import com.example.project.models.Emotion;
import com.example.project.models.MoodEvent;
import com.example.project.models.SocialSituation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddingMoodActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;
    private static final int MAX_IMAGE_SIZE_BYTES = 65536; // 64KB
    private static final int IMAGE_COMPRESSION_QUALITY_STEP = 5;

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Spinner emotionSpinner;
    private Spinner locationSpinner;
    private Spinner socialSituationSpinner;
    private Spinner privacySpinner;
    private EditText reasonEditText;
    private Button submitButton;
    private Button btnPickImage;
    private ImageView imageview;

    private MoodEvent newMood;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Uri selectedImageUri;
    private String currentLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addingmood);

        initializeFirebase();
        initializeUI();
        setupSpinners();
        setupImagePicker();
        setupButtons();
        registerNetworkCallback();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initializeUI() {
        emotionSpinner = findViewById(R.id.emotionSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);
        privacySpinner = findViewById(R.id.privacySpinner);
        locationSpinner = findViewById(R.id.locationspinner);
        submitButton = findViewById(R.id.submitButton);
        btnPickImage = findViewById(R.id.addPhotoButton);
        imageview = findViewById(R.id.photoImageView);
    }

    private void setupSpinners() {
        // Emotion Spinner
        ArrayAdapter<CharSequence> emotionAdapter = ArrayAdapter.createFromResource(
                this, R.array.choices, android.R.layout.simple_spinner_item);
        emotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(emotionAdapter);

        // Social Situation Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = ArrayAdapter.createFromResource(
                this, R.array.social_situations, android.R.layout.simple_spinner_item);
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);

        // Location Spinner
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this, R.array.locationchoice, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        // Privacy Spinner
        ArrayAdapter<CharSequence> privacyAdapter = ArrayAdapter.createFromResource(
                this, R.array.privacy_levels, android.R.layout.simple_spinner_item);
        privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(privacyAdapter);
    }

    private void setupImagePicker() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if (result.getData() != null && result.getData().getData() != null) {
                            selectedImageUri = result.getData().getData();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImageUri);
                            compressAndDisplayImage(bitmap);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
                        Log.e("ImagePicker", "Error selecting image", e);
                    }
                });
    }

    private void compressAndDisplayImage(Bitmap bitmap) {
        int quality = 100;
        byte[] compressedBytes;

        do {
            compressedBytes = compressImage(bitmap, quality);
            quality -= IMAGE_COMPRESSION_QUALITY_STEP;
        } while (compressedBytes != null &&
                compressedBytes.length > MAX_IMAGE_SIZE_BYTES &&
                quality > 10);

        if (compressedBytes != null && compressedBytes.length <= MAX_IMAGE_SIZE_BYTES) {
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(
                    compressedBytes, 0, compressedBytes.length);
            selectedImageUri = saveCompressedImage(compressedBitmap);
            imageview.setImageBitmap(compressedBitmap);
        } else {
            Toast.makeText(this,
                    "Image too large even after compression. Please choose another image.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        btnPickImage.setOnClickListener(v -> pickImage());
        submitButton.setOnClickListener(v -> saveMood());
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private byte[] compressImage(Bitmap bitmap, int quality) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.e("ImageCompression", "Error compressing image", e);
            return null;
        }
    }

    private Uri saveCompressedImage(Bitmap compressedBitmap) {
        try {
            File file = new File(getFilesDir(), "compressed_image_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            }
            return Uri.fromFile(file);
        } catch (IOException e) {
            Log.e("ImageSaving", "Error saving compressed image", e);
            return null;
        }
    }

    private void saveMood() {
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        int socialSituationPosition = socialSituationSpinner.getSelectedItemPosition();
        SocialSituation socialSituation = socialSituationPosition >= 0 ?
                SocialSituation.fromPosition(socialSituationPosition) : null;

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Unknown User");

        int privacyPosition = privacySpinner.getSelectedItemPosition();
        String[] privacyValues = getResources().getStringArray(R.array.privacy_values);
        String selectedPrivacyValue = privacyValues[privacyPosition];

        if (selectedPrivacyValue.isEmpty()) {
            Toast.makeText(this, "Please choose a privacy level", Toast.LENGTH_SHORT).show();
            return;
        }

        Emotion emotion;
        try {
            emotion = Emotion.valueOf(selectedEmotion.toUpperCase());
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid emotion selected", Toast.LENGTH_SHORT).show();
            return;
        }

        newMood = new MoodEvent(
                username,
                emotion,
                new Date(),
                reason,
                socialSituation,
                null,
                selectedImageUri != null ? selectedImageUri.toString() : null,
                selectedPrivacyValue
        );

        if (locationSpinner.getSelectedItemPosition() == 1) {
            checkLocationSettings();
        } else {
            handleMoodSave(newMood);
        }
    }

    private void handleMoodSave(MoodEvent moodEvent) {
        if (isOnline()) {
            uploadImageAndSaveMood(moodEvent);
        } else {
            saveMoodLocally(moodEvent);
            Toast.makeText(this, "Mood saved locally. Will sync when online.", Toast.LENGTH_SHORT).show();
            finishActivityResult(moodEvent);
        }
    }

    private void uploadImageAndSaveMood(MoodEvent moodEvent) {
        if (selectedImageUri == null) {
            saveMoodToFirestore(moodEvent);
            return;
        }

        String fileName = "mood_images/" + moodEvent.getId() + ".jpg";
        FirebaseStorage.getInstance().getReference(fileName)
                .putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        moodEvent.setPhotoUrl(uri.toString());
                        saveMoodToFirestore(moodEvent);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ImageUpload", "Failed to upload image", e);
                    moodEvent.setPhotoUrl(null);
                    saveMoodToFirestore(moodEvent);
                });
    }

    private void saveMoodToFirestore(MoodEvent moodEvent) {
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author", moodEvent.getAuthor());
        moodData.put("emotion", moodEvent.getEmotion().toString());
        moodData.put("date", moodEvent.getDate());
        moodData.put("reason", moodEvent.getReason());
        moodData.put("id", moodEvent.getId());
        moodData.put("privacyLevel", moodEvent.getPrivacyLevel());
        moodData.put("isSynced", true);

        if (moodEvent.getPhotoUrl() != null && !moodEvent.getPhotoUrl().startsWith("local:")) {
            moodData.put("photoUrl", moodEvent.getPhotoUrl());
        }

        if (moodEvent.getSocialSituation() != null) {
            moodData.put("socialSituation", moodEvent.getSocialSituation().toString());
        }
        if (moodEvent.getLocation() != null) {
            moodData.put("location", moodEvent.getLocation());
        }

        db.collection("MoodEvents")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    moodEvent.setSynced(true);
                    moodEvent.setDocumentId(documentReference.getId());
                    Toast.makeText(this, "Mood saved successfully", Toast.LENGTH_SHORT).show();
                    finishActivityResult(moodEvent);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreSave", "Failed to save mood", e);
                    saveMoodLocally(moodEvent);
                    Toast.makeText(this, "Failed to save online. Saved locally.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveMoodLocally(MoodEvent mood) {
        try {
            SharedPreferences prefs = getSharedPreferences("OfflineMoods", MODE_PRIVATE);
            Gson gson = new Gson();

            List<MoodEvent> moodList = gson.fromJson(
                    prefs.getString("moods", "[]"),
                    new TypeToken<List<MoodEvent>>(){}.getType());

            mood.setSynced(false);
            mood.setPendingOperation("ADD");
            moodList.add(mood);

            prefs.edit().putString("moods", gson.toJson(moodList)).apply();
        } catch (Exception e) {
            Log.e("LocalSave", "Failed to save mood locally", e);
            Toast.makeText(this, "Failed to save mood", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishActivityResult(MoodEvent mood) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newMood", mood);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // Location related methods
    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, response -> getLastLocation());
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e("LocationSettings", "Error showing location settings", sendEx);
                }
            }
        });
    }

    private void getLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location.getLatitude() + ", " + location.getLongitude();
                            newMood.setLocation(currentLocation);
                        }
                        handleMoodSave(newMood);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Location", "Error getting location", e);
                        handleMoodSave(newMood);
                    });
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                handleMoodSave(newMood);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location services required", Toast.LENGTH_SHORT).show();
                handleMoodSave(newMood);
            }
        }
    }

    // Network related methods
    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    syncLocalMoods();
                }
            });
        }
    }

    private void syncLocalMoods() {
        if (!isOnline()) return;

        SharedPreferences prefs = getSharedPreferences("OfflineMoods", MODE_PRIVATE);
        Gson gson = new Gson();

        List<MoodEvent> moodList = gson.fromJson(
                prefs.getString("moods", "[]"),
                new TypeToken<List<MoodEvent>>(){}.getType());

        if (moodList.isEmpty()) return;

        // Clear local data first to avoid duplicates
        prefs.edit().putString("moods", "[]").apply();

        for (MoodEvent mood : moodList) {
            if (!mood.isSynced()) {
                if (mood.getPhotoUrl() != null && mood.getPhotoUrl().startsWith("local:")) {
                    uploadLocalImage(new File(mood.getPhotoUrl().substring(6)), mood);
                } else {
                    saveMoodToFirestore(mood);
                }
            }
        }
    }

    private void uploadLocalImage(File imageFile, MoodEvent mood) {
        if (!imageFile.exists()) {
            mood.setPhotoUrl(null);
            saveMoodToFirestore(mood);
            return;
        }

        String fileName = "mood_images/" + mood.getId() + ".jpg";
        FirebaseStorage.getInstance().getReference(fileName)
                .putFile(Uri.fromFile(imageFile))
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        mood.setPhotoUrl(uri.toString());
                        saveMoodToFirestore(mood);
                        imageFile.delete();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ImageUpload", "Failed to upload local image", e);
                    saveSingleMoodLocally(mood);
                });
    }

    private void saveSingleMoodLocally(MoodEvent mood) {
        SharedPreferences prefs = getSharedPreferences("OfflineMoods", MODE_PRIVATE);
        Gson gson = new Gson();

        List<MoodEvent> moodList = gson.fromJson(
                prefs.getString("moods", "[]"),
                new TypeToken<List<MoodEvent>>(){}.getType());

        moodList.add(mood);
        prefs.edit().putString("moods", gson.toJson(moodList)).apply();
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}