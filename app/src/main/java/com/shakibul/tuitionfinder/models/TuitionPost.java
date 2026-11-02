package com.shakibul.tuitionfinder.models;

public class TuitionPost {

    private String postId;
    private String parentId;
    private String parentName;
    private String parentPhone;
    private String title;
    private String studentClass;
    private String subjects;
    private String area;
    private String gender;      // any / male / female (কোন শিক্ষক চান)
    private long salary;
    private int daysPerWeek;
    private String details;
    private String status;      // open / closed
    private long createdAt;

    // Firestore এর জন্য খালি কনস্ট্রাক্টর বাধ্যতামূলক
    public TuitionPost() {
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public long getSalary() { return salary; }
    public void setSalary(long salary) { this.salary = salary; }

    public int getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(int daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}