package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.inkhornsolutions.foodbox.adapters.SearchAdapter;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearch;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private ImageView ivSearchBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        etSearch = (EditText) findViewById(R.id.etSearch);
        rvSearch = (RecyclerView) findViewById(R.id.rvSearch);
        ivSearchBackground = (ImageView) findViewById(R.id.ivSearchBackground);
        rvSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        searchData();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (etSearch.getText().toString().equals("")){
                    ivSearchBackground.setVisibility(View.VISIBLE);
                    rvSearch.setVisibility(View.GONE);
                }
                else {
                    ivSearchBackground.setVisibility(View.GONE);
                    rvSearch.setVisibility(View.VISIBLE);
                    filter(s.toString());
                }
            }
        });
    }

    private void filter(String text) {
        ArrayList<ItemsModelClass> filterdList = new ArrayList<>();
        for (ItemsModelClass items : items){
            if (items.getItemName().toLowerCase().contains(text.toLowerCase())){
                filterdList.add(items);
            }
        }

        rvSearch.setAdapter(new SearchAdapter(SearchActivity.this, filterdList));

    }

    private void searchData() {
        firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String resName = documentSnapshot.getId();
                        firebaseFirestore.collection("Restaurants").document(resName)
                                .collection("Items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()){
                                        String itemName = documentSnapshot1.getId();
                                        String price = documentSnapshot1.getString("price");
                                        String imageUri = documentSnapshot1.getString("imageUri");

                                        ItemsModelClass itemsModelClass = new ItemsModelClass();
                                        itemsModelClass.setItemName(itemName);
                                        itemsModelClass.setPrice(price);
                                        itemsModelClass.setImageUri(imageUri);

                                        items.add(itemsModelClass);
                                    }
                                }
                            }
                        });
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
            }
        });
    }
}