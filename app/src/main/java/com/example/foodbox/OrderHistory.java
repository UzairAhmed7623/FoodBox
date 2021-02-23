package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.foodbox.adapters.HistoryAdapter;
import com.example.foodbox.adapters.RestaurentItemsAdapter;
import com.example.foodbox.models.CartItemsModelClass;
import com.example.foodbox.models.HistoryModelClass;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Ordering;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.StructuredQuery;

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

        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").collection("Cart")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if (documentSnapshot.exists()){

                        String id = documentSnapshot.getId();
                        String itemName = documentSnapshot.getString("title");
                        String price = documentSnapshot.getString("price");
                        String itemCount = documentSnapshot.getString("items_count");
                        String finalPrice = documentSnapshot.getString("final_price");
                        String pId = documentSnapshot.getString("pId");

                        Log.d("asdfgh", ""+id+itemName);

                        HistoryModelClass historyModelClass = new HistoryModelClass();
                        historyModelClass.setId(id);
                        historyModelClass.setItemName(itemName);
                        historyModelClass.setPrice(price);
                        historyModelClass.setItems_Count(itemCount);
                        historyModelClass.setFinalPrice(finalPrice);
                        historyModelClass.setpId(pId);



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