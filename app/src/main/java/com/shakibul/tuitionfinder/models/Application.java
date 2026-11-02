package com.shakibul.tuitionfinder.models;

public class Application {

    private String applicationId;
    private String postId;
    private String postTitle;
    private String parentId;
    private String parentName;
    private String parentPhone;
    private String tutorId;
    private String tutorName;
    private String tutorPhone;
    private String tutorEducation;
    private String tutorSubjects;
    private long tutorExperience;
    private long tutorExpectedSalary;
    private String message;
    private String status;      // pending / accepted / rejected
    private long createdAt;

    public Application() {
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getTutorPhone() { return tutorPhone; }
    public void setTutorPhone(String tutorPhone) { this.tutorPhone = tutorPhone; }

    public String getTutorEducation() { return tutorEducation; }
    public void setTutorEducation(String tutorEducation) { this.tutorEducation = tutorEducation; }

    public String getTutorSubjects() { return tutorSubjects; }
    public void setTutorSubjects(String tutorSubjects) { this.tutorSubjects = tutorSubjects; }

    public long getTutorExperience() { return tutorExperience; }
    public void setTutorExperience(long tutorExperience) { this.tutorExperience = tutorExperience; }

    public long getTutorExpectedSalary() { return tutorExpectedSalary; }
    public void setTutorExpectedSalary(long tutorExpectedSalary) { this.tutorExpectedSalary = tutorExpectedSalary; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    private boolean reviewed;

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
}