package com.example.project;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import java.util.HashMap;
import java.util.Map;

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
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Offline fields
    private boolean isSynced = false;
    private String pendingOperation = "ADD"; // ADD, EDIT, DELETE

    public MoodEvent() {};
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
    public MoodEvent(String author, Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location, Uri photoUri) {
        this.author = author;
        this.emotion = emotion;
        this.date = date;
        this.Reason = reason;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
        this.photoUri = photoUri;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSocialSituation(SocialSituation socialSituation) {
        this.socialSituation = socialSituation;
    }

    public void setReason(String reason) {
        this.Reason = reason;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public Date getDate() {
        return date;
    }

    public String getReason() {
        return Reason;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public SocialSituation getSocialSituation() {
        return socialSituation;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public String getId() {
        return id;
    }
    public void update(MoodEvent newEvent) {
        this.emotion = newEvent.getEmotion();
        this.date = newEvent.getDate();
        this.Reason = newEvent.getReason();
        this.socialSituation = newEvent.getSocialSituation();
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
