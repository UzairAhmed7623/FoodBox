package com.example.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SettingsSlicesContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodbox.adapters.SearchAdapter;
import com.example.foodbox.models.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView rvSearch;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        etSearch = (EditText) findViewById(R.id.etSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        rvSearch = (RecyclerView) findViewById(R.id.rvSearch);
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
                    rvSearch.setVisibility(View.GONE);
                }
                else {
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
        });
    }
}