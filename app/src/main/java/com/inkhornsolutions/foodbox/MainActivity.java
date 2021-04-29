package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.yalantis.pulltomakesoup.PullToRefreshView;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRestaurant;
    private final List<String> tvRestaurant = new ArrayList<>();
    private final List<String> ivRestaurant = new ArrayList<>();
    private final List<String> statusList = new ArrayList<>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String resName;
    private FlowingDrawer drawerLayout;
    private TextView tvUserName;
    private CircleImageView ivProfileImage;
    private ImageView ivProfileSettings;
    private RelativeLayout layout;
    private TextView orderHistory;
    private NiceSpinner spAddress;
    private TextView trackOrder;
    private PullToRefreshView mPullToRefreshView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        layout = (RelativeLayout) findViewById(R.id.layout);
        trackOrder = (TextView) findViewById(R.id.trackOrder);
        orderHistory = (TextView) findViewById(R.id.orderHistory);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfileImage = (CircleImageView) findViewById(R.id.ivProfileImage);
        ivProfileSettings = (ImageView) findViewById(R.id.ivProfileSettings);
        drawerLayout = (FlowingDrawer) findViewById(R.id.drawerLayout);
        spAddress = (NiceSpinner) findViewById(R.id.spAddress);
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_menu);

        headerTextView();

        headerImage();

        loadData();

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        rvRestaurant.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

//        GeofenceModel cavalry_Ground = new GeofenceModel.Builder("cavalry_Ground")
//                .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
//                .setLatitude(31.500668557055956)
//                .setLongitude(74.36623054805328)
//                .setRadius(2000)
//                .build();
//
//        GeofenceModel cuenca = new GeofenceModel.Builder("id_cuenca")
//                .setTransition(Geofence.GEOFENCE_TRANSITION_EXIT)
//                .setLatitude(40.0703925)
//                .setLongitude(-2.1374161999999615)
//                .setRadius(2000)
//                .build();
//
//        SmartLocation.with(this).geofencing()
//                .add(cavalry_Ground)
//                .remove("cavalry_Ground")
//                .start(new OnGeofencingTransitionListener() {
//                    @Override
//                    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {
//
//                        Toast.makeText(MainActivity.this, ""
//                                +transitionGeofence.getGeofenceModel().getTransition()
//                                +" "
//                                +transitionGeofence.getTransitionType(), Toast.LENGTH_SHORT).show();
//                    }
//                });

        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, 3000);
            }
        });

        deleteCartItems();

        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        drawerLayout.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    Log.i("MainActivity", "Drawer STATE_CLOSED");
                    layout.setForeground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));
                }
                else if (newState == ElasticDrawer.STATE_OPEN){
                    Log.i("MainActivity", "Drawer STATE_OPEN");


                    orderHistory.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, OrderHistory.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            drawerLayout.closeMenu();
                        }
                    });
                    trackOrder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, TrackOrders.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            drawerLayout.closeMenu();
                        }
                    });
                    ivProfileSettings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, Profile.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            drawerLayout.closeMenu();
                        }
                    });
                }
            }
            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
                if (openRatio > 0){
                    layout.setForeground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.white_greyish)));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
        firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String address = documentSnapshot.getString("address");
                            String address1 = documentSnapshot.getString("address");

                            progressDialog.dismiss();
                            if (address != null){

                                List<String> addressList = new ArrayList<>();
                                addressList.add(address);
                                addressList.add(address1);

                                spAddress.attachDataSource(addressList);

                                spAddress.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(NiceSpinner parent, View view, int position, long id) {

                                        Intent intent = new Intent(MainActivity.this, Profile.class);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                });
                            }
                            else {
                                Snackbar.make(findViewById(android.R.id.content), "Address not found!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                    }
                });
    }

    private void loadData(){
        firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                tvRestaurant.clear();
                ivRestaurant.clear();
                statusList.clear();

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        resName = documentSnapshot.getId();
                        String status = documentSnapshot.getString("status");
                        String imageUri = documentSnapshot.get("imageUri").toString();

                        if (resName != null && imageUri != null && status != null){
                            tvRestaurant.add(resName);
                            ivRestaurant.add(imageUri);
                            statusList.add(status);
                        }
                        else {
                            Snackbar.make(findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                        }
                    }
                    rvRestaurant.setAdapter(new MainActivityAdapter(getApplicationContext(), tvRestaurant, ivRestaurant, statusList));
                }
                progressDialog.dismiss();
                mPullToRefreshView.setRefreshing(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
            }
        });
    }

    public void headerTextView(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null){
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name +" "+luser_Name);
                            }
                            else {
                                Log.d("TAG", "No data found!");
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    public void headerImage(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("UsersImageProfile") != null){
                                String imageUri = documentSnapshot.getString("UsersImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfileImage);
                            }
                            else {
                                Log.d("TAG", "Not found!");
                            }
                        }
                        else {
                            Log.d("TAG", "No data found!");
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    private void deleteCartItems() {
        EasyDB easyDB = EasyDB.init(MainActivity.this, "DB")
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isMenuVisible()){
            drawerLayout.closeMenu();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search_icon){
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == android.R.id.home) {

            drawerLayout.toggleMenu();

        }
        return true;
    }

}
