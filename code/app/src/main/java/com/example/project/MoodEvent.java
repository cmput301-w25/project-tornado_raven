package com.example.project;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mood event that tracks an individual's emotional state and associated information.
 * This class stores details like emotion, date, reason, social situation, location, and optional photo URI.
 * It implements Serializable to allow easy storage and passing between components.
 */

public class MoodEvent implements Serializable {
    private Emotion emotion;
    private String id;
    private Date date;
    private String Reason;
    private SocialSituation socialSituation;
    private String documentId;
    private String location;
    private Uri photoUri;
    private String author;
    private String privacyLevel;

    public String getDocumentId() {
        return documentId;
    }
    /**
     * Sets the document ID for the mood event.
     *
     * @param documentId The document ID to be set.
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Offline fields
    private boolean isSynced = false;
    private String pendingOperation = "ADD"; // ADD, EDIT, DELETE

    public MoodEvent() {};//for the firestore

    /**
     * Constructor to create a MoodEvent with required details: emotion, date, reason, social situation, and location.
     * A random ID will be generated.
     *
     * @param emotion The emotion experienced.
     * @param date The date when the mood event occurred.
     * @param reason The reason for the mood.
     * @param socialSituation The social situation at the time of the mood event.
     * @param location The location where the mood event occurred.
     */
    public MoodEvent(Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location) {
        this.emotion = emotion;
        this.date = date;
        this.Reason = reason;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
    }

    public MoodEvent(Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location,String id) {
        this.emotion = emotion;
        this.date = date;
        this.Reason = reason;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Constructor to create a MoodEvent with required details and an optional photo URI.
     *
     * @param emotion The emotion experienced.
     * @param date The date when the mood event occurred.
     * @param reason The reason for the mood.
     * @param socialSituation The social situation at the time of the mood event.
     * @param location The location where the mood event occurred.
     * @param photoUri The URI of the photo taken during the mood event, if any.
     * @param privacyLevel The privacy level of the mood event.
     */

    public MoodEvent(String author, Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location, Uri photoUri, String privacyLevel) {
        this.author = author;
        this.emotion = emotion;
        this.date = date;
        this.Reason = reason;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
        this.photoUri = photoUri;
        this.privacyLevel = privacyLevel;

    }
    /**
     * Gets the URI of the photo associated with this mood event.
     *
     * @return The photo URI.
     */
    public Uri getPhotoUri() {
        return photoUri;
    }
    /**
     * Sets the URI of the photo for this mood event.
     *
     * @param photoUri The URI to set.
     */
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }
    /**
     * Sets the emotion for this mood event.
     *
     * @param emotion The emotion to set.
     */
    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    /**
     * Sets the date for this mood event.
     *
     * @param date The date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }
    /**
     * Sets the social situation for this mood event.
     *
     * @param socialSituation The social situation to set.
     */
    public void setSocialSituation(SocialSituation socialSituation) {
        this.socialSituation = socialSituation;
    }
    /**
     * Sets the reason for this mood event.
     *
     * @param reason The reason to set.
     */
    public void setReason(String reason) {
        this.Reason = reason;
    }
    /**
     * Gets the emotion associated with this mood event.
     *
     * @return The emotion.
     */
    public Emotion getEmotion() {
        return emotion;
    }
    /**
     * Gets the date of this mood event.
     *
     * @return The date.
     */
    public Date getDate() {
        return date;
    }
    /**
     * Gets the reason for this mood event.
     *
     * @return The reason.
     */
    public String getReason() {
        return Reason;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * Gets the social situation associated with this mood event.
     *
     * @return The social situation.
     */


    public SocialSituation getSocialSituation() {
        return socialSituation;
    }
    /**
     * Gets the location of this mood event.
     *
     * @return The location.
     */
    public String getLocation() {
        return location;
    }
    /**
     * Sets the location for this mood event.
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }
    /**
     * Gets the unique ID of this mood event.
     *
     * @return The ID.
     */
    public String getId() {
        return id;
    }


    /**
     * Sets the privacy level for this mood event.
     *
     * @return privacy level.
     */
    public String getPrivacyLevel() { return privacyLevel; }

    /**
     * Sets the privacy level for this mood event.
     *
     * @param privacyLevel to the mood.
     */
    public void setPrivacyLevel(String privacyLevel) { this.privacyLevel = privacyLevel; }


    /**
     * Updates the current mood event with the details of a new mood event.
     *
     * @param newEvent The new mood event to update with.
     */
    public void update(MoodEvent newEvent) {
        this.emotion = newEvent.getEmotion();
        this.date = newEvent.getDate();
        this.Reason = newEvent.getReason();
        this.socialSituation = newEvent.getSocialSituation();
        this.privacyLevel = newEvent.getPrivacyLevel();
    }


    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getPendingOperation() {
        return pendingOperation;
    }

    public void setPendingOperation(String pendingOperation) {
        this.pendingOperation = pendingOperation;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("author", this.getAuthor());
        moodData.put("emotion", this.getEmotion().toString());
        moodData.put("date", this.getDate());
        moodData.put("reason", this.getReason());
        moodData.put("id", this.getId());
        moodData.put("privacyLevel", this.getPrivacyLevel());

        if (this.getSocialSituation() != null) {
            moodData.put("socialSituation", this.getSocialSituation().toString());
        }
        if (this.getLocation() != null) {
            moodData.put("location", this.getLocation());
        }
        if (this.getPhotoUri() != null) {
            moodData.put("photoUrl", this.getPhotoUri().toString());
        }
        return moodData;
    }
}
