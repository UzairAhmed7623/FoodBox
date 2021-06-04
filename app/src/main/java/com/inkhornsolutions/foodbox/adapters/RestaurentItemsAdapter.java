package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.ShowItemDetails;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.inkhornsolutions.foodbox.R;

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
        holder.tvItemPrice.setText("PKR"+itemsModelClass.getPrice());
        Glide.with(context).load(itemsModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);
//            holder.tvItemSchedule.setText("Available from: "+ modelClass.getFrom()+" to "+modelClass.getTo());

        String resName = ((RestaurantItems)context).restaurant;


        name = itemsModelClass.getUserName();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ShowItemDetails.class);
                intent.putExtra("resName",resName);
                intent.putExtra("itemName",itemsModelClass.getItemName());
                context.startActivity(intent);

//                showQuantityDialog(modelClass);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItem, tvItemPrice, tvItemSchedule;
        private CircleImageView ivItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            ivItem = itemView.findViewById(R.id.ivItem);
//            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);
        }
    }
}
