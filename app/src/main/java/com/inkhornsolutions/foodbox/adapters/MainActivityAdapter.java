package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private List<String> resturentsNames = new ArrayList<>();
    private List<String> imagesUri = new ArrayList<>();
    private List<String> statusList = new ArrayList<>();
    private List<String> approvalList = new ArrayList<>();
    private int checkedPosition = RecyclerView.NO_POSITION;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public MainActivityAdapter(Context context, List<String> resturentsNames, List<String> imagesUri, List<String> statusList, List<String> approvalList) {
        this.context = context;
        this.resturentsNames = resturentsNames;
        this.imagesUri = imagesUri;
        this.statusList = statusList;
        this.approvalList = approvalList;
    }

    @NonNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(parent.getContext());
        View view =inflater.inflate(R.layout.main_activity_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityAdapter.ViewHolder holder, int position) {
        String resName = resturentsNames.get(position);
        holder.tvRestaurant.setText(resName);

        String imageUri = imagesUri.get(position);
        Glide.with(context).load(imageUri).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivRestaurant);

        holder.itemView.setSelected(checkedPosition == position);

        String status = statusList.get(position);
        String approved = approvalList.get(position);

        if (approved.equals("yes")){
            if (status.equals("online")){

                holder.layout.setEnabled(true);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyItemChanged(checkedPosition);
                        checkedPosition = holder.getLayoutPosition();
                        notifyItemChanged(checkedPosition);

                        String resName = holder.tvRestaurant.getText().toString();

                        firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()){

                                        String first_name = documentSnapshot.getString("firstName");
                                        String last_name = documentSnapshot.getString("lastName");
                                        Intent intent = new Intent(context, RestaurantItems.class);
                                        intent.putExtra("restaurant", resName);
                                        intent.putExtra("first_name", first_name);
                                        intent.putExtra("last_name", last_name);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(v.findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(context.getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
                            }
                        });
                    }
                });
            }
            else {
                holder.layout.setEnabled(false);
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                holder.ivRestaurant.setColorFilter(new ColorMatrixColorFilter(matrix));
            }
        }
        else {
            Log.d("approval", "restaurant not approved yet");
        }
    }

    @Override
    public int getItemCount() {
        return resturentsNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRestaurant;
        private final TextView tvRestaurant;
        private final LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);

            layout = itemView.findViewById(R.id.layout);
        }
    }
}
