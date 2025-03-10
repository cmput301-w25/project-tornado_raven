package com.example.project;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

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
     */
    public MoodEvent(Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location, Uri photoUri) {
        this.emotion = emotion;
        this.date = date;
        this.Reason = reason;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
        this.photoUri = photoUri;
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
     * Updates the current mood event with the details of a new mood event.
     *
     * @param newEvent The new mood event to update with.
     */
    public void update(MoodEvent newEvent) {
        this.emotion = newEvent.getEmotion();
        this.date = newEvent.getDate();
        this.Reason = newEvent.getReason();
        this.socialSituation = newEvent.getSocialSituation();
    }


}
