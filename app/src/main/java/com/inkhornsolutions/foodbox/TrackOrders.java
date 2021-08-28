package com.inkhornsolutions.foodbox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.EventBus.InProgress;
import com.inkhornsolutions.foodbox.adapters.TrackOrdersAdapter;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yalantis.pulltomakesoup.PullToRefreshView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TrackOrders extends AppCompatActivity {

    private Toolbar toolbarTrack;
    private RecyclerView rvTrack;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private HistoryModelClass historyModelClass;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout refresh;
    private TextView txt1, txt2;

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTimeUp(InProgress event){

        Common.showNotification(this, new Random().nextInt(),
                "Alert",
                "Dear customer, time is over now. Please hurryUp",
                getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarTrack = (Toolbar) findViewById(R.id.toolbarTrack);
        txt1 = (TextView) findViewById(R.id.txt1);
        txt2 = (TextView) findViewById(R.id.txt2);

        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(TrackOrders.this, R.color.myColor));
        refresh.setColorSchemeColors(Color.WHITE, Color.WHITE);

        setSupportActionBar(toolbarTrack);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rvTrack = (RecyclerView) findViewById(R.id.rvTrack);
        rvTrack.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadData();

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        loadData();
                    }
                }, 1000);
            }
        });
    }

    private void loadData(){
        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .collection("Cart")
                .whereIn("status", Arrays.asList("Pending","In progress","Rejected"))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        trackOrders.clear();

                        for (DocumentSnapshot documentSnapshot : value.getDocuments()){
                            if (documentSnapshot.exists()){

                                String resId = documentSnapshot.getId();
                                String time = documentSnapshot.getString("Time");
                                String resName = documentSnapshot.getString("restaurantName");
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
                                Snackbar.make(findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                        }
                        Collections.sort(trackOrders, new Comparator<HistoryModelClass>() {
                            @Override
                            public int compare(HistoryModelClass o1, HistoryModelClass o2) {
                                return o2.getDate().compareTo(o1.getDate());
                            }
                        });
                        if (trackOrders.size() == 0){
                            txt1.setVisibility(View.VISIBLE);
                            txt2.setVisibility(View.VISIBLE);
                        }
                        else {
                            txt1.setVisibility(View.GONE);
                            txt2.setVisibility(View.GONE);
                        }

                        progressDialog.dismiss();
                        refresh.setRefreshing(false);
                        rvTrack.setAdapter(new TrackOrdersAdapter(TrackOrders.this, trackOrders));
                    }
                });
    }
}