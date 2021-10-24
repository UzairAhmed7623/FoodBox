package com.inkhornsolutions.foodbox;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.adapters.DODProductsAdapter;
import com.inkhornsolutions.foodbox.adapters.RestaurentItemsAdapter;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mlsdev.animatedrv.AnimatedRecyclerView;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class RestaurantItems extends AppCompatActivity implements RestaurentItemsAdapter.OnImageListener {

    private RecyclerView rvItems;
    private List<ItemsModelClass> productList = new ArrayList<ItemsModelClass>();
    private List<ItemsModelClass> DODProductList = new ArrayList<ItemsModelClass>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    public String name, last_name, restaurant, DOD="";
    private int badgeCount;
    private NotificationBadge notificationBadge;
    private boolean status = false;
    private ImageView cartIcon2;
    static RestaurantItems instance;
    private ProgressDialog progressDialog;
    private HorizontalScrollMenuView menuView;
    private RestaurentItemsAdapter adapter;
    private DODProductsAdapter dodProductsAdapter;

    private ItemsModelClass itemsModelClass;
    int size;
    SweetAlertDialog sweetAlertDialog;
    EventListener<DocumentSnapshot> eventListener;
    ListenerRegistration listenerRegistration;

    public static RestaurantItems getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_items);

        instance = this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        menuView = (HorizontalScrollMenuView) findViewById(R.id.rvTags);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        restaurant = getIntent().getStringExtra("restaurant");
        name = getIntent().getStringExtra("name");
        DOD = getIntent().getStringExtra("DOD");

        getSupportActionBar().setTitle(restaurant);

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvItems.setLayoutManager(staggeredGridLayoutManager);
        adapter = new RestaurentItemsAdapter(RestaurantItems.this, productList, this);

        if (getIntent().getStringExtra("DOD") != null) {
            dodProductsAdapter = new DODProductsAdapter(RestaurantItems.this, DODProductList);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_bar);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);



        if (getIntent().getStringExtra("DOD") == null){
            getData();
            checkRestaurantStatus(restaurant);
            menuView.setVisibility(View.VISIBLE);
        }
        else {
            getDODProducts();
            menuView.setVisibility(View.GONE);
            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
        }


        menuView.addItem("All", R.drawable.all);
        menuView.addItem("Main Course", R.drawable.main_course);
        menuView.addItem("Drinks", R.drawable.soft_drink);
        menuView.addItem("Frozen", R.drawable.frozen);
        menuView.addItem("Sides", R.drawable.sides);
        menuView.addItem("Mess", R.drawable.mess);
//        menuView.addItem("Desserts", R.drawable.desserts);

        menuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
            @Override
            public void onHSMClick(com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem menuItem, int position) {
                if (position == 0) {
                    menuView.editItem(position, "All", R.drawable.all_color, false, 0);
                    menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                    menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                    menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                    menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                    menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                } else if (position == 1) {
                    menuView.editItem(position, "Main Course", R.drawable.main_course_color, false, 0);
                    menuView.editItem(0, "All", R.drawable.all, false, 0);
                    menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                    menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                    menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                    menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                } else if (position == 2) {
                    menuView.editItem(position, "Drinks", R.drawable.soft_drink_color, false, 0);
                    menuView.editItem(0, "All", R.drawable.all, false, 0);
                    menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                    menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                    menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                    menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                } else if (position == 3) {
                    menuView.editItem(position, "Frozen", R.drawable.frozen_color, false, 0);
                    menuView.editItem(0, "All", R.drawable.all, false, 0);
                    menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                    menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                    menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                    menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                } else if (position == 4) {
                    menuView.editItem(position, "Sides", R.drawable.sides_color, false, 0);
                    menuView.editItem(0, "All", R.drawable.all, false, 0);
                    menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                    menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                    menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                    menuView.editItem(5, "Mess", R.drawable.mess, false, 0);
                }
                else if (position == 5) {
                    menuView.editItem(position, "Mess", R.drawable.mess_colored, false, 0);
                    menuView.editItem(0, "All", R.drawable.all, false, 0);
                    menuView.editItem(1, "Main Course", R.drawable.main_course, false, 0);
                    menuView.editItem(2, "Drinks", R.drawable.soft_drink, false, 0);
                    menuView.editItem(3, "Frozen", R.drawable.frozen, false, 0);
                    menuView.editItem(4, "Sides", R.drawable.sides, false, 0);
                }

                if (position != 0) {
                    firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                            .whereEqualTo("category", menuItem.getText())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    productList.clear();
                                    adapter.notifyDataSetChanged();

                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            String name = documentSnapshot.getId();

                                            ItemsModelClass itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                            itemsModelClass.setUserName(name);
                                            itemsModelClass.setItemName(name);
                                            itemsModelClass.setId(getDateTime());

                                            productList.add(itemsModelClass);

                                            rvItems.setAdapter(adapter);
                                        }
                                    }
                                    progressDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                }
                            });
                }
                else {
                    getData();
                }
            }
        });
    }

    private void getDODProducts() {
//        for (int i=0; i<Common.res.size(); i++){
            firebaseFirestore.collection("Restaurants").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                if (documentSnapshot.exists()) {
                                    String resName = documentSnapshot.getId();

                                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                                            .whereEqualTo("isDODAvailable", "yes")
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot) {
//                    DODProductList.clear();
//                        dodProductsAdapter.notifyDataSetChanged();

                                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    String name = documentSnapshot.getId();

                                                    itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                                    itemsModelClass.setUserName(name);
                                                    itemsModelClass.setItemName(name);
                                                    itemsModelClass.setId(getDateTime());
                                                    itemsModelClass.setResName(resName);

                                                    DODProductList.add(itemsModelClass);

                                                }
                                            }
                                            rvItems.setAdapter(dodProductsAdapter);
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

