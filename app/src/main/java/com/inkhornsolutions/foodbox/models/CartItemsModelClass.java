package com.inkhornsolutions.foodbox.models;

public class CartItemsModelClass {
    private String id, pId, itemName, finalPrice, price,  Items_Count;

    public CartItemsModelClass() {
    }

    public CartItemsModelClass(String id, String pId, String itemName, String finalPrice, String price, String items_Count) {
        this.id = id;
        this.pId = pId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.price = price;
        Items_Count = items_Count;
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
}
