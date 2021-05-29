package com.inkhornsolutions.foodbox;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class RestaurantItems extends AppCompatActivity {

    private RecyclerView rvItems;
    private List<ItemsModelClass> productList = new ArrayList<ItemsModelClass>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String first_name,last_name, restaurant;
    private int badgeCount;
    private NotificationBadge notificationBadge;
    private boolean status = false;
    private ImageView cartIcon2;
    static RestaurantItems instance;
    private ProgressDialog progressDialog;
    private HorizontalScrollMenuView menuView;
    private RestaurentItemsAdapter adapter;
    int size;

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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        restaurant = getIntent().getStringExtra("restaurant");
        first_name = getIntent().getStringExtra("first_name");
        last_name = getIntent().getStringExtra("last_name");

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvItems.setLayoutManager(staggeredGridLayoutManager);
        adapter = new RestaurentItemsAdapter(RestaurantItems.this, productList);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getData();

        menuView.addItem("All", R.drawable.all);
        menuView.addItem("Main Course", R.drawable.main_course);
        menuView.addItem("Drinks", R.drawable.soft_drink);
        menuView.addItem("Frozen", R.drawable.frozen);
        menuView.addItem("Sides", R.drawable.salad);
        menuView.addItem("Desserts", R.drawable.desserts);

        menuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
            @Override
            public void onHSMClick(com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem menuItem, int position) {
                if (position != 0){
                    firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                            .whereEqualTo("category", menuItem.getText()).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    productList.clear();
                                    adapter.notifyDataSetChanged();

                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                        if (documentSnapshot.exists()){
                                            String name = documentSnapshot.getId();

                                            ItemsModelClass modelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                            modelClass.setUserName(first_name+last_name);
                                            modelClass.setItemName(name);
//                                          modelClass.setPrice(price);
//                                          modelClass.setImageUri(image);
                                            modelClass.setId(getDateTime());
//                                          modelClass.setSchedule(schedule);

                                            productList.add(modelClass);

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

    private void getData(){
        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        productList.clear();
                        adapter.notifyDataSetChanged();

                        size = queryDocumentSnapshots.size();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getId();

                                ItemsModelClass modelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                modelClass.setUserName(first_name + last_name);
                                modelClass.setItemName(name);
//                                          modelClass.setPrice(price);
//                                          modelClass.setImageUri(image);
                                modelClass.setId(getDateTime());
//                                          modelClass.setSchedule(schedule);
                                modelClass.setListSize(size);

                                productList.add(modelClass);

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
                            if (badgeCount == 0){
                                Snackbar.make(findViewById(android.R.id.content), "You have not added any product till now!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                            else {
                                Intent intent = new Intent(RestaurantItems.this, CartActivity.class);
                                intent.putExtra("restaurant", restaurant);
                                intent.putExtra("first_name", first_name);
                                intent.putExtra("last_name", last_name);
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

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();

        return date;
    }

    public void updateCartCount() {
        if (notificationBadge == null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EasyDB easyDB = EasyDB.init(RestaurantItems.this, "DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("pId", new String[]{"text", "not null"}))
                        .addColumn(new Column("Title", new String[]{"text", "not null"}))
                        .addColumn(new Column("Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                        .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                        .doneTableColumn();

                Cursor res = easyDB.getAllData();
                int c = res.getCount();
                if (c == 0){
                    notificationBadge.setText(String.valueOf(0));
                    notificationBadge.setVisibility(View.INVISIBLE);
                    badgeCount = c;
                }
                else {
                    badgeCount = c;
                    notificationBadge.setVisibility(View.VISIBLE);
                    notificationBadge.setText(String.valueOf(c));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor data = easyDB.getAllData();

        if (data.getCount() != 0){
            AlertDialog.Builder alertDialog = new Builder(RestaurantItems.this);
            alertDialog.setTitle("Cart Alert")
                    .setMessage("If you go back your cart data will be deleted. Would you want to go back?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCartData();
                            RestaurantItems.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .show();
        }
        else {
            super.onBackPressed();
        }
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();
    }
}