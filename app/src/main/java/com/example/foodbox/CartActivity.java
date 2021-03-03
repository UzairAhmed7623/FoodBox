package com.example.foodbox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbox.adapters.CartItemsAdapter;
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
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class CartActivity extends AppCompatActivity {

    private TextView tvShopName, Address, UserName;
    private RecyclerView rvCartItems;
    private Button btnCheckOut;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String delivery = "45";
    private String restaurant, add;

    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsAdapter cartItemsAdapter;
    private CartItemsModelClass cartItemsModelClass;

    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        restaurant = getIntent().getStringExtra("restaurant");

        cartItemsList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        UserName = (TextView) findViewById(R.id.UserName);
        Address = (TextView) findViewById(R.id.Address);
        tvShopName = (TextView) findViewById(R.id.tvShopName);
        tvSubTotal = (TextView) findViewById(R.id.tvSubTotal);
        tvDeliveryFee = (TextView) findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = (TextView) findViewById(R.id.tvGrandTotal);
        rvCartItems = (RecyclerView) findViewById(R.id.rvCartItems);
        btnCheckOut = (Button) findViewById(R.id.btnCheckOut);

        rvCartItems.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CartActivity.this);

        ItemsModelClass itemsModelClass = new ItemsModelClass();
        String userName = itemsModelClass.getUserName();
        UserName.setText(userName);

        getCurrentLocation();

        Address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        showCartDialog();

    }

    public double allTotalPrice = 0.00;
    public TextView tvSubTotal, tvDeliveryFee, tvGrandTotal;

    private void showCartDialog() {

        tvShopName.setText(restaurant);

        EasyDB easyDB = EasyDB.init(CartActivity.this, "DB")
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
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String title = res.getString(3);
            String price = res.getString(4);
            String items_count = res.getString(5);
            String final_price = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(final_price);

            cartItemsModelClass = new CartItemsModelClass(
                    ""+id,
                    ""+pId,
                    ""+title,
                    ""+final_price,
                    ""+price,
                    ""+items_count
            );

            cartItemsList.add(cartItemsModelClass);
        }

        cartItemsAdapter = new CartItemsAdapter(CartActivity.this, cartItemsList);

        rvCartItems.setAdapter(cartItemsAdapter);

        tvDeliveryFee.setText(delivery);
        tvSubTotal.setText("PKR" + String.format("%.2f", allTotalPrice));
        tvGrandTotal.setText("PKR" + (allTotalPrice + Double.parseDouble(delivery.replace("PKR", ""))));

        if (res.getCount() == 0){
            Snackbar.make(findViewById(android.R.id.content), "No Item Found", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
        else {

            btnCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String total = tvGrandTotal.getText().toString().trim().replace("PKR", "");

                    if (cartItemsList.size() == 0){
                        Snackbar.make(findViewById(android.R.id.content), "No Item Found", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    }
                    else {
                        ProgressDialog progressDialog = new ProgressDialog(CartActivity.this);
                        progressDialog.setTitle("Please Wait");
                        progressDialog.setMessage("Order is placing...");
                        progressDialog.show();

                        HashMap<String, Object> order1 = new HashMap<>();
                        order1.put("restaurant name", restaurant);
                        order1.put("total", total);
                        order1.put("Time", getDateTime());
                        order1.put("status", "In progress");

                        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                                .collection("Cart").document(restaurant+" "+getDateTime())
                                .set(order1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                for (int i = 0; i<cartItemsList.size(); i++) {
                                    HashMap<String, Object> order2 = new HashMap<>();
                                    order2.put("id", cartItemsList.get(i).getId());
                                    order2.put("pId", cartItemsList.get(i).getpId());
                                    order2.put("title", cartItemsList.get(i).getItemName());
                                    order2.put("price", cartItemsList.get(i).getPrice());
                                    order2.put("items_count", cartItemsList.get(i).getItems_Count());
                                    order2.put("final_price", cartItemsList.get(i).getFinalPrice());
                                    order2.put("latlng", latLng);

                                    firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                                            .collection("Cart").document(restaurant+" "+getDateTime())
                                            .collection("Orders").document(cartItemsList.get(i).getId())
                                            .set(order2, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                                progressDialog.dismiss();
                                easyDB.deleteAllDataFromTable();
//                                updateCartCount();
                                allTotalPrice = 0.00;
                            }
                        });
                    }
                    Snackbar.make(findViewById(android.R.id.content), "Order Placed!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    Intent intent = new Intent(CartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();

        return date;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    latLng = new LatLng(latitude, longitude);

                    try {
                        add = showAddress(latLng);
                        Address.setText("" + add);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
//                    Toast.makeText(RestaurantItems.this, "Null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String showAddress(LatLng latLng) throws IOException {
        Geocoder geocoder;
        List<android.location.Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0);
//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
        return  address;

    }

}
