package com.inkhornsolutions.foodbox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inkhornsolutions.foodbox.adapters.CartItemsAdapter;
import com.inkhornsolutions.foodbox.models.CartItemsModelClass;

import java.util.ArrayList;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Cart extends AppCompatActivity implements LocationListener, OnLocationUpdatedListener {

    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";

    private RecyclerView rvCartItems;
    private Button btnCheckOut;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String delivery = "45";
    private String restaurant;

    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsAdapter cartItemsAdapter;
    private CartItemsModelClass cartItemsModelClass;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Toolbar toolbar;
    static Cart instance;
    private String name, last_name;
    private double totalDeliveryFee;
    private LocationManager locationManager;

    public static Cart getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        instance = this;

        restaurant = getIntent().getStringExtra("restaurant");
        name = getIntent().getStringExtra("name");
//        last_name = getIntent().getStringExtra("last_name");

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000,
                100, this);
        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        boolean providerAvailable = SmartLocation.with(this).location().state().isAnyProviderAvailable();
        boolean locationServices = SmartLocation.with(this).location().state().locationServicesEnabled();

        if (providerAvailable) {
            if (locationServices) {
                deliveryFee();
            }
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Cart.this);
            alertDialogBuilder
                    .setTitle("Location Required!")
                    .setMessage("Location is disabled in your device. Enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isGPSOn();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
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

//        deliveryFee();

    }

    private void deliveryFee() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait!");
        progressDialog.setMessage("Getting delivery charges...");
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference("Admin")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String deliveryDiscount = snapshot.child("deliveryDiscount").getValue(String.class);
                            int deliveryDiscountAmount = snapshot.child("deliveryDiscountAmount").getValue(Integer.class);

                            if (deliveryDiscount.equals("yes")){

                                tvDeliveryFee.setText("PKR " + deliveryDiscountAmount);
                                tvGrandTotal.setText("PKR" + (allTotalPrice + deliveryDiscountAmount));

                                progressDialog.dismiss();

                                btnCheckOut.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        FirebaseDatabase.getInstance().getReference("Admin").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    String percentage = snapshot.child("percentage").getValue(String.class);
                                                    String available = snapshot.child("available").getValue(String.class);

                                                    if (available.equals("yes")) {
                                                        Intent intent = new Intent(Cart.this, Checkout.class);
                                                        intent.putExtra("name", name);
//                                                        intent.putExtra("last_name", last_name);
                                                        intent.putExtra("total", tvGrandTotal.getText().toString().trim().replace("PKR", ""));
                                                        intent.putExtra("deliveryFee", String.valueOf(deliveryDiscountAmount));
                                                        intent.putExtra("restaurant", restaurant);
                                                        intent.putExtra("subTotal", tvSubTotal.getText().toString().replace("PKR", ""));
                                                        intent.putExtra("available", "yes");

                                                        startActivity(intent);
                                                    } else {
                                                        Intent intent = new Intent(Cart.this, Checkout.class);
                                                        intent.putExtra("name", name);
                                                        intent.putExtra("last_name", last_name);
                                                        intent.putExtra("total", tvGrandTotal.getText().toString().trim().replace("PKR", ""));
                                                        intent.putExtra("deliveryFee", String.valueOf(deliveryDiscountAmount));
                                                        intent.putExtra("restaurant", restaurant);
                                                        intent.putExtra("subTotal", tvSubTotal.getText().toString().replace("PKR", ""));
                                                        intent.putExtra("available", "no");

                                                        startActivity(intent);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                progressDialog.dismiss();

                                            }
                                        });
                                    }
                                });
                            }
                            else {
                                SmartLocation.with(Cart.this).location()
                                        .oneFix()
                                        .start(new OnLocationUpdatedListener() {
                                            @Override
                                            public void onLocationUpdated(Location location) {
                                                if (location != null) {

                                                    Log.d("location", "" + location.getLatitude() + location.getLongitude());

                                                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());

                                                    firebaseFirestore.collection("Restaurants").document(restaurant)
                                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            if (documentSnapshot.exists()) {
                                                                double latitude = documentSnapshot.getDouble("location.latitude");
                                                                double longitude = documentSnapshot.getDouble("location.longitude");

                                                                LatLng destination = new LatLng(latitude, longitude);

                                                                Log.d("location", "" + origin + " " + destination);

                                                                GoogleDirection.withServerKey(DIRECTION_API_KEY)
                                                                        .from(origin)
                                                                        .to(destination)
                                                                        .execute(new DirectionCallback() {
                                                                            @Override
                                                                            public void onDirectionSuccess(@Nullable Direction direction) {
                                                                                if (direction != null && direction.isOK()) {
                                                                                    Route route = direction.getRouteList().get(0);
                                                                                    Leg leg = route.getLegList().get(0);
                                                                                    Info distanceInfo = leg.getDistance();
                                                                                    String distance = distanceInfo.getText().replace("km", "").replace("m", "");

                                                                                    totalDeliveryFee = (Double.parseDouble(distance) * 3) + Double.parseDouble(delivery);

                                                                                    tvDeliveryFee.setText("PKR " + totalDeliveryFee);

                                                                                    tvGrandTotal.setText("PKR" + (allTotalPrice + totalDeliveryFee));

                                                                                    progressDialog.dismiss();

                                                                                    btnCheckOut.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {

                                                                                            FirebaseDatabase.getInstance().getReference("Admin").addValueEventListener(new ValueEventListener() {
                                                                                                @Override
                                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                    if (snapshot.exists()) {
                                                                                                        String percentage = snapshot.child("percentage").getValue(String.class);
                                                                                                        String available = snapshot.child("available").getValue(String.class);

                                                                                                        if (available.equals("yes")) {
                                                                                                            Intent intent = new Intent(Cart.this, Checkout.class);
                                                                                                            intent.putExtra("name", name);
//                                                                                                            intent.putExtra("last_name", last_name);
                                                                                                            intent.putExtra("total", tvGrandTotal.getText().toString().trim().replace("PKR", ""));
                                                                                                            intent.putExtra("deliveryFee", String.valueOf(totalDeliveryFee));
                                                                                                            intent.putExtra("restaurant", restaurant);
                                                                                                            intent.putExtra("subTotal", tvSubTotal.getText().toString().replace("PKR", ""));
                                                                                                            intent.putExtra("available", "yes");

                                                                                                            startActivity(intent);
                                                                                                        } else {
                                                                                                            Intent intent = new Intent(Cart.this, Checkout.class);
                                                                                                            intent.putExtra("name", name);
//                                                                                                            intent.putExtra("last_name", last_name);
                                                                                                            intent.putExtra("total", tvGrandTotal.getText().toString().trim().replace("PKR", ""));
                                                                                                            intent.putExtra("deliveryFee", String.valueOf(totalDeliveryFee));
                                                                                                            intent.putExtra("restaurant", restaurant);
                                                                                                            intent.putExtra("subTotal", tvSubTotal.getText().toString().replace("PKR", ""));
                                                                                                            intent.putExtra("available", "no");

                                                                                                            startActivity(intent);
                                                                                                        }
                                                                                                    }
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                                                    progressDialog.dismiss();

                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                } else {
                                                                                    Log.d("location", direction.getStatus());
                                                                                    progressDialog.dismiss();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onDirectionFailure(@NonNull Throwable t) {
                                                                                Log.d("address: ", "Chala2");
                                                                                progressDialog.dismiss();
                                                                                Snackbar.make(findViewById(android.R.id.content), t.getMessage(), Snackbar.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void isGPSOn() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("TAG", locationSettingsResponse.toString());

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!providerEnabled) {
                    isGPSOn();
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(Cart.this, 1003);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d("TAG", "Error : " + sendEx);
                    }
                }
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
                tvNumberofItems.setText("" + c + " Items");
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
        if (item.getItemId() == R.id.edit_cart) {
            onBackPressed();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("location", "onLocationChanged" + location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("location", "TEMPORARILY_UNAVAILABLE" + status);
                isGPSOn();
                break;
            case LocationProvider.AVAILABLE:
                Log.d("location", "AVAILABLE" + status);
                deliveryFee();

                break;
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d("location", "onProviderEnabled" + provider);
        deliveryFee();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("location", "onProviderDisabled" + provider);
    }

    @Override
    public void onLocationUpdated(Location location) {
        deliveryFee();
    }
}
