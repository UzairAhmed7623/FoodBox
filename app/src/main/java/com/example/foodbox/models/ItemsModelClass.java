package com.example.foodbox.models;

import java.io.Serializable;
import java.util.UUID;

public class ItemsModelClass implements Serializable {
    private String UserName, id, itemName, price, imageUri, itemDescription, timestamp;

    public ItemsModelClass() {
    }

    public ItemsModelClass(String userName, String id, String itemName, String price, String imageUri, String itemDescription, String timestamp) {
        UserName = userName;
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.imageUri = imageUri;
        this.itemDescription = itemDescription;
        this.timestamp = timestamp;
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
}
