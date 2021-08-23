package com.inkhornsolutions.foodbox;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inkhornsolutions.foodbox.UserUtils.UserUtils;
import com.inkhornsolutions.foodbox.models.CartItemsModelClass;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class Checkout extends AppCompatActivity {

    private static final String GOOGLE_API_KEY = "AIzaSyBa4XZ09JsXD8KYZr5wdle--0TQFpfyGew";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton backArrow;
    private TextView tvChangeAddress, tvUserName, tvAddress, tvPhone, tvTotalPrice;
    private RadioButton rbDoorDelivery;
    private Button btnCheckOut;
    private String phone, userName, total, add, restaurant, deliveryFee, subTotal, available;
    private LatLng latLng;
    private ArrayList<CartItemsModelClass> cartItemsList;
    private CartItemsModelClass cartItemsModelClass;
    public double allTotalPrice = 0.00;
    private ConstraintLayout rootLayout;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Search.RESULT_OK && result.getData() != null) {
                AddressData addressData = result.getData().getParcelableExtra(Constants.ADDRESS_INTENT);
                try {
                    latLng = new LatLng(addressData.getLatitude(), addressData.getLongitude());
                    add = showAddress(latLng);

                    tvAddress.setText(add);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        backArrow = (FloatingActionButton) findViewById(R.id.backArrow);
        tvChangeAddress = (TextView) findViewById(R.id.tvChangeAddress);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
        rbDoorDelivery = (RadioButton) findViewById(R.id.rbDoorDelivery);
        btnCheckOut = (Button) findViewById(R.id.btnCheckOut);
        rootLayout = (ConstraintLayout) findViewById(R.id.rootLayout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_API_KEY, Locale.getDefault());
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Checkout.this);

        phone = firebaseAuth.getCurrentUser().getPhoneNumber();
        userName = getIntent().getStringExtra("first_name") + " " + getIntent().getStringExtra("last_name");;
        total = getIntent().getStringExtra("total");
        restaurant = getIntent().getStringExtra("restaurant");
        deliveryFee = getIntent().getStringExtra("deliveryFee");
        subTotal = getIntent().getStringExtra("subTotal");
        available = getIntent().getStringExtra("available");

        cartItemsList = new ArrayList<>();

        tvUserName.setText(userName);
        tvPhone.setText(phone);
        tvTotalPrice.setText(total);

        getCurrentLocation();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(Checkout.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();

                UserUtils.updateToken(Checkout.this, token);
            }
        });

        tvChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new PlacePicker.IntentBuilder()
                        .setLatLong(latLng.latitude, latLng.longitude)  // Initial Latitude and Longitude the Map will load into
                        .setMapZoom(17.0f)  // Map Zoom Level. Default: 14.0
                        .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                        .hideMarkerShadow(false) // Hides the shadow under the map marker. Default: False
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
                        .disableMarkerAnimation(true)   //Disable Marker Animation (Default: false)
                        .build(Checkout.this);

                launcher.launch(intent);
            }
        });


        EasyDB easyDB = EasyDB.init(Checkout.this, "ItemsDatabase")
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
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String title = res.getString(3);
            String price = res.getString(4);
            String items_count = res.getString(5);
            String final_price = res.getString(6);
            String imageUri = res.getString(7);

            allTotalPrice = allTotalPrice + Double.parseDouble(final_price);

            cartItemsModelClass = new CartItemsModelClass(
                    ""+id,
                    ""+pId,
                    ""+title,
                    ""+final_price,
                    ""+price,
                    ""+items_count,
                    ""+imageUri
            );

            cartItemsList.add(cartItemsModelClass);
        }

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cartItemsList.size() == 0){
                    Snackbar.make(findViewById(android.R.id.content), "No Item Found", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                }
                else {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Checkout.this);
                    alertDialog.setTitle("Confirm Address").setMessage(add)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ProgressDialog progressDialog = new ProgressDialog(Checkout.this);
                                    progressDialog.setTitle("Please Wait");
                                    progressDialog.setMessage("Order is placing...");
                                    progressDialog.show();

                                    String address = tvAddress.getText().toString();

                                    HashMap<String, Object> order1 = new HashMap<>();
                                    order1.put("restaurantName", restaurant);
                                    order1.put("Time", getDateTime());
                                    order1.put("status", "Pending");
                                    order1.put("ID", shortUUID());
                                    order1.put("address", address);
                                    order1.put("latlng", latLng);
                                    order1.put("deliveryFee", deliveryFee);
                                    order1.put("subTotal", subTotal);
                                    order1.put("total", total);
                                    order1.put("promotedOrder", available);
                                    order1.put("timeStamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
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

                                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                                                        .collection("Cart").document(restaurant+" "+getDateTime())
                                                        .collection("Orders").document(cartItemsList.get(i).getId())
                                                        .set(order2, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                            easyDB.deleteAllDataFromTable();
//                                          updateCartCount();
//                                          allTotalPrice = 0.00;

                                            Snackbar.make(findViewById(android.R.id.content), "Order Placed!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                                            firebaseFirestore.collection("Restaurants").document(restaurant).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (documentSnapshot.exists()){
                                                            String Id = documentSnapshot.getString("id");
                                                            Log.d("message3", Id);
                                                            UserUtils.sendNewOrderNotificationToKitchen(rootLayout,Checkout.this, Id);
                                                        }
                                                    }
                                                }
                                            });

                                            progressDialog.dismiss();

                                            Handler handler = new Handler(Looper.myLooper());
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(Checkout.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }, 2000);
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

    private String showAddress(LatLng latLng) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0);

        return  address;
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
                        tvAddress.setText("" + add);
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

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy kk-mm",calendar).toString();

        return date;
    }

    public static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
}