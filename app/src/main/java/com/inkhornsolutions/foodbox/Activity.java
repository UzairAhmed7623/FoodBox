package com.inkhornsolutions.foodbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

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

public class Activity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearch;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private TextView numberOfResults;
    private ArrayList<ItemsModelClass> filterdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        numberOfResults = (TextView) findViewById(R.id.numberOfResults);
        etSearch = (EditText) findViewById(R.id.etSearch);

        rvSearch = (RecyclerView) findViewById(R.id.rvSearch);
//        ivSearchBackground = (ImageView) findViewById(R.id.ivSearchBackground);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvSearch.setLayoutManager(staggeredGridLayoutManager);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        searchData();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable newText) {
                if (newText.toString().isEmpty()){
                    filterdList.clear();
                    numberOfResults.setVisibility(View.GONE);
                    numberOfResults.clearComposingText();
                }
                else {
                    numberOfResults.setVisibility(View.VISIBLE);

                    filter(newText.toString());
                }
            }
        });

    }

    private void filter(String text) {
        filterdList = new ArrayList<>();
        for (ItemsModelClass items : items){
            if (items.getItemName().toLowerCase().contains(text.toLowerCase())){
                filterdList.add(items);
            }
            else if (!items.getItemName().toLowerCase().equals(text.toLowerCase())){
                numberOfResults.clearComposingText();
            }
        }
        numberOfResults.setText("Found " + "(" + filterdList.size() + ")" + " results for " + "\n" + "(" + text + ")");

        rvSearch.setAdapter(new SearchAdapter(Activity.this, filterdList));
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