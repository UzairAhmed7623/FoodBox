package com.example.foodbox.models;

public class HistoryModelClass {
    private String id, pId, itemName, finalPrice, price,  Items_Count, progress;

    public HistoryModelClass() {
    }

    public HistoryModelClass(String id, String pId, String itemName, String finalPrice, String price, String items_Count, String progress) {
        this.id = id;
        this.pId = pId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.price = price;
        Items_Count = items_Count;
        this.progress = progress;
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
        return Items_Count;
    }

    public void setItems_Count(String items_Count) {
        Items_Count = items_Count;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
}
