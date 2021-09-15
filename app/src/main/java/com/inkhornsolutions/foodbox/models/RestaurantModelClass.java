package com.inkhornsolutions.foodbox.models;

public class RestaurantModelClass {
    private String resName, status, imageUri, approved, id, noOfOrders, resRating;

    public RestaurantModelClass() {
    }

    public RestaurantModelClass(String resName, String status, String imageUri, String approved, String id, String noOfOrders, String resRating) {
        this.resName = resName;
        this.status = status;
        this.imageUri = imageUri;
        this.approved = approved;
        this.id = id;
        this.noOfOrders = noOfOrders;
        this.resRating = resRating;
    }

    public String getResRating() {
        return resRating;
    }

    public void setResRating(String resRating) {
        this.resRating = resRating;
    }

    public String getNoOfOrders() {
        return noOfOrders;
    }

    public void setNoOfOrders(String noOfOrders) {
        this.noOfOrders = noOfOrders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }
}
