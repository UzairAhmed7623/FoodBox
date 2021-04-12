package com.example.foodbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodbox.R;
import com.example.foodbox.RestaurantItems;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private Context context;
    private List<String> resturentsNames = new ArrayList<>();
    private List<String> imagesUri = new ArrayList<>();
    private int checkedPosition = RecyclerView.NO_POSITION;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public MainActivityAdapter(Context context, List<String> resturentsNames, List<String> imagesUri) {
        this.context = context;
        this.resturentsNames = resturentsNames;
        this.imagesUri = imagesUri;
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
        Glide.with(context).load(imageUri).placeholder(R.drawable.placeholder).fitCenter().into(holder.ivRestaurant);

        holder.itemView.setSelected(checkedPosition == position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(checkedPosition);
                checkedPosition = holder.getLayoutPosition();
                notifyItemChanged(checkedPosition);

                String resName = holder.tvRestaurant.getText().toString();

                firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return resturentsNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivRestaurant;
        private TextView tvRestaurant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);



        }
    }
}
