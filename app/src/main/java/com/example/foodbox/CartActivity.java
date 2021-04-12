package com.example.foodbox;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbox.adapters.CartItemsAdapter;
import com.example.foodbox.models.CartItemsModelClass;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class CartActivity extends AppCompatActivity {

    private static final String GOOGLE_API_KEY = "AIzaSyBa4XZ09JsXD8KYZr5wdle--0TQFpfyGew";
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;

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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_API_KEY, Locale.getDefault()   );
        }

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

        String first_name = getIntent().getStringExtra("first_name");
        String last_name = getIntent().getStringExtra("last_name");

        UserName.setText("Name: " + first_name +" "+ last_name);

        getCurrentLocation();

        Address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new PlacePicker.IntentBuilder()
                        .setLatLong(latLng.latitude, latLng.longitude)  // Initial Latitude and Longitude the Map will load into
                        .setMapZoom(14.0f)  // Map Zoom Level. Default: 14.0
                        .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                        .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                        .setMarkerDrawable(R.drawable.marker) // Change the default Marker Image
                        .setMarkerImageImageColor(R.color.myColor)
                        .setFabColor(R.color.myColor)
                        .setPrimaryTextColor(R.color.black) // Change text color of Shortened Address
                        .setSecondaryTextColor(R.color.black) // Change text color of full Address
                        .setBottomViewColor(R.color.white) // Change Address View Background Color (Default: White)
                        .setMapRawResourceStyle(R.raw.map_style)  //Set Map Style (https://mapstyle.withgoogle.com/)
                        .setMapType(MapType.NORMAL)
                        .setPlaceSearchBar(true, GOOGLE_API_KEY) //Activate GooglePlace Search Bar. Default is false/not activated. SearchBar is a chargeable feature by Google
                        .onlyCoordinates(true)  //Get only Coordinates from Place Picker
                        .hideLocationButton(false)   //Hide Location Button (Default: false)
                        .disableMarkerAnimation(false)   //Disable Marker Animation (Default: false)
                        .build(CartActivity.this);

                startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST);

            }
        });

        showCart();

    }

    public double allTotalPrice = 0.00;
    public TextView tvSubTotal, tvDeliveryFee, tvGrandTotal;

    private void showCart() {

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

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String total = tvGrandTotal.getText().toString().trim().replace("PKR", "");

                if (cartItemsList.size() == 0){
                    Snackbar.make(findViewById(android.R.id.content), "No Item Found", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                }
                else {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
                    alertDialog.setTitle("Confirm Address").setMessage(add)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ProgressDialog progressDialog = new ProgressDialog(CartActivity.this);
                                    progressDialog.setTitle("Please Wait");
                                    progressDialog.setMessage("Order is placing...");
                                    progressDialog.show();

                                    String address = Address.getText().toString();

                                    HashMap<String, Object> order1 = new HashMap<>();
                                    order1.put("restaurant name", restaurant);
                                    order1.put("total", total);
                                    order1.put("Time", getDateTime());
                                    order1.put("status", "Pending");
                                    order1.put("ID", shortUUID());
                                    order1.put("address", address);
                                    order1.put("latlng", latLng);

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
//                                          updateCartCount();
                                            allTotalPrice = 0.00;

                                            Snackbar.make(findViewById(android.R.id.content), "Order Placed!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                            Handler handler = new Handler(Looper.myLooper());
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(CartActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }, 1000);
                                        }
                                    });
                                }
                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                  dialog.dismiss();
                                }
                            }).show();
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

        return  address;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                try {
                    latLng = new LatLng(addressData.getLatitude(), addressData.getLongitude() );
                    add = showAddress(latLng);

                    Address.setText(add);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
}
