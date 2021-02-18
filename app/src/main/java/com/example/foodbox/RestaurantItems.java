package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.foodbox.adapters.RestaurentItemsAdapter;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RestaurantItems extends AppCompatActivity {

    private RecyclerView rvItems;
    private List<ItemsModelClass> productList = new ArrayList<ItemsModelClass>();
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String Name, restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_items);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        restaurant = getIntent().getStringExtra("restaurant");
        Name = getIntent().getStringExtra("name");

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        //Biryai = items krna ha firsore mn.

        firebaseFirestore.collection("Restaurants").document(restaurant).collection("Biryai").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()){
                            String name = documentSnapshot.getId();
                            String price = documentSnapshot.get("price").toString();
                            String image = documentSnapshot.get("imageUri").toString();

                            ItemsModelClass modelClass = new ItemsModelClass();

                            modelClass.setUserName(Name);
                            modelClass.setItemName(name);
                            modelClass.setPrice(price);
                            modelClass.setImageUri(image);
                            modelClass.setId(getDateTime());

                            productList.add(modelClass);

                            rvItems.setAdapter(new RestaurentItemsAdapter(RestaurantItems.this, productList));
                        }
                    }
                }
            }
        });
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        Long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();
        return date;
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
                Toast.makeText(this, "item Clicked!", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}