package com.example.pookies;

// Feedback.java
public class Feedback {
    private String userId;
    private String username;
    private String email;
    private String feedbackType;
    private String description;
    private String feedbackTime;

    public Feedback() {
        // Default constructor required for calls to DataSnapshot.getValue(Feedback.class)
    }

    public Feedback(String userId, String username, String email, String feedbackType, String description, String feedbackTime) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.feedbackType = feedbackType;
        this.description = description;
        this.feedbackTime = feedbackTime;
    }

    // Getters and Setters

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFeedbackTime() { return feedbackTime; }
    public void setFeedbackTime(String feedbackTime) { this.feedbackTime = feedbackTime; }
}

