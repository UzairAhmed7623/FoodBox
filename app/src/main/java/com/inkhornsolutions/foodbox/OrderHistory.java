package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.inkhornsolutions.foodbox.adapters.HistoryAdapter;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistory extends AppCompatActivity {

    private Toolbar toolbarHistory;
    private RecyclerView rvHistory;
    private List<HistoryModelClass> OrderHistory = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarHistory = (Toolbar) findViewById(R.id.toolbarHistory);

        setSupportActionBar(toolbarHistory);

        rvHistory = (RecyclerView) findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                .collection("Cart").whereEqualTo("status", "completed")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if (documentSnapshot.exists()){

                        String resId = documentSnapshot.getId();
                        String time = documentSnapshot.getString("Time");
                        String resName = documentSnapshot.getString("restaurant name");
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
                        Toast.makeText(OrderHistory.this, "Data not found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}