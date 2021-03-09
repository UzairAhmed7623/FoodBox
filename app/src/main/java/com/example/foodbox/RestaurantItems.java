package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodbox.adapters.CartItemsAdapter;
import com.example.foodbox.adapters.RestaurentItemsAdapter;
import com.example.foodbox.models.CartItemsModelClass;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

        setSupportActionBar(toolbar);

        restaurant = getIntent().getStringExtra("restaurant");
        first_name = getIntent().getStringExtra("first_name");
        last_name = getIntent().getStringExtra("last_name");

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(android.R.id.content), "Minimum order amount is PKR50.", Snackbar.LENGTH_INDEFINITE).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            }
        });

        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()){
                            String name = documentSnapshot.getId();
                            String price = documentSnapshot.get("price").toString();
                            String image = documentSnapshot.get("imageUri").toString();
                            String schedule = documentSnapshot.getString("schedule");

                            ItemsModelClass modelClass = new ItemsModelClass();

                            modelClass.setUserName(first_name+last_name);
                            modelClass.setItemName(name);
                            modelClass.setPrice(price);
                            modelClass.setImageUri(image);
                            modelClass.setId(getDateTime());
                            modelClass.setSchedule(schedule);

                            productList.add(modelClass);

                            rvItems.setAdapter(new RestaurentItemsAdapter(RestaurantItems.this, productList));
                        }
                    }
                }
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
//                firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
//                        .collection("Cart").whereEqualTo("status", "In progress")
//                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isComplete()){
//                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
//                                if (documentSnapshot.exists()){
//                                    status = true;
//                                }
//                            }
//                        }
//                    }
//                });
//                if (status){
//                    Snackbar.make(findViewById(android.R.id.content), "Your orders are already in progress.", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
//                }
//                else {

                Dexter.withContext(RestaurantItems.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            if (badgeCount == 0){
                                Snackbar.make(findViewById(android.R.id.content), "You have not added any product till now!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
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
                            Toast.makeText(RestaurantItems.this, "Please accept the permission!", Toast.LENGTH_SHORT).show();
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