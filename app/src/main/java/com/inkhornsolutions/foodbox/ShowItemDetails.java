package com.inkhornsolutions.foodbox;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShowItemDetails extends AppCompatActivity {

    private ElegantNumberButton enbNumofOrders;
    private FloatingActionButton backArrow;
    private ImageView civItemImage;
    private TextView tvItem, tvPrice, tvDescription, tvQuantity;
    private Button btnAddtoCart;
    private String resName, itemName, itemImage, itemPrice;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private double price = 0;
    private double finalPrice = 0;
    private int itemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item_details);

        enbNumofOrders = (ElegantNumberButton) findViewById(R.id.enbNumofOrders);
        backArrow = (FloatingActionButton) findViewById(R.id.backArrow);
        civItemImage = (ImageView) findViewById(R.id.civItemImage);
        tvItem = (TextView) findViewById(R.id.tvItem);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvQuantity = (TextView) findViewById(R.id.tvQuantity);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        btnAddtoCart = (Button) findViewById(R.id.btnAddtoCart);
        enbNumofOrders = (ElegantNumberButton) findViewById(R.id.enbNumofOrders);

        firebaseFirestore = FirebaseFirestore.getInstance();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        enbNumofOrders.bringToFront();

        resName = getIntent().getStringExtra("resName");
        itemName = getIntent().getStringExtra("itemName");

        backArrow.bringToFront();
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        documentReference = firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName);

        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String itemName = documentSnapshot.getId();
                            itemImage = documentSnapshot.getString("imageUri");
                            itemPrice = documentSnapshot.getString("price");
                            String quantity = documentSnapshot.getString("quantity");
                            String itemDescription = documentSnapshot.getString("description");

                            tvItem.setText(itemName);
                            Glide.with(ShowItemDetails.this).load(itemImage).placeholder(R.drawable.food_placeholder).into(civItemImage);
                            price = Double.parseDouble(itemPrice.replace("PKR",""));
                            tvPrice.setText("PKR" + price);
                            tvDescription.setText(itemDescription);
                            tvQuantity.setText(quantity);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });

        itemCount = Integer.parseInt(enbNumofOrders.getNumber());
        finalPrice = Double.parseDouble(tvPrice.getText().toString().replace("PKR", ""));

        enbNumofOrders.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Log.d("TAG", String.format("oldValue: %d   newValue: %d", oldValue, newValue));

                itemCount = newValue;

                int itemsPrice = Integer.parseInt(itemPrice);

                finalPrice = itemsPrice * newValue;

                tvPrice.setText("PKR" + finalPrice);
            }
        });

//        enbNumofOrders.setOnClickListener(new ElegantNumberButton.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                itemCount = Integer.parseInt(enbNumofOrders.getNumber());
//
//                int itemsPrice = Integer.parseInt(itemPrice);
//
//                finalPrice = itemsPrice * itemCount;
//                tvPrice.setText("PKR" + finalPrice);
//            }
//        });

        btnAddtoCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = tvItem.getText().toString().trim();
                String Price = String.valueOf(price);
                finalPrice = Double.parseDouble(tvPrice.getText().toString().replace("PKR", ""));

                addToCart(getDateTime(), title, itemImage, Price, String.valueOf(finalPrice), String.valueOf(itemCount));

                Log.d("btnAddtoCart2", title + Price + finalPrice + itemCount);
            }
        });
    }

    private int itemId = 0;
    private void addToCart(String productId, String title, String imageUri, String price, String finalPrice, String itemCount) {
        itemId++;

        EasyDB easyDB = EasyDB.init(this, "ItemsDatabase")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                .doneTableColumn();

        Log.d("btnAddtoCart3", productId +" " +imageUri+" " +title+" " + price+" " + finalPrice+" " + itemCount);

        boolean b = easyDB
                .addData("Item_Id", itemId)
                .addData("pId", productId)
                .addData("Title", title)
                .addData("Price", price)
                .addData("Items_Count", itemCount)
                .addData("Final_Price", finalPrice)
//                .addData("Description", description)
                .addData("Item_Image_Uri", imageUri)
                .doneDataAdding();

        if (b){
            Snackbar.make(findViewById(android.R.id.content), "Added to Cart!", Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
        }
        else {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }

        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            boolean update = easyDB.updateData(1,id).rowID(Integer.valueOf(id));
        }

        RestaurantItems.getInstance().updateCartCount();
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();

        return date;
    }

}