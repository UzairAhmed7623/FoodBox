package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.inkhornsolutions.foodbox.models.RestaurantModelClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveKitchens extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvAddress;

    private RecyclerView rvRestaurant;
    private final List<RestaurantModelClass> resDetails = new ArrayList<>();
    private MainActivityAdapter mainActivityAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_kitchens);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLACK, Color.BLACK);

        rvRestaurant.setLayoutManager(new LinearLayoutManager(LiveKitchens.this));
        mainActivityAdapter = new MainActivityAdapter(LiveKitchens.this, resDetails, LiveKitchens.this);
        rvRestaurant.setAdapter(mainActivityAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadData();

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
    }

    private void loadData() {

        firebaseFirestore.collection("Restaurants")
                .whereEqualTo("approved", "yes")
                .whereNotEqualTo("resName", "Organic Shop")
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
                            Toast.makeText(LiveKitchens.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        if (documentSnapshot.exists()) {
                            String address = documentSnapshot.getString("address");

                            if (address != null) {

                                tvAddress.setText(address);

                                tvAddress.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(LiveKitchens.this, Profile.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_image, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
        }
        else if (item.getItemId() == R.id.image){
            Intent intent = new Intent(LiveKitchens.this, Search.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return true;
    }
}