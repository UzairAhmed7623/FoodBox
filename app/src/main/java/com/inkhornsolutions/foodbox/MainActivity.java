package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MainActivity extends AppCompatActivity  {

    private RecyclerView rvRestaurant;
    private List<String> tvRestaurant = new ArrayList<>();
    private List<String> ivRestaurant = new ArrayList<>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String resName, delivery = "45";
    private NavigationView navigationView;
    private FlowingDrawer drawerLayout;
    private TextView tvUserName;
    private CircleImageView ivProfileImage;
    private ImageView ivProfileSettings;
    private RelativeLayout layout;
    private TextView orderHistory;
    private TextView trackOrder;

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

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_menu);

        headerTextView();

        headerImage();

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        rvRestaurant.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        View view = LayoutInflater.from(this).inflate(R.layout.progress_bar, null);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_bar);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        resName = documentSnapshot.getId();
                        String imageUri = documentSnapshot.get("imageUri").toString();

                        tvRestaurant.add(resName);
                        ivRestaurant.add(imageUri);
                    }
                    rvRestaurant.setAdapter(new MainActivityAdapter(getApplicationContext(), tvRestaurant, ivRestaurant));
                }
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
