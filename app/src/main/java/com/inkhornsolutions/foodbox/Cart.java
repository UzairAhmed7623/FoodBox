package com.inkhornsolutions.foodbox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.inkhornsolutions.foodbox.adapters.CartItemsAdapter;
import com.inkhornsolutions.foodbox.models.CartItemsModelClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class Cart extends AppCompatActivity {

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";

    private RecyclerView rvCartItems;
    private Button btnCheckOut;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String delivery = "45";
    private String restaurant, itemImage;

    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsAdapter cartItemsAdapter;
    private CartItemsModelClass cartItemsModelClass;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private ImageView backArrow;
    private Toolbar toolbar;
    static Cart instance;
    private String first_name, last_name;
    private double totalDeliveryFee;

    public static Cart getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        instance = this;

        restaurant = getIntent().getStringExtra("restaurant");
        first_name = getIntent().getStringExtra("first_name");
        last_name = getIntent().getStringExtra("last_name");

        cartItemsList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        tvSubTotal = (TextView) findViewById(R.id.tvSubTotal);
        tvDeliveryFee = (TextView) findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = (TextView) findViewById(R.id.tvGrandTotal);
        tvNumberofItems = (TextView) findViewById(R.id.tvNumberofItems);
        rvCartItems = (RecyclerView) findViewById(R.id.rvCartItems);
        btnCheckOut = (Button) findViewById(R.id.btnCheckOut);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rvCartItems.setLayoutManager(new LinearLayoutManager(Cart.this));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Cart.this);

        showCart();
    }

    public double allTotalPrice = 0.00;
    public TextView tvSubTotal, tvDeliveryFee, tvGrandTotal, tvNumberofItems;

    private void showCart() {

        EasyDB easyDB = EasyDB.init(Cart.this, "ItemsDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String title = res.getString(3);
            String price = res.getString(4);
            String items_count = res.getString(5);
            String final_price = res.getString(6);
            String imageUri = res.getString(7);

            allTotalPrice = allTotalPrice + Double.parseDouble(final_price);

            cartItemsModelClass = new CartItemsModelClass(
                    "" + id,
                    "" + pId,
                    "" + title,
                    "" + final_price,
                    "" + price,
                    "" + items_count,
                    "" + imageUri
            );

            cartItemsList.add(cartItemsModelClass);
        }

        cartItemsAdapter = new CartItemsAdapter(Cart.this, cartItemsList);

        rvCartItems.setAdapter(cartItemsAdapter);

        updateNumberofItems();

        tvSubTotal.setText("PKR" + String.format("%.2f", allTotalPrice));

        deliveryFee();

    }

    private void deliveryFee() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());

                firebaseFirestore.collection("Restaurants").document(restaurant)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            double latitude = documentSnapshot.getDouble("location.latitude");
                            double longitude = documentSnapshot.getDouble("location.longitude");

                            LatLng destination = new LatLng(latitude, longitude);

                            GoogleDirection.withServerKey(DIRECTION_API_KEY)
                                    .from(origin)
                                    .to(destination)
                                    .execute(new DirectionCallback() {
                                        @Override
                                        public void onDirectionSuccess(@Nullable Direction direction) {
                                            Route route = direction.getRouteList().get(0);
                                            Leg leg = route.getLegList().get(0);
                                            Info distanceInfo = leg.getDistance();
                                            String distance = distanceInfo.getText().replace("km","").replace("m","");

                                            totalDeliveryFee = (Double.parseDouble(distance)*6) + Double.parseDouble(delivery);

                                            tvDeliveryFee.setText("PKR "+totalDeliveryFee);

                                            tvGrandTotal.setText("PKR" + (allTotalPrice + totalDeliveryFee));

                                            btnCheckOut.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent intent = new Intent(Cart.this, Checkout.class);
                                                    intent.putExtra("first_name", first_name);
                                                    intent.putExtra("last_name", last_name);
                                                    intent.putExtra("total", tvGrandTotal.getText().toString().trim().replace("PKR", ""));
                                                    intent.putExtra("deliveryFee", String.valueOf(totalDeliveryFee));
                                                    intent.putExtra("restaurant", restaurant);
                                                    intent.putExtra("subTotal", tvSubTotal.getText().toString().replace("PKR", ""));

                                                    startActivity(intent);

                                                }
                                            });
                                        }

                                        @Override
                                        public void onDirectionFailure(@NonNull Throwable t) {
                                            Log.d("address: ", "Chala2");

                                            Snackbar.make(findViewById(android.R.id.content), t.getMessage(), Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    public void updateNumberofItems() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EasyDB easyDB = EasyDB.init(Cart.this, "ItemsDatabase")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("pId", new String[]{"text", "not null"}))
                        .addColumn(new Column("Title", new String[]{"text", "not null"}))
                        .addColumn(new Column("Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                        .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                        .doneTableColumn();

                Cursor res = easyDB.getAllData();
                int c = res.getCount();
                tvNumberofItems.setText(""+c+" Items");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_cart){
            onBackPressed();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
