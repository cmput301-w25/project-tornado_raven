package com.example.project.activities;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * FollowManager handles all functionality related to following users.
 * It supports:
 * <ul>
 *     <li>Sending follow requests</li>
 *     <li>Accepting follow requests</li>
 *     <li>Rejecting follow requests</li>
 *     <li>Unfollowing users</li>
 * </ul>
 *
 * Firestore collections used:
 * <ul>
 *     <li><b>FollowRequests</b> - Stores pending/accepted/rejected requests.</li>
 *     <li><b>Follows</b> - Stores actual follow relationships after acceptance.</li>
 * </ul>
 */
public class FollowManager {

    /**
     * Sends a follow request from user A to user B.
     * Creates a new document in the "FollowRequests" collection with status "PENDING".
     *
     * @param fromUser The username of the user sending the request.
     * @param toUser   The username of the user to follow.
     */
    public static void sendFollowRequest(String fromUser, String toUser) {
        if (fromUser == null || toUser == null || fromUser.equals(toUser)) {
            Log.e("FollowManager", "Invalid follow request");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
     * Accept the follow request from user A to user B.
     * Then create doc in "Follows" (the final relationship).
     * @param fromUser The user who sent the follow request.
     * @param toUser   The user accepting the request.
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

                        //update request to "ACCEPTED"
                        reqDocRef.update("status", "ACCEPTED")
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FollowManager", "Follow request accepted: " + fromUser + " -> " + toUser)
                                )
                                .addOnFailureListener(e ->
                                        Log.e("FollowManager", "Error updating follow request: " + e.getMessage())
                                );

                        // create follow relationship.
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
     *
     *
     * @param fromUser The user who sent the follow request.
     * @param toUser   The user rejecting the request.
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
    /**
     * Unfollows a user by deleting the relationship document.
     *
     * @param fromUser The user who wants to unfollow.
     * @param toUser   The user to unfollow.
     */
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
                        // Delete all matching FOLLOW RELATIONSHIPS.
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

