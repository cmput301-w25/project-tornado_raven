package com.example.project.activities;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the new request-based follow system.
 * - "sendFollowRequest" => creates a doc in "FollowRequests" with status="PENDING"
 * - "acceptFollowRequest" => sets doc to "ACCEPTED" + writes doc in "Follows"
 * - "rejectFollowRequest" => sets doc to "REJECTED"
 */
public class FollowManager {

    /**
     * Send a follow request from user A to user B.
     */
    public static void sendFollowRequest(String fromUser, String toUser) {
        if (fromUser == null || toUser == null || fromUser.equals(toUser)) {
            Log.e("FollowManager", "Invalid follow request");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create doc in "FollowRequests" with status "PENDING"
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("fromUser", fromUser);
        requestData.put("toUser", toUser);
        requestData.put("status", "PENDING");
        requestData.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("FollowRequests")
                .add(requestData)
                .addOnSuccessListener(docRef ->
                        Log.d("FollowManager", "Follow request sent from " + fromUser + " to " + toUser)
                )
                .addOnFailureListener(e ->
                        Log.e("FollowManager", "Failed to send follow request: " + e.getMessage())
                );
    }

    /**
     * Accept the follow request from user A to user B. Then create doc in "Follows" (the final relationship).
     */
    public static void acceptFollowRequest(String fromUser, String toUser) {
        if (fromUser == null || toUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("FollowRequests")
                .whereEqualTo("fromUser", fromUser)
                .whereEqualTo("toUser", toUser)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentReference reqDocRef = snap.getDocuments().get(0).getReference();
                        // Mark request as "ACCEPTED"
                        reqDocRef.update("status", "ACCEPTED")
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FollowManager", "Follow request accepted: " + fromUser + " -> " + toUser)
                                )
                                .addOnFailureListener(e ->
                                        Log.e("FollowManager", "Error updating follow request: " + e.getMessage())
                                );

                        // Now create doc in "Follows"
                        Map<String, Object> followData = new HashMap<>();
                        followData.put("followerUsername", fromUser);
                        followData.put("followedUsername", toUser);
                        followData.put("timestamp", com.google.firebase.Timestamp.now());

                        db.collection("Follows")
                                .add(followData)
                                .addOnSuccessListener(followRef ->
                                        Log.d("FollowManager", fromUser + " now follows " + toUser)
                                )
                                .addOnFailureListener(e ->
                                        Log.e("FollowManager", "Error creating 'Follows' doc: " + e.getMessage())
                                );
                    } else {
                        Log.e("FollowManager", "No PENDING request found to accept.");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FollowManager", "Error retrieving follow request: " + e.getMessage())
                );
    }

    /**
     * Reject the follow request from user A to user B.
     * Set "status"="REJECTED".
     */
    public static void rejectFollowRequest(String fromUser, String toUser) {
        if (fromUser == null || toUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("FollowRequests")
                .whereEqualTo("fromUser", fromUser)
                .whereEqualTo("toUser", toUser)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentReference reqDocRef = snap.getDocuments().get(0).getReference();
                        // Mark request as "REJECTED", or you can do reqDocRef.delete()
                        reqDocRef.update("status", "REJECTED")
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FollowManager", "Follow request rejected.")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("FollowManager", "Error updating follow request: " + e.getMessage())
                                );
                    } else {
                        Log.e("FollowManager", "No PENDING request found to reject.");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FollowManager", "Error searching follow request: " + e.getMessage())
                );
    }

    public static void unfollowUser(String fromUser, String toUser) {
        if (fromUser == null || toUser == null || fromUser.equals(toUser)) {
        return;
    }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Follows")
                .whereEqualTo("followerUsername", fromUser)
                .whereEqualTo("followedUsername", toUser)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        // Delete all matching docs (usually just one)
                        for (DocumentSnapshot doc : snap) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FollowManager", fromUser + " has unfollowed " + toUser);
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e("FollowManager", "Failed to unfollow user: " + e.getMessage())
                                    );
                        }
                    } else {
                        Log.d("FollowManager", "No existing follow doc found for " + fromUser + " -> " + toUser);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FollowManager", "Error searching Follows for unfollow: " + e.getMessage())
                );
    }
}

