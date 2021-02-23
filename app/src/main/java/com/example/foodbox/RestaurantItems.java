package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodbox.adapters.CartItemsAdapter;
import com.example.foodbox.adapters.RestaurentItemsAdapter;
import com.example.foodbox.models.CartItemsModelClass;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsAdapter cartItemsAdapter;
    private CartItemsModelClass cartItemsModelClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_items);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        restaurant = getIntent().getStringExtra("restaurant");
        Name = getIntent().getStringExtra("name");

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        Context context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RestaurantItems.this);
        getLocation();
        //Biryai = items krna ha firsore mn.

        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Biryai").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.cart) {
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
        return true;
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

                    for (int i=0; i<cartItemsList.size(); i++){

                        HashMap<String, Object> order1 = new HashMap<>();
                        order1.put("id", cartItemsList.get(i).getId());
                        order1.put("pId", cartItemsList.get(i).getpId());
                        order1.put("title", cartItemsList.get(i).getItemName());
                        order1.put("price", cartItemsList.get(i).getPrice());
                        order1.put("items_count", cartItemsList.get(i).getItems_Count());
                        order1.put("final_price", cartItemsList.get(i).getFinalPrice());
                        order1.put("latlng", latLng);
//
//                        HashMap<String, Object> order3 = new HashMap<>();
//                        order3.put(cartItemsList.get(i).getId(), order1);

                        DocumentReference documentReference = firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").collection("Cart").document(cartItemsList.get(i).getId());

                        int finalI = i;
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()){
                                        documentReference.update(order1);
                                        Toast.makeText(RestaurantItems.this, "Order Placed!", Toast.LENGTH_SHORT).show();

                                        dialog.dismiss();
                                        easyDB.deleteAllDataFromTable();
                                        allTotalPrice = 0.00;

                                    }
                                    else {
                                        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").collection("Cart").document(cartItemsList.get(finalI).getId()).set(order1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(RestaurantItems.this, "Order Placed!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                easyDB.deleteAllDataFromTable();
                                                allTotalPrice = 0.00;

                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }

                }
            }
        });
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

}