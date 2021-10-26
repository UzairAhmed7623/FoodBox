package com.inkhornsolutions.foodbox.models;

public class CartItemsModelClass {
    private String id, pId, itemName, finalPrice, price,  items_Count, imageUri, actualFinalPrice, dateAndTime;

    public CartItemsModelClass() {
    }

    public CartItemsModelClass(String id, String pId, String itemName, String finalPrice, String price, String items_Count, String imageUri, String actualFinalPrice, String dateAndTime) {
        this.id = id;
        this.pId = pId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.price = price;
        this.items_Count = items_Count;
        this.imageUri = imageUri;
        this.actualFinalPrice = actualFinalPrice;
        this.dateAndTime = dateAndTime;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getActualFinalPrice() {
        return actualFinalPrice;
    }

    public void setActualFinalPrice(String actualFinalPrice) {
        this.actualFinalPrice = actualFinalPrice;
    }

    public String getItemImage() {
        return imageUri;
    }

    public void setItemImage(String itemImage) {
        this.imageUri = itemImage;
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
}
