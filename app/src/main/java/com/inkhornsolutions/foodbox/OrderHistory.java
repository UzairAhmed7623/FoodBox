package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrderHistory extends AppCompatActivity {

    private Toolbar toolbarHistory;
    private RecyclerView rvHistory;
    private List<HistoryModelClass> OrderHistory = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private TextView txt1, txt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarHistory = (Toolbar) findViewById(R.id.toolbarHistory);
        txt1 = (TextView) findViewById(R.id.txt1);
        txt2 = (TextView) findViewById(R.id.txt2);

        setSupportActionBar(toolbarHistory);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(OrderHistory.this, R.color.myColor));
        swipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);

        rvHistory = (RecyclerView) findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              loadData();
            }
        });
    }

    private void loadData(){
        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .collection("Cart")
                .whereIn("status", Arrays.asList("Dispatched", "Completed"))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                OrderHistory.clear();
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if (documentSnapshot.exists()){
                        HistoryModelClass historyModelClass = new HistoryModelClass();

                        String resId = documentSnapshot.getId();
                        String time = documentSnapshot.getString("Time");
                        Timestamp timeStamp = documentSnapshot.getTimestamp("timeStamp");
                        String resName = documentSnapshot.getString("restaurantName");
                        String status = documentSnapshot.getString("status");
                        String total = documentSnapshot.getString("total");
                        if (documentSnapshot.getString("userRating") != null){
                            String userRating = documentSnapshot.getString("userRating");
                            historyModelClass.setUserRating(userRating);
                        }
                        Log.d("asdfgh", ""+resId+time);

                        historyModelClass.setResId(resId);
                        historyModelClass.setDate(time);
                        historyModelClass.setTimeStamp(timeStamp);
                        historyModelClass.setResName(resName);
                        historyModelClass.setStatus(status);
                        historyModelClass.setTotalPrice(total);

                        OrderHistory.add(historyModelClass);
                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                    }
                }
                Collections.sort(OrderHistory, new Comparator<HistoryModelClass>() {
                    @Override
                    public int compare(HistoryModelClass o1, HistoryModelClass o2) {
                        return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                    }
                });
                if (OrderHistory.size() == 0) {
                    txt1.setVisibility(View.VISIBLE);
                    txt2.setVisibility(View.VISIBLE);
                } else {
                    txt1.setVisibility(View.GONE);
                    txt2.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                rvHistory.setAdapter(new HistoryAdapter(OrderHistory.this, OrderHistory));
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