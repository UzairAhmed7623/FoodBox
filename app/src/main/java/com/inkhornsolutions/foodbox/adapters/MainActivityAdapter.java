package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.models.RatingClass;
import com.inkhornsolutions.foodbox.models.RatingsList;
import com.inkhornsolutions.foodbox.models.RestaurantModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import per.wsj.library.AndRatingBar;

//import per.wsj.library.AndRatingBar;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private List<RestaurantModelClass> resDetails = new ArrayList<>();
    private int checkedPosition = RecyclerView.NO_POSITION;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private List<RatingClass> ratingsList = new ArrayList<>();
    private List<String> resList = new ArrayList<>();
    View view;
    String first_name = "",last_name = "";

    int size;
    String rating;
    float finalRating = 0f;

    public MainActivityAdapter(Context context, List<RestaurantModelClass> resDetails) {
        this.context = context;
        this.resDetails = resDetails;
    }

    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(parent.getContext());
        view =inflater.inflate(R.layout.main_activity_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityAdapter.ViewHolder holder, int position) {
        RestaurantModelClass restaurantModelClass = resDetails.get(position);

        holder.tvRestaurant.setText(restaurantModelClass.getResName());

        Glide.with(context).load(restaurantModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivRestaurant);
//        Picasso.get().load(restaurantModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fit().centerCrop().into(holder.ivRestaurant);

        holder.itemView.setSelected(checkedPosition == position);

        String status = restaurantModelClass.getStatus();
        String approved = restaurantModelClass.getApproved();

        if (approved.equals("yes")){
            if (status.equals("online")){

                holder.layout.setEnabled(true);

                firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()){

                                        first_name = documentSnapshot.getString("firstName");
                                        last_name = documentSnapshot.getString("lastName");
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
            else {
                holder.layout.setEnabled(false);
                holder.layout.setClickable(false);
                holder.itemView.setEnabled(false);
                holder.itemView.setClickable(false);

                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                holder.ivRestaurant.setColorFilter(new ColorMatrixColorFilter(matrix));
//                Toast.makeText(context, "Restaurant is offline for now, Please try later.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Log.d("approval", "restaurant not approved yet");
            holder.layout.setEnabled(false);
            holder.layout.setClickable(false);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            holder.ivRestaurant.setColorFilter(new ColorMatrixColorFilter(matrix));
//            Toast.makeText(context, "Restaurant not approved yet", Toast.LENGTH_LONG).show();
        }

        String resName = restaurantModelClass.getResName().trim();
        Log.d("TAG1", resName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (first_name.equals("") && last_name.equals("")) {
                    Toast.makeText(context, "Please complete your profile first.", Toast.LENGTH_LONG).show();
                }
                else {
                    if (approved.equals("yes")) {
                        if (status.equals("online")) {
                            Intent intent = new Intent(context, RestaurantItems.class);
                            intent.putExtra("restaurant", resName);
                            intent.putExtra("first_name", first_name);
                            intent.putExtra("last_name", last_name);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                        else {
                            Toast.makeText(context, "Restaurant is offline for now, Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(context, "This restaurant is not approved yet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        firebaseFirestore.collection("Rating").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                ratingsList = Objects.requireNonNull(documentSnapshot.toObject(RatingsList.class)).Rating;
                                Log.d("TAG1", ratingsList.get(0).getKitchenName());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        float tempRating = 0f;

        resList.clear();

        for (int x = 0; x < ratingsList.size(); x++) {

            String kitchenName = ratingsList.get(x).getKitchenName();

            if (ratingsList.get(x).getKitchenName().equals(resName)){
                resList.add(ratingsList.get(x).getKitchenName());
            }

            if (kitchenName.equals(restaurantModelClass.getResName())){
                String itemName = ratingsList.get(x).getItemName();
                rating = ratingsList.get(x).getRating();

                tempRating += Float.parseFloat(rating);
            }
        }

        finalRating = tempRating/resList.size();

//        holder.ratingStar.setRating(finalRating);

        for (String id : Common.id) {

            firebaseFirestore.collection("Users").document(id)
                    .collection("Cart").whereEqualTo("restaurantName", resName).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (documentSnapshot.exists()) {
                                    size = task.getResult().size();

                                    Log.d("size", "" + size);

                                    holder.tvNoOrders.setText("(" + size + ")");
                                }
                            }
                            size = 0;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return resDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRestaurant;
        private final TextView tvRestaurant;
        private TextView tvNoOrders;
        private final LinearLayout layout;
        private AndRatingBar ratingStar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
            ratingStar = itemView.findViewById(R.id.ratingStar);
            tvNoOrders = itemView.findViewById(R.id.tvNoOrders);

            layout = itemView.findViewById(R.id.layout);

        }
    }
}
