package com.example.project.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.project.models.Emotion;
import com.example.project.models.EmotionData;
import com.example.project.models.MoodEvent;
import com.example.project.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Activity that displays a map with markers representing mood events.
 * Supports filtering by emotion, date, location proximity, and followees.
 */
public class mood_mapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private ToggleButton toggleFollowees;
    private Button btnFilterNearbyFollowees;

    private static final double MAX_DISTANCE_KM = 5.0;
    private String currentUser;
    private Button btnFilterEmotion;
    private Button btnFilterDate;
    private Button btnClearFilters;
    private LatLng currentUserLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Circle proximityCircle;

    private List<MoodEvent> allMoodEvents = new ArrayList<>();
    private List<MoodEvent> filteredMoodEvents = new ArrayList<>();
    private Set<String> followeeNames = new HashSet<>();


    /**
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_map);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUser = prefs.getString("username", null);
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in. Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnFilterDate = findViewById(R.id.btnFilterByDate);
        btnFilterEmotion = findViewById(R.id.btnFilterByEmotion);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        toggleFollowees = findViewById(R.id.toggleViewMode);
        btnFilterNearbyFollowees = findViewById(R.id.btnNearbyFollowees);

        // Set click listeners
        btnFilterDate.setOnClickListener(v -> filterByLastWeek());
        btnFilterEmotion.setOnClickListener(v -> showMoodFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());
        btnFilterNearbyFollowees.setOnClickListener(v -> showNearbyFolloweesRecentMoods());
        loadFolloweeNames();
        toggleFollowees.setOnCheckedChangeListener((buttonView, isChecked) -> {
            removeProximityCircle();
            if (isChecked) {
                loadAllFolloweesMoods();
            } else {
                loadMoodEventsWithLocations();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        checkLocationPermission();

        //set up bottom navbar.
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_mood_map);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_common_space && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, CommonSpaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_followees_moods && !isCurrentActivity(FolloweesMoodsActivity.class)) {
                startActivity(new Intent(this, FolloweesMoodsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_following_users) {
                startActivity(new Intent(this, FollowingUsersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }else if (id == R.id.nav_mood_map) {
                return true;
            }else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }


    /**
     * called when the google map is ready.
     * @param  googleMap The googleMap instance.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }

        loadMoodEventsWithLocations();
    }

    /**
     * Loads all mood events created by the current user that include location data.
     */
    private void loadMoodEventsWithLocations() {
        db.collection("MoodEvents")
                .whereNotEqualTo("location", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allMoodEvents.clear();
                        filteredMoodEvents.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            if (moodEvent.getLocation() != null && !moodEvent.getLocation().isEmpty() && Objects.equals(moodEvent.getAuthor(), currentUser)) {
                                allMoodEvents.add(moodEvent);
                            }
                        }

                        filteredMoodEvents.addAll(allMoodEvents);
                        updateMapMarkers();

                        Toast.makeText(this,
                                "Loaded " + allMoodEvents.size() + " mood events with locations",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w("MapActivity", "Error loading documents", task.getException());
                    }
                });
    }


    /**
     * add markers to the map based on filteredMoodEvents.
     */
    private void updateMapMarkers() {
        mMap.clear();
        for (MoodEvent moodEvent : filteredMoodEvents) {
            addMarkerForMoodEvent(moodEvent);
        }
    }


    /**
     *
     * @param moodEvent the mood event to represent.
     */
    private void addMarkerForMoodEvent(MoodEvent moodEvent) {
        try {
            String[] latLng = moodEvent.getLocation().split(",");
            if (latLng.length == 2) {
                LatLng position = new LatLng(
                        Double.parseDouble(latLng[0].trim()),
                        Double.parseDouble(latLng[1].trim()));

                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(moodEvent.getEmotion().name())
                        .snippet(moodEvent.getReason())
                        .icon(getCustomMarkerIcon(moodEvent.getEmotion())));
            }
        } catch (Exception e) {
            Log.e("MapActivity", "Error adding marker for: " + moodEvent.getDocumentId(), e);
        }
    }
    /**
     * Checks if the current activity is the specified activity class.
     */
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }



    private BitmapDescriptor getCustomMarkerIcon(Emotion emotion) {
        try {
            Drawable vectorDrawable = EmotionData.getEmotionIcon(this, emotion);
            Bitmap bitmap = Bitmap.createBitmap(
                    100, 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e("MapActivity", "Error creating marker icon", e);
            return BitmapDescriptorFactory.defaultMarker();
        }
    }


    /**
     * Gets the user's last known location and moves the camera there.
     */
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 12));
                        }
                    });
        }
    }


    /**
     * Requests location permission if not already granted.
     */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * Callback for location permission result.
     * @param requestCode The request code passed in {@link #requestPermissions(
     * android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    getLastKnownLocation();}}}}

    /**
     * display mood options dialog.
     */    private void showMoodFilterDialog() {
        final String[] moods = {"ANGER","CONFUSION","DISGUST","FEAR","HAPPINESS","SADNESS","SHAME","SURPRISE","CLEAR FILTER"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Mood to Filter")
                .setItems(moods, (dialog, which) -> {
                    if (moods[which].equals("CLEAR FILTER")) {
                        clearFilters();
                    } else {
                        filterByMood(Emotion.valueOf(moods[which]));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     *
     * @param selectedMood filter list on the basis of selected mood.
     */
    private void filterByMood(Emotion selectedMood) {
        removeProximityCircle();
        filteredMoodEvents.clear();
        for (MoodEvent mood : allMoodEvents) {
            if (mood.getEmotion() == selectedMood) {
                filteredMoodEvents.add(mood);
            }
        }
        updateMapMarkers();
        Toast.makeText(this, "Filtered by " + selectedMood.name(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Filters mood events to only those from the last week.
     */
    private void filterByLastWeek() {
        removeProximityCircle();
        filteredMoodEvents.clear();
        long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        for (MoodEvent mood : allMoodEvents) {
            if (mood.getDate() != null && mood.getDate().getTime() >= oneWeekAgo) {
                filteredMoodEvents.add(mood);
            }
        }
        updateMapMarkers();
        Toast.makeText(this, "Showing last week's moods", Toast.LENGTH_SHORT).show();
    }

    /**
     * clear filter and display all moods.
     */
    private void clearFilters() {
        removeProximityCircle();
        filteredMoodEvents.clear();
        filteredMoodEvents.addAll(allMoodEvents);
        updateMapMarkers();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads the usernames of all users that the current user is following.
     */
    private void loadFolloweeNames() {
        db.collection("Follows")
                .whereEqualTo("followerUsername", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    followeeNames.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String followedUser = document.getString("followedUsername");
                        if (followedUser != null) {
                            followeeNames.add(followedUser);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load followees", Toast.LENGTH_SHORT).show();
                    Log.e("MapActivity", "Error loading followees", e);
                });
    }


    private void addMarkerForMoodEvent(MoodEvent moodEvent, boolean isFollowee) {
        try {
            String[] latLng = moodEvent.getLocation().split(",");
            if (latLng.length == 2) {
                LatLng position = new LatLng(
                        Double.parseDouble(latLng[0].trim()),
                        Double.parseDouble(latLng[1].trim()));

                String title = isFollowee ?
                        moodEvent.getAuthor() + ": " + moodEvent.getEmotion().name() :
                        moodEvent.getEmotion().name();

                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(title)
                        .snippet(moodEvent.getReason())
                        .icon(getCustomMarkerIcon(moodEvent.getEmotion())));
            }
        } catch (Exception e) {
            Log.e("MapActivity", "Error adding marker", e);
        }
    }

    /**
     * load moods of the users the current user follows.
     */
    private void loadAllFolloweesMoods() {
        if (followeeNames.isEmpty()) {
            Toast.makeText(this, "You're not following anyone yet", Toast.LENGTH_SHORT).show();
            toggleFollowees.setChecked(false);
            return;
        }

        db.collection("MoodEvents")
                .whereNotEqualTo("location", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allMoodEvents.clear();
                        filteredMoodEvents.clear();
                        mMap.clear();
                        Map<String, Integer> followeeMoodCounts = new HashMap<>();
                        for (String followee : followeeNames) {
                            followeeMoodCounts.put(followee, 0);
                        }

                        int loadedCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            if (moodEvent.getLocation() != null &&
                                    !moodEvent.getLocation().isEmpty() &&
                                    followeeNames.contains(moodEvent.getAuthor()) &&
                                    !Objects.equals(moodEvent.getPrivacyLevel(), "PRIVATE")) {
                                String author=moodEvent.getAuthor();
                                if (followeeMoodCounts.get(author) < 3) {
                                    allMoodEvents.add(moodEvent);
                                    addMarkerForMoodEvent(moodEvent, true);
                                    loadedCount++;
                                    followeeMoodCounts.put(author, followeeMoodCounts.get(author) + 1);
                                }

                            }
                        }

                        filteredMoodEvents.addAll(allMoodEvents);
                        Toast.makeText(this,
                                "Loaded " + loadedCount + " moods from all followees",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error loading followees' moods", Toast.LENGTH_SHORT).show();
                        toggleFollowees.setChecked(false);
                        loadMoodEventsWithLocations();
                    }
                });
    }
    /**
     *
     * Remove the circle made in nearby filter
     */
    private void removeProximityCircle() {
        if (proximityCircle != null) {
            proximityCircle.remove();
            proximityCircle = null;
        }
    }



    /**
     *
     * Shows recent moods from followees who are nearby (within 5 km).
     */
    private void showNearbyFolloweesRecentMoods() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Reuse your existing loadAllFolloweesMoods but with distance check
                    if (followeeNames.isEmpty()) {
                        Toast.makeText(this, "You're not following anyone yet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Clear previous markers
                    mMap.clear();

                    // Add the circle (only for nearby followees view)
                    proximityCircle = mMap.addCircle(new CircleOptions()
                            .center(currentUserLocation)
                            .radius(MAX_DISTANCE_KM * 1000)
                            .strokeColor(Color.argb(50, 70, 70, 70))
                            .fillColor(Color.argb(40, 200, 230, 255))
                            .strokeWidth(2f));

                    db.collection("MoodEvents")
                            .whereNotEqualTo("location", null)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    Map<String, MoodEvent> latestEvents = new HashMap<>();

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                        if (moodEvent.getLocation() != null &&
                                                !moodEvent.getLocation().isEmpty() &&
                                                followeeNames.contains(moodEvent.getAuthor()) &&
                                                !Objects.equals(moodEvent.getPrivacyLevel(), "PRIVATE")) {

                                            try {
                                                String[] latLng = moodEvent.getLocation().split(",");
                                                LatLng eventLocation = new LatLng(
                                                        Double.parseDouble(latLng[0].trim()),
                                                        Double.parseDouble(latLng[1].trim()));

                                                float[] results = new float[1];
                                                Location.distanceBetween(
                                                        currentUserLocation.latitude, currentUserLocation.longitude,
                                                        eventLocation.latitude, eventLocation.longitude,
                                                        results);

                                                if ((results[0] / 1000) <= MAX_DISTANCE_KM) {
                                                    // only the most recent event per followee
                                                    MoodEvent existing = latestEvents.get(moodEvent.getAuthor());
                                                    if (existing == null || moodEvent.getDate().after(existing.getDate())) {
                                                        latestEvents.put(moodEvent.getAuthor(), moodEvent);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.e("MapActivity", "Error processing location", e);
                                            }
                                        }
                                    }

                                    for (MoodEvent event : latestEvents.values()) {
                                        addMarkerForMoodEvent(event, true);
                                    }

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            currentUserLocation, 12));

                                    Toast.makeText(this,
                                            "Showing " + latestEvents.size() + " nearby followees' recent moods",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Error loading followees' moods", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

}