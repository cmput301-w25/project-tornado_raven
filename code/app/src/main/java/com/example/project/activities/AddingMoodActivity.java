package com.example.project.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.project.Emotion;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.example.project.SocialSituation;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddingMoodActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner emotionSpinner;
    private Spinner socialSituationSpinner;

    private Spinner locationSpinner;

    private Spinner privacySpinner;

    private EditText reasonEditText;
    private EditText locationEditText;
    private Button submitButton;
    private Button btnPickImage;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> resultLauncher;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;
    private Uri selectedImageUri;
    private String currentLocation;
    private MoodEvent newMood; // Store the MoodEvent object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addingmood);
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emotionSpinner = findViewById(R.id.emotionSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);

        locationSpinner = findViewById(R.id.locationspinner);

        privacySpinner = findViewById(R.id.privacySpinner);

        // Populate the social situation Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.social_situations, // Reference to the string array
                android.R.layout.simple_spinner_item
        );
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);

        // Population the privacy level spinner.
        ArrayAdapter<CharSequence> privacyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.privacy_levels,
                android.R.layout.simple_spinner_item
        );
        privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(privacyAdapter);


        locationEditText = findViewById(R.id.locationEditText);
        submitButton = findViewById(R.id.submitButton);
        ImageButton backButton = findViewById(R.id.backButton);
        btnPickImage = findViewById(R.id.addPhotoButton);
        imageView = findViewById(R.id.photoImageView);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Register the result launcher for image selection
        registerResult();

        btnPickImage.setOnClickListener(view -> pickImage());

        // Populate spinners
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this, R.array.locationchoice, android.R.layout.simple_spinner_item
        );
        ArrayAdapter<CharSequence> socialSituationAdapter = ArrayAdapter.createFromResource(
                this, R.array.social_situations, android.R.layout.simple_spinner_item
        );
        ArrayAdapter<CharSequence> emotionAdapter = ArrayAdapter.createFromResource(
                this, R.array.choices, android.R.layout.simple_spinner_item
        );

        emotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(emotionAdapter);
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

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
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData(); // Store the selected image URI
                        imageView.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the mood event to Firestore.
     */
    private void saveMood() {
        // Get user inputs
        String selectedEmotion = emotionSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();
        int socialSituationPosition = socialSituationSpinner.getSelectedItemPosition();
        SocialSituation socialSituation = socialSituationPosition >= 0 ? SocialSituation.fromPosition(socialSituationPosition) : null;
        String location = locationEditText.getText().toString().trim();
        int choice = locationSpinner.getSelectedItemPosition();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Unknown User");


        //Get the selected privacy type.
        int privacyPosition = privacySpinner.getSelectedItemPosition();
        String[] privacyValues = getResources().getStringArray(R.array.privacy_values); // Get STORAGE values
        String selectedPrivacyValue = privacyValues[privacyPosition];


        // Validate required fields
        if (selectedEmotion.isEmpty()) {
            Toast.makeText(this, "Emotion is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPrivacyValue.isEmpty()){
            Toast.makeText(this, "Please choose a privacy level.", Toast.LENGTH_SHORT).show();
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

        // Create MoodEvent object (even if location is required)
        newMood = new MoodEvent(
                username,
                emotion,
                new Date(),
                reason,
                socialSituation,

              
                location.isEmpty() ? null : location,
                selectedImageUri != null ? selectedImageUri.toString() : null
                selectedPrivacyValue
        );

        if (choice == 1) { // Location is required
            checkLocationSettings(); // Fetch location asynchronously
        } else { // Location is not required
            saveMoodToFirestore(newMood); // Save immediately
        }
    }

    /**
     * Checks if location services are enabled.
     */
    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // Location settings are satisfied, proceed with location request
            getLastLocation();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, show a dialog to enable them
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error
                }
            }
        });
    }

    /**
     * Fetches the last known location.
     */
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Use the location
                currentLocation = location.getLatitude() + ", " + location.getLongitude();
                saveMoodWithLocation();
            } else {
                // Request a new location update
                requestNewLocation();
            }
        });
    }

    /**
     * Requests a new location update.
     */
    private void requestNewLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Use the location
                        currentLocation = location.getLatitude() + ", " + location.getLongitude();
                        saveMoodWithLocation();
                        fusedLocationProviderClient.removeLocationUpdates(this); // Stop updates
                        break;
                    }
                }
            }
        }, null);
    }

    /**
     * Saves the mood event with the current location.
     */
    private void saveMoodWithLocation() {
        if (newMood != null) {
            newMood.setLocation(currentLocation); // Update with fetched location
            saveMoodToFirestore(newMood); // Save to Firestore
        }
    }

    /**
     * Saves the mood event to Firestore.
     *
     * @param moodEvent The mood event to be saved.
     */
    private void saveMoodToFirestore(MoodEvent moodEvent) {
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author", moodEvent.getAuthor());
        moodData.put("emotion", moodEvent.getEmotion().toString());
        moodData.put("date", moodEvent.getDate());
        moodData.put("reason", moodEvent.getReason());

        moodData.put("id",moodEvent.getId());
        moodData.put("privacyLevel", moodEvent.getPrivacyLevel());

        // Add optional fields if they are not null
        if (moodEvent.getSocialSituation() != null) {
            moodData.put("socialSituation", moodEvent.getSocialSituation().toString());
        }
        if (moodEvent.getLocation() != null) {
            moodData.put("location", moodEvent.getLocation());
        }
        if (moodEvent.getPhotoUri() != null) {
            moodData.put("photoUrl", moodEvent.getPhotoUri());
        }

        // Add a new document with an auto-generated ID
        db.collection("MoodEvents")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    moodEvent.setDocumentId(documentId);
                    Toast.makeText(this, "Mood saved to Firestore!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save mood to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(); // Retry fetching location after permission is granted
            } else {
                Toast.makeText(this, "Location permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // Location settings enabled, fetch location
                getLastLocation();
            } else {
                Toast.makeText(this, "Location services are required to fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}