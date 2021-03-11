package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class TrackOrders extends AppCompatActivity {

    private Toolbar toolbarTrack;
    private RecyclerView rvTrack;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    HistoryModelClass historyModelClass;

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
                .collection("Cart")
                .whereIn("status", Arrays.asList("Pending","In progress","Rejected"))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                trackOrders.clear();

                for (QueryDocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()){

                        String resId = documentSnapshot.getId();
                        String time = documentSnapshot.getString("Time");
                        String resName = documentSnapshot.getString("restaurant name");
                        String status = documentSnapshot.getString("status");
                        String total = documentSnapshot.getString("total");

                        historyModelClass = new HistoryModelClass();

                        historyModelClass.setResId(resId);
                        historyModelClass.setDate(time);
                        historyModelClass.setResName(resName);
                        historyModelClass.setStatus(status);
                        historyModelClass.setTotalPrice(total);

                        trackOrders.add(historyModelClass);

                    }
                    else {
                        Toast.makeText(TrackOrders.this, "Data not found!", Toast.LENGTH_SHORT).show();
                    }
                }
                rvTrack.setAdapter(new TrackOrdersAdapter(TrackOrders.this, trackOrders));
            }
        });
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
//                    if (documentSnapshot.exists()){
//
//                        String resId = documentSnapshot.getId();
//                        String time = documentSnapshot.getString("Time");
//                        String resName = documentSnapshot.getString("restaurant name");
//                        String status = documentSnapshot.getString("status");
//                        String total = documentSnapshot.getString("total");
//
//                        historyModelClass = new HistoryModelClass();
//
//                        historyModelClass.setResId(resId);
//                        historyModelClass.setDate(time);
//                        historyModelClass.setResName(resName);
//                        historyModelClass.setStatus(status);
//                        historyModelClass.setTotalPrice(total);
//
//                        trackOrders.add(historyModelClass);
//
//                        rvTrack.setAdapter(new TrackOrdersAdapter(TrackOrders.this, trackOrders));
//                    }
//                    else {
//                        Toast.makeText(TrackOrders.this, "Data not found!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });

    }
}