package com.example.project;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MoodEvent implements Serializable {
    private Emotion emotion;
    private String id;
    private Date date;
    private String trigger;
    private String socialSituation;
    private String location;

    public MoodEvent(Emotion emotion, Date date, String trigger, String socialSituation, String location) {
        this.emotion = emotion;
        this.date = date;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.location=location;
        this.id = UUID.randomUUID().toString();
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public Date getDate() {
        return date;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getSocialSituation() {
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
        this.trigger = newEvent.getTrigger();
        this.socialSituation = newEvent.getSocialSituation();
    }

}
