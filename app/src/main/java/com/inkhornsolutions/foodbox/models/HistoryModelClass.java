package com.inkhornsolutions.foodbox.models;

import com.google.firebase.Timestamp;

public class HistoryModelClass {
    private String resId, id, pId, itemName, finalPrice, price,  items_Count, status, resName, totalPrice, date, userRating;
    private boolean expanded;
    private Timestamp timeStamp;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public HistoryModelClass() {
    }

    public HistoryModelClass(String resId, String id, String pId, String itemName, String finalPrice, String price, String items_Count, String status, String resName, String totalPrice, String date, String userRating, boolean expanded, Timestamp timeStamp) {
        this.resId = resId;
        this.id = id;
        this.pId = pId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.price = price;
        this.items_Count = items_Count;
        this.status = status;
        this.resName = resName;
        this.totalPrice = totalPrice;
        this.date = date;
        this.userRating = userRating;
        this.expanded = expanded;
        this.timeStamp = timeStamp;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(String finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getItems_Count() {
        return items_Count;
    }

    public void setItems_Count(String items_Count) {
        this.items_Count = items_Count;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }
}
