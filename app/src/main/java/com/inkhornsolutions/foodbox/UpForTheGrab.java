package com.inkhornsolutions.foodbox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.inkhornsolutions.foodbox.adapters.UpForTheGrabAdapter;
import com.inkhornsolutions.foodbox.models.RestaurantModelClass;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UpForTheGrab extends AppCompatActivity {

    private RecyclerView rvRestaurant;
    private final List<RestaurantModelClass> resDetails = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private UpForTheGrabAdapter upForTheGrabAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_for_the_grab);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLACK, Color.BLACK);

        rvRestaurant.setLayoutManager(new LinearLayoutManager(UpForTheGrab.this));
        upForTheGrabAdapter = new UpForTheGrabAdapter(UpForTheGrab.this, resDetails, UpForTheGrab.this);
        rvRestaurant.setAdapter(upForTheGrabAdapter);

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
                .whereEqualTo("UFG", "yes")
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
                            Toasty.error(UpForTheGrab.this, error.getMessage(), Toasty.LENGTH_LONG).show();
                        }
                    }
                });
    }

}