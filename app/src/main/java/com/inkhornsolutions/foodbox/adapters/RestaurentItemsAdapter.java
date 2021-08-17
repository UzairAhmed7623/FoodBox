package com.inkhornsolutions.foodbox.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.ShowItemDetails;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.inkhornsolutions.foodbox.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RestaurentItemsAdapter extends RecyclerView.Adapter<RestaurentItemsAdapter.ViewHolder>{

    final private Context context;
    final private List<ItemsModelClass> productList;
    private String name;

    public RestaurentItemsAdapter(Context context, List<ItemsModelClass> productList) {
        this.context = context;
        this.productList = productList;
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
    @NotNull
    @Override
    public RestaurentItemsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0){
            View view = inflater.inflate(R.layout.restaurant_items_adapter_left, parent, false);
            return new ViewHolder(view);
        }
        else {
            View view = inflater.inflate(R.layout.restaurant_items_adapter_right, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RestaurentItemsAdapter.ViewHolder holder, int position) {
        final ItemsModelClass itemsModelClass = productList.get(position);

        holder.tvItem.setText(itemsModelClass.getItemName());
//        Glide.with(context).load(itemsModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);
//            holder.tvItemSchedule.setText("Available from: "+ modelClass.getFrom()+" to "+modelClass.getTo());

        Picasso.get().load(itemsModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fit().centerCrop().into(holder.ivItem);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference("Admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String percentage = snapshot.child("percentage").getValue(String.class);
                    String available = snapshot.child("available").getValue(String.class);

                    if (available.equals("yes")){
                        holder.discountLayout.setVisibility(View.VISIBLE);

                        holder.tvItemDiscount.setText("-" + percentage + "%");

                        int discountedPrice = ((100 - Integer.parseInt(percentage)) * Integer.parseInt(itemsModelClass.getPrice())) / 100;

                        holder.tvItemPrice.setText("PKR"+discountedPrice);
                        holder.tvItemCuttingPrice.setText("PKR"+itemsModelClass.getPrice());

                        String resName = ((RestaurantItems)context).restaurant;

                        name = itemsModelClass.getUserName();

                        progressDialog.dismiss();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(context, ShowItemDetails.class);
                                intent.putExtra("resName",resName);
                                intent.putExtra("itemName",itemsModelClass.getItemName());
                                intent.putExtra("available","yes");
                                intent.putExtra("percentage",percentage);
                                context.startActivity(intent);
                                ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        });
                    }
                    else {
                        holder.discountLayout.setVisibility(View.GONE);

                        holder.tvItemPrice.setText("PKR"+itemsModelClass.getPrice());
                        holder.tvItemCuttingPrice.setText("PKR"+itemsModelClass.getPrice());

                        String resName = ((RestaurantItems)context).restaurant;

                        name = itemsModelClass.getUserName();

                        progressDialog.dismiss();

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(context, ShowItemDetails.class);
                                intent.putExtra("resName",resName);
                                intent.putExtra("itemName",itemsModelClass.getItemName());
                                intent.putExtra("available","no");

                                context.startActivity(intent);
                                ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItem, tvItemPrice, tvItemCuttingPrice, tvItemDiscount, tvItemSchedule;
        private CircleImageView ivItem;
        private RelativeLayout discountLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            ivItem = itemView.findViewById(R.id.ivItem);
            tvItemCuttingPrice = itemView.findViewById(R.id.tvItemCuttingPrice);
            tvItemDiscount = itemView.findViewById(R.id.tvItemDiscount);
            discountLayout = itemView.findViewById(R.id.discountLayout);

//            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);
        }
    }
}
