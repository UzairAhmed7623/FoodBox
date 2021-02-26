package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private String Name, restaurant;
    private String delivery = "45";
    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NotificationBadge notificationBadge;
    private ImageView cartIcon2;

    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsAdapter cartItemsAdapter;
    private CartItemsModelClass cartItemsModelClass;
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
        Name = getIntent().getStringExtra("name");

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RestaurantItems.this);
        getLocation();

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

                            ItemsModelClass modelClass = new ItemsModelClass();

                            modelClass.setUserName(Name);
                            modelClass.setItemName(name);
                            modelClass.setPrice(price);
                            modelClass.setImageUri(image);
                            modelClass.setId(getDateTime());

                            productList.add(modelClass);

                            rvItems.setAdapter(new RestaurentItemsAdapter(RestaurantItems.this, productList));
                        }
                    }
                }
            }
        });
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();

        return date;
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
                final boolean[] status = {false};
                firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                        .collection("Cart").whereEqualTo("status", "In progress")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isComplete()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                if (documentSnapshot.exists()){
                                    status[0] = true;
                                }
                            }
                        }
                    }
                });
                if (status[0]){
                    Snackbar.make(findViewById(android.R.id.content), "Your orders are already in progress.", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }

                else {
                    Dexter.withContext(RestaurantItems.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            showCartDialog();
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
                }
            }
        });
        return true;
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
                    Log.d("sasas",""+c);
                }
                else {
                    notificationBadge.setVisibility(View.VISIBLE);
                    notificationBadge.setText(String.valueOf(c));
                }
            }
        });
    }

    public double allTotalPrice = 0.00;
    public TextView tvSubTotal, tvDeliveryFee, tvGrandTotal;

    private void showCartDialog() {

        cartItemsList = new ArrayList<>();

        View view = LayoutInflater.from(RestaurantItems.this).inflate(R.layout.cart_dialog, null);
        TextView tvShopName;
        RecyclerView rvCartItems;
        Button btnCheckOut;

        tvShopName = (TextView) view.findViewById(R.id.tvShopName);
        tvSubTotal = (TextView) view.findViewById(R.id.tvSubTotal);
        tvDeliveryFee = (TextView) view.findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = (TextView) view.findViewById(R.id.tvGrandTotal);
        rvCartItems = (RecyclerView) view.findViewById(R.id.rvCartItems);
        btnCheckOut = (Button) view.findViewById(R.id.btnCheckOut);

        tvShopName.setText(restaurant);

        AlertDialog.Builder builder = new AlertDialog.Builder(RestaurantItems.this);
        builder.setView(view);

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

        cartItemsAdapter = new CartItemsAdapter(RestaurantItems.this, cartItemsList);

        rvCartItems.setAdapter(cartItemsAdapter);

        tvDeliveryFee.setText(delivery);
        tvSubTotal.setText("Pkr" + String.format("%.2f", allTotalPrice));
        tvGrandTotal.setText("Pkr" + (allTotalPrice + Double.parseDouble(delivery.replace("Pkr", ""))));

        if (res.getCount() == 0){
            Snackbar.make(findViewById(android.R.id.content), "You have not added any product till now!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }else {
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    allTotalPrice = 0.00;

                }
            });

            btnCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String total = tvGrandTotal.getText().toString().trim().replace("Pkr", "");

                    if (cartItemsList.size() == 0){
                        Toast.makeText(RestaurantItems.this,"no Item Found" , Toast.LENGTH_SHORT).show();
                    }
                    else {

                        ProgressDialog progressDialog = new ProgressDialog(RestaurantItems.this);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        for (int i=0; i<cartItemsList.size(); i++){

                            HashMap<String, Object> order1 = new HashMap<>();
                            order1.put("id", cartItemsList.get(i).getId());
                            order1.put("pId", cartItemsList.get(i).getpId());
                            order1.put("title", cartItemsList.get(i).getItemName());
                            order1.put("price", cartItemsList.get(i).getPrice());
                            order1.put("items_count", cartItemsList.get(i).getItems_Count());
                            order1.put("final_price", cartItemsList.get(i).getFinalPrice());
                            order1.put("status", "In progress");
                            order1.put("latlng", latLng);
//
//                        HashMap<String, Object> order3 = new HashMap<>();
//                        order3.put(cartItemsList.get(i).getId(), order1);

                            DocumentReference documentReference = firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").collection("Cart").document(cartItemsList.get(i).getId());

                            final int I = i;
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists()){
                                            documentReference.update(order1);

                                            progressDialog.dismiss();

                                            dialog.dismiss();
                                            easyDB.deleteAllDataFromTable();
                                            updateCartCount();
                                            allTotalPrice = 0.00;

                                        }
                                        else {
                                            firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").collection("Cart").document(cartItemsList.get(I).getId()).set(order1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog.dismiss();

                                                    dialog.dismiss();
                                                    easyDB.deleteAllDataFromTable();
                                                    updateCartCount();
                                                    allTotalPrice = 0.00;

                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                    Snackbar.make(findViewById(android.R.id.content), "Order Placed!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
            });
        }


    }

    private void getLocation() {
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