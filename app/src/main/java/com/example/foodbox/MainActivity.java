package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.foodbox.adapters.CartItemsAdapter;
import com.example.foodbox.adapters.MainActivityAdapter;
import com.example.foodbox.models.CartItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRestaurant;
    private List<String> tvRestaurant = new ArrayList<>();
    private List<String> ivRestaurant = new ArrayList<>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String resName, delivery = "45";

    private ArrayList<CartItemsModelClass> cartItemsList;
    CartItemsAdapter cartItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        rvRestaurant = (RecyclerView) findViewById(R.id.rvRestaurantName);
        rvRestaurant.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        resName = documentSnapshot.getId();
                        String imageUri = documentSnapshot.get("imageUri").toString();

                        tvRestaurant.add(resName);
                        ivRestaurant.add(imageUri);

                        rvRestaurant.setAdapter(new MainActivityAdapter(getApplicationContext(), tvRestaurant, ivRestaurant));
                    }
                }
            }
        });

        deleteCartData();

    }

    private void deleteCartData() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart:

                showCartDialog();

                break;
        }
        return true;
    }

    public double allTotalPrice = 0.00;
    public TextView tvSubTotal, tvDeliveryFee, tvGrandTotal;

    private void showCartDialog() {

        cartItemsList = new ArrayList<>();

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.cart_dialog, null);
        TextView tvShopName;
        RecyclerView rvCartItems;
        Button btnCheckOut;

        tvShopName = (TextView) view.findViewById(R.id.tvShopName);
        tvSubTotal = (TextView) view.findViewById(R.id.tvSubTotal);
        tvDeliveryFee = (TextView) view.findViewById(R.id.tvDeliveryFee);
        tvGrandTotal = (TextView) view.findViewById(R.id.tvGrandTotal);
        rvCartItems = (RecyclerView) view.findViewById(R.id.rvCartItems);
        btnCheckOut = (Button) view.findViewById(R.id.btnCheckOut);

        tvShopName.setText(resName);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);

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

        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String title = res.getString(3);
            String price = res.getString(4);
            String items_count = res.getString(5);
            String final_price = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(final_price);

            CartItemsModelClass cartItemsModelClass = new CartItemsModelClass(
                    ""+id,
                    ""+pId,
                    ""+title,
                    ""+final_price,
                    ""+price,
                    ""+items_count
            );

            cartItemsList.add(cartItemsModelClass);
        }

        cartItemsAdapter = new CartItemsAdapter(MainActivity.this, cartItemsList);

        rvCartItems.setAdapter(cartItemsAdapter);

        tvDeliveryFee.setText(delivery);
        tvSubTotal.setText("Pkr" + String.format("%.2f", allTotalPrice));
        tvGrandTotal.setText("Pkr" + (allTotalPrice + Double.parseDouble(delivery.replace("Pkr", ""))));

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;

            }
        });

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String total = tvGrandTotal.getText().toString().trim().replace("Pkr", "");

                if (cartItemsList.size() == 0){
                    Toast.makeText(MainActivity.this,"no Item Found" , Toast.LENGTH_SHORT).show();
                }
                else {
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

                    Cursor res = easyDB.getAllData();
                    while (res.moveToNext()){
                        String id = res.getString(1);
                        String pId = res.getString(2);
                        String title = res.getString(3);
                        String price = res.getString(4);
                        String items_count = res.getString(5);
                        String final_price = res.getString(6);



                        Log.d("cartlist", id+" "+pId+" "+title+" "+price+" "+items_count+" "+final_price+" = "+total);
                    }


                }
            }
        });

    }
}
