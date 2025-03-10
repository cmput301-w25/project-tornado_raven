package com.example.project;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

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

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

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
    public MoodEvent(Emotion emotion, Date date, String reason, SocialSituation socialSituation, String location, Uri photoUri) {
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


}
