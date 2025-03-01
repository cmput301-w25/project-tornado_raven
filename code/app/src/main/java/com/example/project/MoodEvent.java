package com.example.project;

import java.util.Date;

public class MoodEvent {
    private Emotion emotion;
    private Date date;
    private String trigger;
    private String socialSituation;

    public MoodEvent(Emotion emotion, Date date, String trigger, String socialSituation) {
        this.emotion = emotion;
        this.date = date;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
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
}
