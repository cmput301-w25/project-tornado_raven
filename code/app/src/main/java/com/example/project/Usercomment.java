package com.example.project;

import java.util.Date;

public class Usercomment {
    private String commentId;
    private String moodEventId;
    private String author;
    private String content;
    private Date timestamp;

    public Usercomment() {}

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getMoodEventId() { return moodEventId; }
    public void setMoodEventId(String moodEventId) { this.moodEventId = moodEventId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}