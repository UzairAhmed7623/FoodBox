package com.inkhornsolutions.foodbox;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Checkout extends AppCompatActivity {

    private static final String GOOGLE_API_KEY = "AIzaSyBa4XZ09JsXD8KYZr5wdle--0TQFpfyGew";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton backArrow;
    private TextView tvChangeAddress, tvUserName, tvAddress, tvPhone, tvTotalPrice;
    private RadioButton rbDoorDelivery;
    private Button btnCheckOut;
    private String phone, userName, total, add;
    private LatLng latLng;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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

        tvUserName.setText(userName);
        tvPhone.setText(phone);
        tvTotalPrice.setText(total);

        getCurrentLocation();

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

}