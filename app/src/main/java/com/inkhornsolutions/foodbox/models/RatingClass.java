package com.inkhornsolutions.foodbox.models;

public class RatingClass {
    private String id,ItemName,kitchenName,rating;

    public RatingClass() {
    }

    public RatingClass(String id, String itemName, String kitchenName, String rating) {
        this.id = id;
        ItemName = itemName;
        this.kitchenName = kitchenName;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getKitchenName() {
        return kitchenName;
    }

    public void setKitchenName(String kitchenName) {
        this.kitchenName = kitchenName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
