package com.example.project.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.project.Emotion;
import com.example.project.EmotionData;
import com.example.project.MoodEvent;
import com.example.project.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class mood_mapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private Button btnFilterEmotion;
    private Button btnFilterDate;
    private Button btnClearFilters;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private List<MoodEvent> allMoodEvents = new ArrayList<>();
    private List<MoodEvent> filteredMoodEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_map);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize filter buttons
        btnFilterDate = findViewById(R.id.btnFilterByDate);
        btnFilterEmotion = findViewById(R.id.btnFilterByEmotion);
        btnClearFilters = findViewById(R.id.btnClearFilters);

        // Set click listeners
        btnFilterDate.setOnClickListener(v -> filterByLastWeek());
        btnFilterEmotion.setOnClickListener(v -> showMoodFilterDialog());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        checkLocationPermission();
        // 6) Setup bottom navigation
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
                            if (moodEvent.getLocation() != null && !moodEvent.getLocation().isEmpty()) {
                                allMoodEvents.add(moodEvent);
                            }
                        }

                        // Initially show all mood events
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

    private void updateMapMarkers() {
        // Clear all existing markers
        mMap.clear();

        // Add markers for filtered mood events
        for (MoodEvent moodEvent : filteredMoodEvents) {
            addMarkerForMoodEvent(moodEvent);
        }
    }

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

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

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
                    getLastKnownLocation();
                }
            }
        }
    }

    // Filter methods
    private void showMoodFilterDialog() {
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

    private void filterByMood(Emotion selectedMood) {
        filteredMoodEvents.clear();
        for (MoodEvent mood : allMoodEvents) {
            if (mood.getEmotion() == selectedMood) {
                filteredMoodEvents.add(mood);
            }
        }
        updateMapMarkers();
        Toast.makeText(this, "Filtered by " + selectedMood.name(), Toast.LENGTH_SHORT).show();
    }

    private void filterByLastWeek() {
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

    private void clearFilters() {
        filteredMoodEvents.clear();
        filteredMoodEvents.addAll(allMoodEvents);
        updateMapMarkers();
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }

}