//        }
    }

    private void checkRestaurantStatus(String restaurant) {

        firebaseFirestore.collection("Restaurants").document(restaurant).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String status = documentSnapshot.getString("status");

                    if (status != null && status.equals("offline")) {

                        sweetAlertDialog = new SweetAlertDialog(RestaurantItems.this, SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setTitleText("Oops...");
                        sweetAlertDialog.setContentText("Restaurants was closed!");
                        sweetAlertDialog.setCancelable(false);
                        sweetAlertDialog.setConfirmButton("Ok!", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(RestaurantItems.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                        sweetAlertDialog.show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(RestaurantItems.this, e.getMessage(), Toasty.LENGTH_LONG).show();
            }
        });
    }

    private void getData() {
        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Items")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        productList.clear();
//                        adapter.notifyDataSetChanged();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getId();

                                itemsModelClass = documentSnapshot.toObject(ItemsModelClass.class);

                                itemsModelClass.setUserName(name);
                                itemsModelClass.setItemName(name);
                                itemsModelClass.setId(getDateTime());

                                productList.add(itemsModelClass);

                            }
                        }
                        rvItems.setAdapter(adapter);
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.cart_view, menu);

        MenuItem item1 = menu.findItem(R.id.cartIcon);
        item1.setActionView(R.layout.cart_notification_icon);
        View view = item1.getActionView();
        notificationBadge = (NotificationBadge) view.findViewById(R.id.notificationBadge);
        updateCartCount();
        cartIcon2 = (ImageView) view.findViewById(R.id.cartIcon2);
        cartIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(RestaurantItems.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (badgeCount == 0) {
                            Snackbar.make(findViewById(android.R.id.content), "You have not added any product till now!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.white))
                                    .setTextColor(ContextCompat.getColor(RestaurantItems.this, R.color.myColor)).show();
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
                            String restaurant = sharedPreferences.getString("restName","");

                            SharedPreferences sharedPreferencesName = getSharedPreferences("userName", MODE_PRIVATE);
                            String name = sharedPreferencesName.getString("name", "User Name");
                            Log.d("resName", restaurant);

                            Intent intent = new Intent(RestaurantItems.this, Cart.class);
                            intent.putExtra("restaurant", restaurant);
                            intent.putExtra("name", name);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(findViewById(android.R.id.content), "Please accept the permission!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
//                }
            }
        });
        return true;
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm", calendar).toString();

        return date;
    }

    public void updateCartCount() {
        if (notificationBadge == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EasyDB easyDB = EasyDB.init(RestaurantItems.this, "ordersDatabase")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("pId", new String[]{"text", "not null"}))
                        .addColumn(new Column("Title", new String[]{"text", "not null"}))
                        .addColumn(new Column("Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                        .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                        .doneTableColumn();

                Cursor res = easyDB.getAllData();
                int c = res.getCount();
                if (c == 0) {
                    notificationBadge.setText(String.valueOf(0));
                    notificationBadge.setVisibility(View.INVISIBLE);
                    badgeCount = c;
                } else {
                    badgeCount = c;
                    notificationBadge.setVisibility(View.VISIBLE);
                    notificationBadge.setText(String.valueOf(c));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "ordersDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor data = easyDB.getAllData();

        if (data.getCount() != 0) {
            AlertDialog.Builder alertDialog = new Builder(RestaurantItems.this);
            alertDialog.setTitle("Cart Alert")
                    .setMessage("If you go back your cart data will be deleted. Would you want to go back?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCartData();
                            SharedPreferences sharedPreferences = getSharedPreferences("resName", MODE_PRIVATE);
                            sharedPreferences.edit().clear().apply();

                            RestaurantItems.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(RestaurantItems.this, "ordersDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .doneTableColumn();

        easyDB.deleteAllDataFromTable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getStringExtra("DOD") == null) {
            checkRestaurantStatus(restaurant);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
            sweetAlertDialog = null;
        }
    }

    @Override
    public void onImageClick(int position) {

    }
}