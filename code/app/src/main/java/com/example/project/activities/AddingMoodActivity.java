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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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


/**
 * Activity for adding a new mood event.
 * Allows users to select an emotion, provide a reason, pick an image, and submit the mood event.
 */
public class AddingMoodActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Spinner emotionSpinner;
    private Spinner locationSpinner;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1003;
    private Spinner socialSituationSpinner;
    private Spinner privacySpinner;
    private EditText reasonEditText;
    //private EditText socialSituationEditText;
    //private EditText locationEditText;
    private Button submitButton;
    private Button btnPickImage;
    private ImageView imageview;
    private MoodEvent newMood;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Uri selectedImageUri; // Add this variable
    private String currentLocation;
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements
        emotionSpinner = findViewById(R.id.emotionSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        socialSituationSpinner = findViewById(R.id.socialSituationSpinner);
        privacySpinner = findViewById(R.id.privacySpinner);
        locationSpinner=findViewById(R.id.locationspinner);
        // Populate the social situation Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.social_situations, // Reference to the string array
                android.R.layout.simple_spinner_item
        );
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialSituationAdapter);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this, R.array.locationchoice, android.R.layout.simple_spinner_item
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        // Population the privacy level spinner.
        ArrayAdapter<CharSequence> privacyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.privacy_levels,
                android.R.layout.simple_spinner_item
        );
        privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(privacyAdapter);

        //locationEditText = findViewById(R.id.locationEditText);
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
        SocialSituation socialSituation = socialSituationPosition >= 0 ? SocialSituation.fromPosition(socialSituationPosition) : null;
        //String location = locationEditText.getText().toString().trim();
        Uri photoUri = selectedImageUri;
        int choice = locationSpinner.getSelectedItemPosition();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Unknown User");

        //Get the selected privacy type.
        int privacyPosition = privacySpinner.getSelectedItemPosition();
        String[] privacyValues = getResources().getStringArray(R.array.privacy_values); // Get STORAGE values
        String selectedPrivacyValue = privacyValues[privacyPosition];

        // Validate required fields
//        if (selectedEmotion.isEmpty() || reason.isEmpty()) {
//            Toast.makeText(this, "Emotion and Reason are required", Toast.LENGTH_SHORT).show();
//            return;
//        }

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

        // Create MoodEvent
        newMood = new MoodEvent(
                username,
                emotion,
                new Date(),
                reason,
                socialSituation,
                null,
                photoUri,
                selectedPrivacyValue
        );

        if (choice==1){
            checkLocationSettings();
        }else{
//        // Connectivity Check
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        Network network = connectivityManager.getActiveNetwork();

//        if (network != null && connectivityManager.getNetworkCapabilities(network) != null &&
//                connectivityManager.getNetworkCapabilities(network).hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            // Online: Save to Firestore
            saveMoodToFirestore(newMood);}
        newMood.setSynced(true);

//        } else {
//            // Offline: Save locally
//            newMood.setSynced(false);
//            newMood.setPendingOperation("ADD");
//            saveMoodLocally(newMood);
//            Toast.makeText(this, "Offline! Mood saved locally.", Toast.LENGTH_SHORT).show();
//            finishActivityResult(newMood);
//        }
        finishActivityResult(newMood);

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
        // Get user inputs
        newMood.setLocation(currentLocation);
        // Save to Firestore
        saveMoodToFirestore(newMood);
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
        moodData.put("privacyLevel", moodEvent.getPrivacyLevel());

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
                    finishActivityResult(newMood);

                    Toast.makeText(this, "Mood saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save mood to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
