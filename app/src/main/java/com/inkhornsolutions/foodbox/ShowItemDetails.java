package com.inkhornsolutions.foodbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShowItemDetails extends AppCompatActivity {

    private MaterialButton btnIncrement, btnDecrement;
    private int count = 1;
    private ImageButton backArrow;
    private KenBurnsView civItemImage;
    private TextView tvItem, tvPrice, tvDescription, tvQuantity, tvDisplay, tvFinalPrice, tvResName;
    private MaterialButton btnAddtoCart;
    private String resName, itemName, itemImage, itemPrice, userName, available, percentage, UFG, itemCookingTime;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private double price = 0;
    private double finalPrice = 0 , actualFinalPrice = 0;
    private int itemCount, discountedPrice;
    SweetAlertDialog sweetAlertDialog;
    EventListener<DocumentSnapshot> eventListener;
    ListenerRegistration listenerRegistration;
    private RelativeLayout layout1;
    SharedPreferences resNamePref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_details);

        backArrow = (ImageButton) findViewById(R.id.backArrow);
        civItemImage = (KenBurnsView) findViewById(R.id.civItemImage);
        tvItem = (TextView) findViewById(R.id.tvItem);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvQuantity = (TextView) findViewById(R.id.tvQuantity);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        btnAddtoCart = (MaterialButton) findViewById(R.id.btnAddtoCart);
        btnIncrement = (MaterialButton) findViewById(R.id.btnIncrement);
        btnDecrement = (MaterialButton) findViewById(R.id.btnDecrement);
        tvDisplay = (TextView) findViewById(R.id.tvDisplay);
        tvFinalPrice = (TextView) findViewById(R.id.tvFinalPrice);
        tvResName = (TextView) findViewById(R.id.tvResName);
        layout1 = (RelativeLayout) findViewById(R.id.layout1);
        layout1.bringToFront();

        firebaseFirestore = FirebaseFirestore.getInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        resName = getIntent().getStringExtra("resName");
        itemName = getIntent().getStringExtra("itemName");
        available = getIntent().getStringExtra("available");
        percentage = getIntent().getStringExtra("percentage");
        UFG = getIntent().getStringExtra("UFG");

        backArrow.bringToFront();
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        resNamePref = getSharedPreferences("resName", MODE_PRIVATE);
        editor = resNamePref.edit();
        editor.putString("restName",resName);

        tvResName.setText(resName);

        documentReference = firebaseFirestore.collection("Restaurants").document(resName)
                .collection("Items").document(itemName);

        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String itemName = documentSnapshot.getId();
                            itemImage = documentSnapshot.getString("imageUri");
                            itemPrice = documentSnapshot.getString("price");
                            String quantity = documentSnapshot.getString("quantity");
                            String itemDescription = documentSnapshot.getString("description");
                            if (documentSnapshot.getString("itemCookingTime") != null){
                                itemCookingTime = documentSnapshot.getString("itemCookingTime");
                            }
                            else {
                                itemCookingTime = "8";
                            }

                            tvItem.setText(itemName);

                            RequestOptions reqOpt = RequestOptions
                                    .fitCenterTransform()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(1024, 768)
                                    .priority(Priority.IMMEDIATE)
                                    .encodeFormat(Bitmap.CompressFormat.PNG)
                                    .format(DecodeFormat.DEFAULT);

                            Glide.with(ShowItemDetails.this)
                                    .load(itemImage)
                                    .placeholder(R.drawable.food_placeholder)
                                    .fitCenter()
                                    .apply(reqOpt)
                                    .into(civItemImage);

                            price = Double.parseDouble(itemPrice.replace("PKR", ""));
                            actualFinalPrice = price;

                            if (available.equals("yes")) {

                                discountedPrice = ((100 - Integer.parseInt(percentage)) * (int) price) / 100;

                                tvPrice.setText("PKR" + discountedPrice);
                                tvFinalPrice.setText(String.valueOf(discountedPrice));

                            } else {
                                tvPrice.setText("PKR" + price);
                                tvFinalPrice.setText(String.valueOf(price));

                            }
                            tvDescription.setText(itemDescription);
                            tvQuantity.setText("(" + quantity + ")");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });

        itemCount = Integer.parseInt(tvDisplay.getText().toString().trim());
        finalPrice = Double.parseDouble(tvPrice.getText().toString().replace("PKR", ""));

        btnIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                tvDisplay.setText(String.valueOf(count));
                itemCount = Integer.parseInt(tvDisplay.getText().toString().trim());

                if (available.equals("yes")) {
                    finalPrice = discountedPrice * itemCount;
                    actualFinalPrice = price * itemCount;
                }
                else {
                    finalPrice = Integer.parseInt(itemPrice) * itemCount;
                    actualFinalPrice = Integer.parseInt(itemPrice) * itemCount;
                }

                tvFinalPrice.setText("" + finalPrice);
            }
        });

        btnDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > 1) {
                    count--;
                    tvDisplay.setText(String.valueOf(count));
                    itemCount = Integer.parseInt(tvDisplay.getText().toString().trim());

                    if (available.equals("yes")) {
                        finalPrice = discountedPrice * itemCount;
                        actualFinalPrice = price * itemCount;

                    } else {
                        finalPrice = Integer.parseInt(itemPrice) * itemCount;
                        actualFinalPrice = Integer.parseInt(itemPrice) * itemCount;
                    }

                    tvFinalPrice.setText("" + finalPrice);
                }
            }
        });

        if (UFG != null && UFG.equals("yes")){
            btnAddtoCart.setText("Schedule");

            btnAddtoCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DateTime dateTime = DateTime.now().plusHours(Integer.parseInt(itemCookingTime));
                    Date date = dateTime.toDate();

                    new SingleDateAndTimePickerDialog.Builder(ShowItemDetails.this)
                            .bottomSheet()
                            .curved()
                            .bottomSheetHeight(600)
                            .defaultDate(date)
                            .minDateRange(date)
                            .mustBeOnFuture()
                            .backgroundColor(Color.WHITE)
                            .mainColor(ContextCompat.getColor(ShowItemDetails.this, R.color.myColor))
                            .titleTextColor(ContextCompat.getColor(ShowItemDetails.this, R.color.myColor))
                            .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                                @Override
                                public void onDisplayed(SingleDateAndTimePicker picker) {
//                                    Toast.makeText(ShowItemDetails.this, picker.getDate().toString(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .title("Choose your order time")
                            .listener(new SingleDateAndTimePickerDialog.Listener() {
                                @Override
                                public void onDateSelected(Date date) {
                                    String title = tvItem.getText().toString().trim();
                                    String dateAndTime = date.toString();
                                    String Price;

                                    if (available.equals("yes")) {
                                        Price = String.valueOf(discountedPrice);
                                    } else {
                                        Price = String.valueOf(price);
                                    }
                                    finalPrice = Double.parseDouble(tvFinalPrice.getText().toString().trim());

                                    addToCartScheduled(getDateTime(), title, itemImage, Price, String.valueOf(finalPrice), String.valueOf(itemCount), String.valueOf(actualFinalPrice), dateAndTime);

                                    Log.d("btnAddtoCart2", title + Price + finalPrice + itemCount + date.toString());
                                }
                            }).display();

                }
            });
        }
        else {
            btnAddtoCart.setText("Add to cart");
            btnAddtoCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = tvItem.getText().toString().trim();
                    String Price;
                    if (available.equals("yes")) {
                        Price = String.valueOf(discountedPrice);
                    } else {
                        Price = String.valueOf(price);
                    }
                    finalPrice = Double.parseDouble(tvFinalPrice.getText().toString().trim());

                    addToCartScheduled(getDateTime(), title, itemImage, Price, String.valueOf(finalPrice), String.valueOf(itemCount), String.valueOf(actualFinalPrice), "0");

                    Log.d("btnAddtoCart2", title + Price + finalPrice + itemCount);
                }
            });
        }


        checkRestaurantStatus(resName);
    }
    private int itemId = 0;

    private void addToCartScheduled(String dateTime, String title, String itemImage, String price, String finalPrice, String itemCount, String actualFinalPrice, String dateAndTime) {
        itemId++;

        EasyDB easyDB = EasyDB.init(this, "scheduledOrdersDatabase")
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
                .addColumn(new Column("orderTime", new String[]{"text", "not null"}))
                .doneTableColumn();

        Log.d("btnAddtoCart3", dateTime + " " + itemImage + " " + title + " " + price + " " + finalPrice + " " + itemCount);

        boolean b = easyDB
                .addData("Item_Id", itemId)
                .addData("pId", dateTime)
                .addData("Title", title)
                .addData("Price", price)
                .addData("Items_Count", itemCount)
                .addData("Final_Price", finalPrice)
                .addData("actualFinalPrice", actualFinalPrice)
//                .addData("Description", description)
                .addData("Item_Image_Uri", itemImage)
                .addData("orderTime", dateAndTime)
                .doneDataAdding();

        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            boolean update = easyDB.updateData(1, id).rowID(Integer.valueOf(id));
        }
        if (b) {
            Snackbar.make(findViewById(android.R.id.content), "Added to Cart!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();

            editor.putBoolean("added",true);
            editor.apply();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 650);
        } else {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }

        RestaurantItems.getInstance().updateCartCount();
    }


    private void checkRestaurantStatus(String resName) {

        firebaseFirestore.collection("Restaurants").document(resName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String status = documentSnapshot.getString("status");
                    if (status != null && status.equals("offline")) {

                        sweetAlertDialog = new SweetAlertDialog(ShowItemDetails.this, SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setTitleText("Oops...");
                        sweetAlertDialog.setContentText("Restaurants was closed!");
                        sweetAlertDialog.setCancelable(false);
                        sweetAlertDialog.setConfirmButton("Ok!", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(ShowItemDetails.this, MainActivity.class);
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
                Toasty.error(ShowItemDetails.this, e.getMessage(), Toasty.LENGTH_LONG).show();
            }
        });
    }

//    private void addToCart(String productId, String title, String imageUri, String price, String finalPrice, String itemCount, String actualFinalPrice) {
//        itemId++;
//
//        EasyDB easyDB = EasyDB.init(this, "ordersDatabase")
//                .setTableName("ITEMS_TABLE")
//                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
//                .addColumn(new Column("pId", new String[]{"text", "not null"}))
//                .addColumn(new Column("Title", new String[]{"text", "not null"}))
//                .addColumn(new Column("Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
//                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("actualFinalPrice", new String[]{"text", "not null"}))
////                .addColumn(new Column("Description", new String[]{"text", "not null"}))
//                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
//                .doneTableColumn();
//
//        Log.d("btnAddtoCart3", productId + " " + imageUri + " " + title + " " + price + " " + finalPrice + " " + itemCount);
//
//        boolean b = easyDB
//                .addData("Item_Id", itemId)
//                .addData("pId", productId)
//                .addData("Title", title)
//                .addData("Price", price)
//                .addData("Items_Count", itemCount)
//                .addData("Final_Price", finalPrice)
//                .addData("actualFinalPrice", actualFinalPrice)
////                .addData("Description", description)
//                .addData("Item_Image_Uri", imageUri)
//                .doneDataAdding();
//
//        Cursor cursor = easyDB.getAllData();
//        while (cursor.moveToNext()) {
//            String id = cursor.getString(0);
//            boolean update = easyDB.updateData(1, id).rowID(Integer.valueOf(id));
//        }
//        if (b) {
//            Snackbar.make(findViewById(android.R.id.content), "Added to Cart!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
//
//            editor.putBoolean("added",true);
//            editor.apply();
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    onBackPressed();
//                }
//            }, 650);
//        } else {
//            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
//        }
//
//        RestaurantItems.getInstance().updateCartCount();
//    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy kk-mm", calendar).toString();

        return date;
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
}