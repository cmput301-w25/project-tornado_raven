package com.example.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.project.models.MoodEvent;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodSyncManager {

    public static void syncOfflineMoods(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("OfflineMoods", Context.MODE_PRIVATE);
        String stored = prefs.getString("moods", "[]");
        Gson gson = new Gson();
        List<MoodEvent> offlineMoods = gson.fromJson(stored, new TypeToken<List<MoodEvent>>(){}.getType());

        if (offlineMoods == null || offlineMoods.isEmpty()) return;

        List<MoodEvent> updatedList = new ArrayList<>();
        for (MoodEvent mood : offlineMoods) {
            if (!mood.isSynced() && "ADD".equals(mood.getPendingOperation())) {
                uploadImageAndSync(context, mood, updatedList);
            } else {
                updatedList.add(mood);
            }
        }

        prefs.edit().putString("moods", gson.toJson(updatedList)).apply();
    }

    private static void uploadImageAndSync(Context context, MoodEvent mood, List<MoodEvent> updatedList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Runnable onSuccess = () -> {
            mood.setSynced(true);
            mood.setPendingOperation(null);
            updatedList.add(mood);
        };

        if (mood.getPhotoUrl() != null && mood.getPhotoUrl().startsWith("file://")) {
            Uri localUri = Uri.parse(mood.getPhotoUrl());
            String fileName = "mood_images/" + mood.getId() + "_" + System.currentTimeMillis() + ".jpg";
            FirebaseStorage.getInstance().getReference(fileName)
                    .putFile(localUri)
                    .addOnSuccessListener(taskSnapshot ->
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                mood.setPhotoUrl(uri.toString());
                                saveMoodToFirestore(db, mood, onSuccess);
                            }))
                    .addOnFailureListener(e -> Log.e("MoodSync", "Image upload failed: " + e.getMessage()));
        } else {
            saveMoodToFirestore(db, mood, onSuccess);
        }
    }

    private static void saveMoodToFirestore(FirebaseFirestore db, MoodEvent mood, Runnable onSuccess) {
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author", mood.getAuthor());
        moodData.put("emotion", mood.getEmotion().toString());
        moodData.put("date", mood.getDate());
        moodData.put("reason", mood.getReason());
        moodData.put("id", mood.getId());
        moodData.put("privacyLevel", mood.getPrivacyLevel());
        if (mood.getSocialSituation() != null) {
            moodData.put("socialSituation", mood.getSocialSituation().toString());
        }
        if (mood.getLocation() != null) {
            moodData.put("location", mood.getLocation());
        }
        if (mood.getPhotoUrl() != null) {
            moodData.put("photoUrl", mood.getPhotoUrl());
        }

        db.collection("MoodEvents")
                .add(moodData)
                .addOnSuccessListener(documentReference -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("MoodSync", "Firestore save failed: " + e.getMessage()));
    }
}
