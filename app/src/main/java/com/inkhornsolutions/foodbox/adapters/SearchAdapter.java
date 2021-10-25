package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public SearchAdapter(Context context, ArrayList<ItemsModelClass> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1){
            return 1;
        }
        else {
            return 0;
        }
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0){
            View view = inflater.inflate(R.layout.restaurant_items_adapter_left, parent, false);
            return new SearchAdapter.ViewHolder(view);
        }
        else {
            View view = inflater.inflate(R.layout.restaurant_items_adapter_right, parent, false);
            return new SearchAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModelClass item = items.get(position);

        String itemname = item.getItemName();
        String price = item.getPrice();
        String imageUri = item.getImageUri();

        holder.tvItem.setText(itemname);
        holder.tvItemPrice.setText("PKR"+price);
        Glide.with(context).load(imageUri).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Restaurants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                if (documentSnapshot.exists()){
                                    String resName = documentSnapshot.getId();
                                    firebaseFirestore.collection("Restaurants").document(resName)
                                            .collection("Items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()) {
                                                    if (documentSnapshot1.exists()) {
                                                        String itemName = documentSnapshot1.getId();

                                                        if (itemname.equals(itemName)){
                                                            String res = documentSnapshot.getId();
                                                            Log.d("Restaurant", res);

                                                            Intent intent = new Intent(context, RestaurantItems.class);
                                                            intent.putExtra("restaurant", res);
                                                            context.startActivity(intent);

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView ivItem;
        private TextView tvItem, tvItemPrice;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            ivItem = (CircleImageView) itemView.findViewById(R.id.ivItem);
            tvItem = (TextView) itemView.findViewById(R.id.tvItem);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
        }
    }
}
