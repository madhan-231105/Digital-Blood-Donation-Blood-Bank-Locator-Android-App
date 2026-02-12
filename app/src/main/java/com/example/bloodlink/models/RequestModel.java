package com.example.bloodlink.models;

public class RequestModel {

    private String id;
    private String bloodGroup;
    private String district;
    private String status;
    private String requesterUid;

    public RequestModel() {
        // Required empty constructor for Firestore
    }

    // Getters
    public String getId() { return id; }
    public String getBloodGroup() { return bloodGroup; }
    public String getDistrict() { return district; }
    public String getStatus() { return status; }
    public String getRequesterUid() { return requesterUid; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setDistrict(String district) { this.district = district; }
    public void setStatus(String status) { this.status = status; }
    public void setRequesterUid(String requesterUid) { this.requesterUid = requesterUid; }
}
