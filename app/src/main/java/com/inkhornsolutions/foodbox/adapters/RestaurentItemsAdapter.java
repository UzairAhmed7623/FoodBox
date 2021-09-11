package com.inkhornsolutions.foodbox.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.inkhornsolutions.foodbox.Common.Common;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.ShowItemDetails;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

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

        if (viewType == 0) {
            View view = inflater.inflate(R.layout.restaurant_items_adapter_left, parent, false);
            return new ViewHolder(view);
        } else {
            View view1 = inflater.inflate(R.layout.restaurant_items_adapter_right, parent, false);
            return new ViewHolder(view1);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RestaurentItemsAdapter.ViewHolder holder, int position) {
        final ItemsModelClass itemsModelClass = productList.get(position);

        String imageUri = itemsModelClass.getImageUri();
        String from = itemsModelClass.getFrom();
        String to = itemsModelClass.getTo();

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        holder.tvItem.setText(itemsModelClass.getItemName());

        RequestOptions reqOpt = RequestOptions
                .fitCenterTransform()
                .transform(new CircleCrop())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .override(300, 300);

        Glide.with(context)
                .load(imageUri)
                .thumbnail(0.25f)
                .apply(reqOpt)
                .placeholder(R.drawable.food_placeholder)
                .into(holder.ivItem);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        String getCurrentDateTime = sdf.format(c.getTime());
        int compareFrom = getCurrentDateTime.compareTo(from);
        int compareTo = getCurrentDateTime.compareTo(to);

        if (Common.discountAvailable.get("available").toString().equals("yes")) {
            holder.discountLayout.setVisibility(View.VISIBLE);
            progressDialog.dismiss();

            holder.tvItemDiscount.setText("-" + Common.discountAvailable.get("percentage").toString() + "%");

            int discountedPrice = ((100 - Integer.parseInt(Common.discountAvailable.get("percentage").toString())) * Integer.parseInt(itemsModelClass.getPrice())) / 100;

            holder.tvItemPrice.setText("PKR" + discountedPrice);
            holder.tvItemCuttingPrice.setText("PKR" + itemsModelClass.getPrice());

            String resName = ((RestaurantItems) context).restaurant;

            name = itemsModelClass.getUserName();

            Log.d("time1time1", ""+getCurrentDateTime +" : "+from + " : " + to);

            Log.d("time1time2", ""+compareFrom +" : "+compareTo);

            if (compareFrom > 0 && compareTo < 0) {
                Log.d("time1time2", ""+compareFrom +" : "+compareTo);

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

            }
            else {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                holder.ivItem.setColorFilter(new ColorMatrixColorFilter(matrix));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toasty.error(context, "Sorry! This product is only available from " +from+" to "+to,Toasty.LENGTH_LONG).show();
                    }
                });
            }
        }
        else {
            holder.discountLayout.setVisibility(View.GONE);
            progressDialog.dismiss();

            holder.tvItemPrice.setText("PKR" + itemsModelClass.getPrice());
            holder.tvItemCuttingPrice.setText("PKR" + itemsModelClass.getPrice());

            String resName = ((RestaurantItems) context).restaurant;

            name = itemsModelClass.getUserName();

            if (compareFrom > 0 || compareTo < 0) {

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
            else {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                holder.ivItem.setColorFilter(new ColorMatrixColorFilter(matrix));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toasty.error(context, "Sorry! This product is only available from " +from+" to "+to,Toasty.LENGTH_LONG).show();
                    }
                });
            }
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

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("hh:mm aa", calendar).toString();


        return date;
    }
}
