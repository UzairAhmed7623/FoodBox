package com.inkhornsolutions.foodbox;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.adapters.DODProductsAdapter;
import com.inkhornsolutions.foodbox.adapters.RestaurentItemsAdapter;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class RestaurantItems extends AppCompatActivity implements RestaurentItemsAdapter.OnImageListener {

    private RecyclerView rvItems;
    private List<ItemsModelClass> productList = new ArrayList<ItemsModelClass>();
    private List<ItemsModelClass> DODProductList = new ArrayList<ItemsModelClass>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    public String name, last_name, restaurant, DOD = "", UFG = "", items = "";
    private int badgeCount;
    private NotificationBadge notificationBadge;
    private boolean status = false;
    private ImageView cartIcon2;
    static RestaurantItems instance;
    private ProgressDialog progressDialog;
    private HorizontalScrollMenuView menuView;
    private RestaurentItemsAdapter adapter;
    private DODProductsAdapter dodProductsAdapter;

    private ItemsModelClass itemsModelClass;
    int size;
    SweetAlertDialog sweetAlertDialog;
    EventListener<DocumentSnapshot> eventListener;
    ListenerRegistration listenerRegistration;

    public static RestaurantItems getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_items);

        instance = this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        menuView = (HorizontalScrollMenuView) findViewById(R.id.rvTags);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        restaurant = getIntent().getStringExtra("restaurant");
        name = getIntent().getStringExtra("name");
        DOD = getIntent().getStringExtra("DOD");
        UFG = getIntent().getStringExtra("UFG");
        items = getIntent().getStringExtra("items");

        getSupportActionBar().setTitle(restaurant);

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvItems.setLayoutManager(staggeredGridLayoutManager);
        adapter = new RestaurentItemsAdapter(RestaurantItems.this, productList, this);

        if (getIntent().getStringExtra("DOD") != null) {
            dodProductsAdapter = new DODProductsAdapter(RestaurantItems.this, DODProductList);
        }
        if (getIntent().getStringExtra("name") != null) {
            deleteCartData();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (getIntent().getStringExtra("DOD") != null) {
            getDODProducts();
            menuView.setVisibility(View.GONE);
            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
        }
        else if (getIntent().getStringExtra("items") != null) {
            getOrganicData();
        } else {
            getData();
            checkRestaurantStatus(restaurant);
            menuView.setVisibility(View.VISIBLE);
        }

        if (getIntent().getStringExtra("items") != null) {

            menuView.addItem("All_Foods", R.drawable.all);
            menuView.addItem("Vegetables", R.drawable.vegetables);
            menuView.addItem("Fruits", R.drawable.fruits);
            menuView.addItem("Flour", R.drawable.flour);
            menuView.addItem("Grains", R.drawable.grains);
            menuView.addItem("SuperFoods", R.drawable.super_foods);
            menuView.addItem("Meat", R.drawable.meat);
            menuView.addItem("Seafood", R.drawable.sea_food);
            menuView.addItem("Eggs", R.drawable.eggs);
            menuView.addItem("Dairy", R.drawable.dairy);
            menuView.addItem("Drinks", R.drawable.drinks);
            menuView.addItem("Ghee", R.drawable.ghee);
            menuView.addItem("Oil", R.drawable.oil);

            menuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
                @Override
                public void onHSMClick(com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem menuItem, int position) {
                    if (position == 0) {
                        menuView.editItem(position, "All_Foods", R.drawable.all_color, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    } else if (position == 1) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(position, "Vegetables", R.drawable.vegetables_coloured, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    } else if (position == 2) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(position, "Fruits", R.drawable.fruits_colored, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    } else if (position == 3) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(position, "Flour", R.drawable.flour_coloured, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    } else if (position == 4) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(position, "Grains", R.drawable.grains_colored, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);

                    } else if (position == 5) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(position, "SuperFoods", R.drawable.super_foods_colored, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);

                    }
                    else if (position == 6) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(position, "Meat", R.drawable.meat_colored, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 7) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(position, "Seafood", R.drawable.sea_food_colored, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 8) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(position, "Eggs", R.drawable.eggs_colored, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 9) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(position, "Dairy", R.drawable.dairy_colored, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 10) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(position, "Drinks", R.drawable.drinks_colored, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 11) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(position, "Ghee", R.drawable.ghee_colored, false, 0);
                        menuView.editItem(12, "Oil", R.drawable.oil, false, 0);
                    }
                    else if (position == 12) {
                        menuView.editItem(0, "All_Foods", R.drawable.all, false, 0);
                        menuView.editItem(1, "Vegetables", R.drawable.vegetables, false, 0);
                        menuView.editItem(2, "Fruits", R.drawable.fruits, false, 0);
                        menuView.editItem(3, "Flour", R.drawable.flour, false, 0);
                        menuView.editItem(4, "Grains", R.drawable.grains, false, 0);
                        menuView.editItem(5, "SuperFoods", R.drawable.super_foods, false, 0);
                        menuView.editItem(6, "Meat", R.drawable.meat, false, 0);
                        menuView.editItem(7, "Seafood", R.drawable.sea_food, false, 0);
                        menuView.editItem(8, "Eggs", R.drawable.eggs, false, 0);
                        menuView.editItem(9, "Dairy", R.drawable.dairy, false, 0);
                        menuView.editItem(10, "Drinks", R.drawable.drinks, false, 0);
                        menuView.editItem(11, "Ghee", R.drawable.ghee, false, 0);
                        menuView.editItem(position, "Oil", R.drawable.oil_colored, false, 0);
                    }

                    if (position != 0) {

                        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                                .whereEqualTo("category", menuItem.getText())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        productList.clear();
                                        adapter.notifyDataSetChanged();

                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            if (documentSnapshot.exists()) {
                                                String name = documentSnapshot.getId();

                                                ItemsModelClass itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                itemsModelClass.setUserName(name);
                                                itemsModelClass.setItemName(name);
                                                itemsModelClass.setId(getDateTime());

                                                productList.add(itemsModelClass);

                                                rvItems.setAdapter(adapter);
                                            }
                                        }
                                        progressDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                    }
                                });
                    }
                    else {
                        getData();
                    }
                }
            });
        }
        else {
            menuView.addItem("All", R.drawable.all);
            menuView.addItem("Main Course", R.drawable.main_course);
            menuView.addItem("Drinks", R.drawable.soft_drink);
            menuView.addItem("Frozen", R.drawable.frozen);
            menuView.addItem("Sides", R.drawable.sides);
            menuView.addItem("Mess", R.drawable.mess);
//        menuView.addItem("Desserts", R.drawable.desserts);

            menuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
                @Override
                public void onHSMClick(com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem menuItem, int position) {
                    if (position == 0) {
                        menuView.editItem(position, "All", R.drawable.all_color, false, 0);
                        menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                        menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                        menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                        menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                        menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                    } else if (position == 1) {
                        menuView.editItem(position, "Main Course", R.drawable.main_course_color, false, 0);
                        menuView.editItem(0, "All", R.drawable.all, false, 0);
                        menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                        menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                        menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                        menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                    } else if (position == 2) {
                        menuView.editItem(position, "Drinks", R.drawable.soft_drink_color, false, 0);
                        menuView.editItem(0, "All", R.drawable.all, false, 0);
                        menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                        menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                        menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                        menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                    } else if (position == 3) {
                        menuView.editItem(position, "Frozen", R.drawable.frozen_color, false, 0);
                        menuView.editItem(0, "All", R.drawable.all, false, 0);
                        menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                        menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                        menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                        menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                    } else if (position == 4) {
                        menuView.editItem(position, "Sides", R.drawable.sides_color, false, 0);
                        menuView.editItem(0, "All", R.drawable.all, false, 0);
                        menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                        menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                        menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                        menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                    } else if (position == 5) {
                        menuView.editItem(position, "Mess", R.drawable.mess_colored, false, 0);
                        menuView.editItem(0, "All", R.drawable.all, false, 0);
                        menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                        menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                        menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                        menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                    }

                    if (getIntent().getStringExtra("DOD") != null) {
                        if (position != 0) {
                            firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                                    .whereEqualTo("category", menuItem.getText())
                                    .whereEqualTo("isDODAvailable", "yes")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            productList.clear();
                                            adapter.notifyDataSetChanged();

                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if (documentSnapshot.exists()) {
                                                    String name = documentSnapshot.getId();

                                                    ItemsModelClass itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                    itemsModelClass.setUserName(name);
                                                    itemsModelClass.setItemName(name);
                                                    itemsModelClass.setId(getDateTime());

                                                    productList.add(itemsModelClass);

                                                    rvItems.setAdapter(adapter);
                                                }
                                            }
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                        }
                                    });
                        } else {
                            getData();
                        }
                    } else if (getIntent().getStringExtra("UFG") != null) {
                        if (position != 0) {
                            firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                                    .whereEqualTo("category", menuItem.getText())
                                    .whereEqualTo("scheduled", "1")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            productList.clear();
                                            adapter.notifyDataSetChanged();

                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if (documentSnapshot.exists()) {
                                                    String name = documentSnapshot.getId();

                                                    ItemsModelClass itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                    itemsModelClass.setUserName(name);
                                                    itemsModelClass.setItemName(name);
                                                    itemsModelClass.setId(getDateTime());

                                                    productList.add(itemsModelClass);

                                                    rvItems.setAdapter(adapter);
                                                }
                                            }
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                        }
                                    });
                        } else {
                            getData();
                        }
                    } else {
                        if (position != 0) {
                            firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                                    .whereEqualTo("category", menuItem.getText())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            productList.clear();
                                            adapter.notifyDataSetChanged();

                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if (documentSnapshot.exists()) {
                                                    String name = documentSnapshot.getId();

                                                    ItemsModelClass itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                    itemsModelClass.setUserName(name);
                                                    itemsModelClass.setItemName(name);
                                                    itemsModelClass.setId(getDateTime());

                                                    productList.add(itemsModelClass);

                                                    rvItems.setAdapter(adapter);
                                                }
                                            }
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                        }
                                    });
                        } else {
                            getData();
                        }
                    }

                }
            });
        }
    }

    private void getOrganicData() {
        firebaseFirestore.collection("Restaurants").document("Organic Shop").collection("Items")
                .whereEqualTo("scheduled", "0")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        productList.clear();
//                        adapter.notifyDataSetChanged();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getId();

                                itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                itemsModelClass.setUFG("no");
                                itemsModelClass.setUserName(name);
                                itemsModelClass.setItemName(name);
                                itemsModelClass.setId(getDateTime());
                                itemsModelClass.setResName("Organic Shop");

                                productList.add(itemsModelClass);

                            }
                        }
                        rvItems.setAdapter(adapter);
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });
    }

    private void getDODProducts() {
//        for (int i=0; i<Common.res.size(); i++){
        firebaseFirestore.collection("Restaurants").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            if (documentSnapshot.exists()) {
                                String resName = documentSnapshot.getId();

                                firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                                        .whereEqualTo("isDODAvailable", "yes")
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot querySnapshot) {
//                    DODProductList.clear();
//                        dodProductsAdapter.notifyDataSetChanged();

                                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                            if (documentSnapshot.exists()) {
                                                String name = documentSnapshot.getId();

                                                itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                itemsModelClass.setUserName(name);
                                                itemsModelClass.setItemName(name);
                                                itemsModelClass.setId(getDateTime());
                                                itemsModelClass.setResName(resName);

                                                DODProductList.add(itemsModelClass);

                                            }
                                        }
                                        rvItems.setAdapter(dodProductsAdapter);
                                        progressDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

//        }
    }

    private void checkRestaurantStatus(String restaurant) {

        if (getIntent().getStringExtra("items") != null) {
            firebaseFirestore.collection("Restaurants").document("Organic Shop").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        if (status != null && status.equals("offline")) {

                            sweetAlertDialog = new SweetAlertDialog(RestaurantItems.this, SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setTitleText("Oops...");
                            sweetAlertDialog.setContentText("Restaurants was closed!");
                            sweetAlertDialog.setCancelable(false);
                            sweetAlertDialog.setConfirmButton("Ok!", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent intent = new Intent(RestaurantItems.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                            sweetAlertDialog.show();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(RestaurantItems.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                }
            });
        } else {
            firebaseFirestore.collection("Restaurants").document(restaurant).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        if (status != null && status.equals("offline")) {

                            sweetAlertDialog = new SweetAlertDialog(RestaurantItems.this, SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setTitleText("Oops...");
                            sweetAlertDialog.setContentText("Restaurants was closed!");
                            sweetAlertDialog.setCancelable(false);
                            sweetAlertDialog.setConfirmButton("Ok!", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent intent = new Intent(RestaurantItems.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                            sweetAlertDialog.show();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(RestaurantItems.this, e.getMessage(), Toasty.LENGTH_LONG).show();
                }
            });
        }


    }

    private void getData() {
        if (getIntent().getStringExtra("UFG") != null && getIntent().getStringExtra("UFG").equals("yes")) {
            firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                    .whereNotEqualTo("scheduled", "0")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            productList.clear();
//                        adapter.notifyDataSetChanged();

                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    String name = documentSnapshot.getId();

                                    itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                    itemsModelClass.setUFG("yes");
                                    itemsModelClass.setUserName(name);
                                    itemsModelClass.setItemName(name);
                                    itemsModelClass.setId(getDateTime());

                                    productList.add(itemsModelClass);

                                }
                            }
                            rvItems.setAdapter(adapter);
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                        }
                    });
        } else {
            firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                    .whereEqualTo("scheduled", "0")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            productList.clear();
//                        adapter.notifyDataSetChanged();

                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    String name = documentSnapshot.getId();

                                    itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                    itemsModelClass.setUFG("no");
                                    itemsModelClass.setUserName(name);
                                    itemsModelClass.setItemName(name);
                                    itemsModelClass.setId(getDateTime());

                                    productList.add(itemsModelClass);

                                }
                            }
                            rvItems.setAdapter(adapter);
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.cart_view, menu);

        MenuItem item1 = menu.findItem(R.id.cartIcon);
        item1.setActionView(R.layout.cart_notification_icon);
        View view = item1.getActionView();
        notificationBadge = (NotificationBadge) view.findViewById(R.id.notificationBadge);
        updateCartCount();
        cartIcon2 = (ImageView) view.findViewById(R.id.cartIcon2);
        cartIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(RestaurantItems.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (badgeCount == 0) {
                            Snackbar.make(findViewById(android.R.id.content), "You have not added any product till now!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.white))
                                    .setTextColor(ContextCompat.getColor(RestaurantItems.this, R.color.myColor)).show();
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
                            String restaurant = sharedPreferences.getString("restName", "");

                            SharedPreferences sharedPreferencesName = getSharedPreferences("userName", MODE_PRIVATE);
                            String name = sharedPreferencesName.getString("name", "User Name");
                            Log.d("resName", restaurant);

                            Intent intent = new Intent(RestaurantItems.this, Cart.class);
                            intent.putExtra("restaurant", restaurant);
                            intent.putExtra("name", name);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(findViewById(android.R.id.content), "Please accept the permission!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
//                }
            }
        });
        return true;
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm", calendar).toString();

        return date;
    }

    public void updateCartCount() {
        if (notificationBadge == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EasyDB easyDB = EasyDB.init(RestaurantItems.this, "scheduledOrdersDatabase")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("pId", new String[]{"text", "not null"}))
                        .addColumn(new Column("Title", new String[]{"text", "not null"}))
                        .addColumn(new Column("Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                        .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                        .addColumn(new Column("orderTime", new String[]{"text", "not null"}))
                        .doneTableColumn();

                Cursor res = easyDB.getAllData();
                int c = res.getCount();
                if (c == 0) {
                    notificationBadge.setText(String.valueOf(0));
                    notificationBadge.setVisibility(View.INVISIBLE);
                    badgeCount = c;
                } else {
                    badgeCount = c;
                    notificationBadge.setVisibility(View.VISIBLE);
                    notificationBadge.setText(String.valueOf(c));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "scheduledOrdersDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .addColumn(new Column("orderTime", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor data = easyDB.getAllData();

        if (data.getCount() != 0) {
            AlertDialog.Builder alertDialog = new Builder(RestaurantItems.this);
            alertDialog.setTitle("Cart Alert")
                    .setMessage("If you go back your cart data will be deleted. Would you want to go back?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCartData();
                            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
                            sharedPreferences.edit().clear().apply();

                            RestaurantItems.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "scheduledOrdersDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .addColumn(new Column("orderTime", new String[]{"text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getStringExtra("DOD") == null) {
            checkRestaurantStatus(restaurant);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    public void onImageClick(int position) {

    }
}