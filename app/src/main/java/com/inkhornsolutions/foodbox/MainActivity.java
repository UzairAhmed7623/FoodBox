package com.inkhornsolutions.foodbox;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.MalformedJsonException;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.infideap.drawerbehavior.AdvanceDrawerLayout;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.inkhornsolutions.foodbox.models.RestaurantModelClass;
import com.mlsdev.animatedrv.AnimatedRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView rvRestaurant;
    private final List<RestaurantModelClass> resDetails = new ArrayList<>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private AdvanceDrawerLayout drawerLayout;
    private TextView tvUserName;
    private CircleImageView ivProfileImage;
    private TextView tvAddress;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog progressDialog;
    private String imageUri;
    private TextView tvItemSearch;
    private LoginManager loginManager;
    private MainActivityAdapter mainActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loginManager = LoginManager.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (AdvanceDrawerLayout) findViewById(R.id.drawerLayout);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);
        tvItemSearch = (TextView) findViewById(R.id.tvItemSearch);
        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        setupWindowAnimations();

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLACK, Color.BLACK);

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Show status bar
        // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.black));

        drawerLayout.setViewScale(Gravity.START, 0.8f);
        drawerLayout.setRadius(Gravity.START, 0);
        drawerLayout.setViewElevation(Gravity.START, 100);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        ivProfileImage = (CircleImageView) header.findViewById(R.id.ivProfileImage);
        ImageView ivProfileSettings = (ImageView) header.findViewById(R.id.ivProfileSettings);
        tvUserName = (TextView) header.findViewById(R.id.tvUserName);

        headerTextView();

        headerImage();

        ivProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        loadData();

        rvRestaurant.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mainActivityAdapter = new MainActivityAdapter(MainActivity.this, resDetails, MainActivity.this);
        rvRestaurant.setAdapter(mainActivityAdapter);

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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, 1500);
            }
        });

        tvItemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                        findViewById(R.id.tvItemSearch),
                        "search");
                startActivity(intent, options.toBundle());

            }
        });

        deleteCartItems();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAllIds();
        loadData();
        checkForDiscount();
        firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String address = documentSnapshot.getString("address");

                            progressDialog.dismiss();
                            if (address != null) {

                                tvAddress.setText(address);

                                tvAddress.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, Profile.class);
                                        startActivity(intent);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                });
                            } else {
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

    private void checkForDiscount() {
        if (Common.discountAvailable.isEmpty()){

            FirebaseDatabase.getInstance().getReference("Admin").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String percentage = snapshot.child("percentage").getValue(String.class);
                        String available = snapshot.child("available").getValue(String.class);

                        Common.discountAvailable.put("percentage", percentage);
                        Common.discountAvailable.put("available", available);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toasty.error(MainActivity.this, error.getMessage(), Toasty.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadAllIds() {
        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        if (documentSnapshot.exists()) {
                            Common.id.add(documentSnapshot.getId());
                        }
                    }
                } else {
                    Toasty.error(getApplicationContext(), Objects.requireNonNull(error.getMessage()), Toasty.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadData() {

        firebaseFirestore.collection("Restaurants").orderBy("resName", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        resDetails.clear();

                        if (error == null) {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                RestaurantModelClass restaurantModelClass = documentSnapshot.toObject(RestaurantModelClass.class);

                                resDetails.add(restaurantModelClass);
                            }
                            progressDialog.dismiss();
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void headerTextView() {
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null) {
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name + " " + luser_Name);
                                SharedPreferences sharedPreferences = getSharedPreferences("userName", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("name", fuser_Name + " " + luser_Name);
                                editor.apply();
                            } else {
                                Log.d("TAG", "No data found!");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }
    }

    public void headerImage() {
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("UsersImageProfile") != null) {
                                imageUri = documentSnapshot.getString("UsersImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfileImage);
                            } else {
                                Log.d("TAG", "Not found!");
                            }
                        } else {
                            Log.d("TAG", "No data found!");
                        }
                    } else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    private void deleteCartItems() {
        EasyDB easyDB = EasyDB.init(MainActivity.this, "ordersDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_image, menu);

        MenuItem menuItem = menu.findItem(R.id.image);
        menuItem.setActionView(R.layout.toobar_profile_image);
        View view = menuItem.getActionView();
        CircleImageView toolbar_profile_Image = view.findViewById(R.id.toolbar_profile_Image);

        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("UsersImageProfile") != null) {
                                imageUri = documentSnapshot.getString("UsersImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.account_circle_black)).into(toolbar_profile_Image);
                            } else {
                                Log.d("TAG", "Not found!");
                            }
                        } else {
                            Log.d("TAG", "No data found!");
                        }
                    } else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }
        toolbar_profile_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

        if (item.getItemId() == R.id.trackOrder) {
            Intent intent = new Intent(MainActivity.this, TrackOrders.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (item.getItemId() == R.id.orderHistory) {
            Intent intent = new Intent(MainActivity.this, OrderHistory.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (item.getItemId() == R.id.logout) {
            if (firebaseAuth != null) {
                SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                SharedPreferences.Editor editor = signUpPreferences.edit();
                editor.putBoolean("registered", false);
                editor.apply();

                SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putBoolean("isFirstTime", true);
                editor1.apply();

                firebaseAuth.signOut();
            }
            if (loginManager != null) {
                SharedPreferences signUpPreferences = getSharedPreferences("signUp", MODE_PRIVATE);
                SharedPreferences.Editor editor = signUpPreferences.edit();
                editor.putBoolean("registered", false);
                editor.apply();

                SharedPreferences preferences = getSharedPreferences("profile", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = preferences.edit();
                editor1.putBoolean("isFirstTime", true);
                editor1.apply();

                loginManager.logOut();
            }

            finish();
            startActivity(new Intent(this, SplashScreen.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}
