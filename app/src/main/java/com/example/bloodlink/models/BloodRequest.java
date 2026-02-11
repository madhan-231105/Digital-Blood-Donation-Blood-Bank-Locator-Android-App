package com.example.bloodlink.models;

public class BloodRequest {

    private String id;               // Firestore document ID
    private String requesterUid;
    private String requesterName;
    private String bloodGroup;
    private String district;
    private String locationMessage;
    private String phone;
    private String status;
    private long timestamp;

    // Required empty constructor for Firestore
    public BloodRequest() {
    }

    // Optional full constructor
    public BloodRequest(String id,
                        String requesterUid,
                        String requesterName,
                        String bloodGroup,
                        String district,
                        String locationMessage,
                        String phone,
                        String status,
                        long timestamp) {

        this.id = id;
        this.requesterUid = requesterUid;
        this.requesterName = requesterName;
        this.bloodGroup = bloodGroup;
        this.district = district;
        this.locationMessage = locationMessage;
        this.phone = phone;
        this.status = status;
        this.timestamp = timestamp;
    }

    // ================= GETTERS =================

    public String getId() {
        return id;
    }

    public String getRequesterUid() {
        return requesterUid;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getDistrict() {
        return district;
    }

    public String getLocationMessage() {
        return locationMessage;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ================= SETTERS =================

    public void setId(String id) {
        this.id = id;
    }

    public void setRequesterUid(String requesterUid) {
        this.requesterUid = requesterUid;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setLocationMessage(String locationMessage) {
        this.locationMessage = locationMessage;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
