package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.foodbox.adapters.MainActivityAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView rvRestaurant;
    private List<String> tvRestaurant = new ArrayList<>();
    private List<String> ivRestaurant = new ArrayList<>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String resName, delivery = "45";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvUserName;
    private CircleImageView ivProfileImage;
    private ImageView ivProfileSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        View header_View = navigationView.getHeaderView(0);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        navigationView.setCheckedItem(R.id.navView);

        tvUserName = (TextView) header_View.findViewById(R.id.tvUserName);
        headerTextView();

        ivProfileImage = (CircleImageView) header_View.findViewById(R.id.ivProfileImage);
        headerImage();

        ivProfileSettings = (ImageView) header_View.findViewById(R.id.ivProfileSettings);
        ivProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        rvRestaurant.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        resName = documentSnapshot.getId();
                        String imageUri = documentSnapshot.get("imageUri").toString();

                        tvRestaurant.add(resName);
                        ivRestaurant.add(imageUri);
                    }
                    rvRestaurant.setAdapter(new MainActivityAdapter(getApplicationContext(), tvRestaurant, ivRestaurant));
                }
            }
        });

        deleteCartItems();
    }

    public void headerTextView(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null){
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name +" "+luser_Name);
                            }
                            else {
                                Log.d("TAG", "No data found!");
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    public void headerImage(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("UsersImageProfile") != null){
                                String imageUri = documentSnapshot.getString("UsersImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfileImage);
                            }
                            else {
                                Log.d("TAG", "Not found!");
                            }
                        }
                        else {
                            Log.d("TAG", "No data found!");
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    private void deleteCartItems() {
        EasyDB easyDB = EasyDB.init(MainActivity.this, "DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search_icon){
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.orderHistory){
            Intent intent = new Intent(MainActivity.this, OrderHistory.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == R.id.trackOrder) {
            Intent intent = new Intent(MainActivity.this, TrackOrders.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}
