package com.example.project.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.MoodEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class mood_mapActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double userLat , userLng;

    private FirebaseFirestore db;
    private List<MoodEvent> moodEventList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    
}
