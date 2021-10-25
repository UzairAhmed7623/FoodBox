package com.inkhornsolutions.foodbox.models;

import java.io.Serializable;

public class ItemsModelClass implements Serializable {
    private String UserName, id, itemName, price, imageUri, itemDescription, timestamp, availability, from, to, available, discountPercentage, resName, UFG;

    public ItemsModelClass() {
    }

    public ItemsModelClass(String userName, String id, String itemName, String price, String imageUri, String itemDescription, String timestamp, String availability, String from, String to, String available, String discountPercentage, String resName) {
        UserName = userName;
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.imageUri = imageUri;
        this.itemDescription = itemDescription;
        this.timestamp = timestamp;
        this.availability = availability;
        this.from = from;
        this.to = to;
        this.available = available;
        this.discountPercentage = discountPercentage;
        this.resName = resName;
    }

    public ItemsModelClass(String UFG) {
        this.UFG = UFG;
    }

    public ItemsModelClass(String available, String discountPercentage) {
        this.available = available;
        this.discountPercentage = discountPercentage;
    }

    public String getUFG() {
        return UFG;
    }

    public void setUFG(String UFG) {
        this.UFG = UFG;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(String discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
