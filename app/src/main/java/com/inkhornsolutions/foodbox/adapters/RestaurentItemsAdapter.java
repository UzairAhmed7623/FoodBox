package com.inkhornsolutions.foodbox.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.ShowItemDetails;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RestaurentItemsAdapter extends RecyclerView.Adapter<RestaurentItemsAdapter.ViewHolder> {

    final private Context context;
    final private List<ItemsModelClass> productList;
    private String name;

    public RestaurentItemsAdapter(Context context, List<ItemsModelClass> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @NotNull
    @Override
    public RestaurentItemsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        ViewHolder vh = null;

        switch (viewType){
            case 0:
                view = inflater.inflate(R.layout.restaurant_items_adapter_left, parent, false);
                vh = new ViewHolder(view);

            break;

            case 1:
                view = inflater.inflate(R.layout.restaurant_items_adapter_right, parent, false);
            vh = new ViewHolder(view);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RestaurentItemsAdapter.ViewHolder holder, int position) {
        final ItemsModelClass itemsModelClass = productList.get(position);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        holder.tvItem.setText(itemsModelClass.getItemName());

        RequestOptions reqOpt = RequestOptions
                .fitCenterTransform()
                .transform(new CircleCrop())
                .diskCacheStrategy(DiskCacheStrategy.DATA) // It will cache your image after loaded for first time
                .override(300,300)
                .priority(Priority.IMMEDIATE)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .format(DecodeFormat.DEFAULT);

//                Glide.with(context).load(itemsModelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter()
//                        .diskCacheStrategy(DiskCacheStrategy.DATA)
//                        .apply(new RequestOptions().override(150).override(250, 250))
//                        .into(holder.ivItem);

                Glide.with(context)
                        .load(itemsModelClass.getImageUri())
                        .thumbnail(0.25f)
                        .apply(reqOpt)
                        .placeholder(R.drawable.food_placeholder)
                        .into(holder.ivItem);

        if (Common.discountAvailable.get("available").toString().equals("yes")) {
            holder.discountLayout.setVisibility(View.VISIBLE);
            progressDialog.dismiss();

            holder.tvItemDiscount.setText("-" + Common.discountAvailable.get("percentage").toString() + "%");

            int discountedPrice = ((100 - Integer.parseInt(Common.discountAvailable.get("percentage").toString())) * Integer.parseInt(itemsModelClass.getPrice())) / 100;

            holder.tvItemPrice.setText("PKR" + discountedPrice);
            holder.tvItemCuttingPrice.setText("PKR" + itemsModelClass.getPrice());

            String resName = ((RestaurantItems) context).restaurant;

            name = itemsModelClass.getUserName();


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, ShowItemDetails.class);
                    intent.putExtra("resName", resName);
                    intent.putExtra("itemName", itemsModelClass.getItemName());
                    intent.putExtra("available", "yes");
                    intent.putExtra("percentage", Common.discountAvailable.get("percentage").toString());
                    context.startActivity(intent);
                    ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        } else {
            holder.discountLayout.setVisibility(View.GONE);
            progressDialog.dismiss();

            holder.tvItemPrice.setText("PKR" + itemsModelClass.getPrice());
            holder.tvItemCuttingPrice.setText("PKR" + itemsModelClass.getPrice());

            String resName = ((RestaurantItems) context).restaurant;

            name = itemsModelClass.getUserName();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, ShowItemDetails.class);
                    intent.putExtra("resName", resName);
                    intent.putExtra("itemName", itemsModelClass.getItemName());
                    intent.putExtra("available", "no");

                    context.startActivity(intent);
                    ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

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
