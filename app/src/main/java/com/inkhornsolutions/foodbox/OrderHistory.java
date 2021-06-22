package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.inkhornsolutions.foodbox.adapters.HistoryAdapter;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yalantis.pulltomakesoup.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderHistory extends AppCompatActivity {

    private Toolbar toolbarHistory;
    private RecyclerView rvHistory;
    private List<HistoryModelClass> OrderHistory = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private PullToRefreshView mPullToRefreshView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarHistory = (Toolbar) findViewById(R.id.toolbarHistory);

        setSupportActionBar(toolbarHistory);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);

        rvHistory = (RecyclerView) findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadData();

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
    }

    private void loadData(){
        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .collection("Cart").whereEqualTo("status", "Dispatched")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                OrderHistory.clear();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if (documentSnapshot.exists()){

                        String resId = documentSnapshot.getId();
                        String time = documentSnapshot.getString("Time");
                        String resName = documentSnapshot.getString("restaurantName");
                        String status = documentSnapshot.getString("status");
                        String total = documentSnapshot.getString("total");

                        Log.d("asdfgh", ""+resId+time);

                        HistoryModelClass historyModelClass = new HistoryModelClass();
                        historyModelClass.setResId(resId);
                        historyModelClass.setDate(time);
                        historyModelClass.setResName(resName);
                        historyModelClass.setStatus(status);
                        historyModelClass.setTotalPrice(total);



                        OrderHistory.add(historyModelClass);

                        rvHistory.setAdapter(new HistoryAdapter(OrderHistory.this, OrderHistory));

                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                    }
                }
                mPullToRefreshView.setRefreshing(false);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
            }
        });
    }
}