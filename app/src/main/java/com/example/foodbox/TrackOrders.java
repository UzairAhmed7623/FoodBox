package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.foodbox.adapters.HistoryAdapter;
import com.example.foodbox.adapters.TrackOrdersAdapter;
import com.example.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrackOrders extends AppCompatActivity {

    private Toolbar toolbarTrack;
    private RecyclerView rvTrack;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarTrack = (Toolbar) findViewById(R.id.toolbarTrack);

        setSupportActionBar(toolbarTrack);

        rvTrack = (RecyclerView) findViewById(R.id.rvTrack);
        rvTrack.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                .collection("Cart").whereEqualTo("status", "In progress")
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
                        String status = documentSnapshot.getString("status");

                        Log.d("asdfgh", ""+id+itemName);

                        HistoryModelClass historyModelClass = new HistoryModelClass();
                        historyModelClass.setId(id);
                        historyModelClass.setItemName(itemName);
                        historyModelClass.setPrice(price);
                        historyModelClass.setItems_Count(itemCount);
                        historyModelClass.setFinalPrice(finalPrice);
                        historyModelClass.setpId(pId);
                        historyModelClass.setStatus(status);



                        trackOrders.add(historyModelClass);

                        rvTrack.setAdapter(new TrackOrdersAdapter(TrackOrders.this, trackOrders));

                    }
                    else {
                        Toast.makeText(TrackOrders.this, "Data not found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}