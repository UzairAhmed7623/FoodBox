package com.inkhornsolutions.foodbox.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.Image;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
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
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class RestaurentItemsAdapter extends RecyclerView.Adapter<RestaurentItemsAdapter.ViewHolder> {

    final private Context context;
    final private List<ItemsModelClass> productList;
    private String name;
    private OnImageListener mOnImageListener;

    public RestaurentItemsAdapter(Context context, List<ItemsModelClass> productList, OnImageListener onImageListener) {
        this.context = context;
        this.productList = productList;
        this.mOnImageListener = onImageListener;
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
            return new ViewHolder(view, mOnImageListener);
        } else {
            View view1 = inflater.inflate(R.layout.restaurant_items_adapter_right, parent, false);
            return new ViewHolder(view1, mOnImageListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RestaurentItemsAdapter.ViewHolder holder, int position) {
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_view_animation));

        final ItemsModelClass itemsModelClass = productList.get(position);

        String imageUri = itemsModelClass.getImageUri();
        String from = itemsModelClass.getFrom();
        String to = itemsModelClass.getTo();
        String available = itemsModelClass.getAvailable();

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        holder.tvItem.setText(itemsModelClass.getItemName());

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        String getCurrentDateTime = sdf.format(c.getTime());
        int compareFrom = getCurrentDateTime.compareTo(from);
        int compareTo = getCurrentDateTime.compareTo(to);

        if (available.equals("yes")) {
            if (Common.discountAvailable.get("available").toString().equals("yes")) {
                holder.discountLayout.setVisibility(View.VISIBLE);
                progressDialog.dismiss();

                holder.tvItemDiscount.setText("-" + Common.discountAvailable.get("percentage").toString() + "%");

                int discountedPrice = ((100 - Integer.parseInt(Common.discountAvailable.get("percentage").toString())) * Integer.parseInt(itemsModelClass.getPrice())) / 100;

                holder.tvItemPrice.setText("PKR" + discountedPrice);
                holder.tvItemCuttingPrice.setText("PKR" + itemsModelClass.getPrice());

                String resName = ((RestaurantItems) context).restaurant;

                name = itemsModelClass.getUserName();

                Log.d("time1time1", "" + getCurrentDateTime + " : " + from + " : " + to);

                Log.d("time1time2", "" + compareFrom + " : " + compareTo);

                if (compareFrom > 0 && compareTo < 0) {
                    Log.d("time1time2", "" + compareFrom + " : " + compareTo);

                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(context, ShowItemDetails.class);
                            intent.putExtra("resName", resName);
                            intent.putExtra("itemName", itemsModelClass.getItemName());
                            intent.putExtra("available", "yes");
                            intent.putExtra("percentage", Common.discountAvailable.get("percentage").toString());
                            context.startActivity(intent);
//                            ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });

                }
                else {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    holder.ivItem.setColorFilter(new ColorMatrixColorFilter(matrix));

                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toasty.error(context, "Sorry! This product is only available from " + from + " to " + to, Toasty.LENGTH_LONG).show();
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

                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(context, ShowItemDetails.class);
                            intent.putExtra("resName", resName);
                            intent.putExtra("itemName", itemsModelClass.getItemName());
                            intent.putExtra("available", "no");

                            context.startActivity(intent);
//                            ((RestaurantItems) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });
                }
                else {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    holder.ivItem.setColorFilter(new ColorMatrixColorFilter(matrix));

                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toasty.error(context, "Sorry! This product is only available from " + from + " to " + to, Toasty.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
        else {
            progressDialog.dismiss();

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            holder.ivItem.setColorFilter(new ColorMatrixColorFilter(matrix));

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toasty.error(context, "Sorry! This product is not available right now.", Toasty.LENGTH_LONG).show();
                }
            });
        }

        RequestOptions reqOpt = RequestOptions
                .fitCenterTransform()
                .transform(new CircleCrop())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(1024, 768)
                .priority(Priority.IMMEDIATE)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .format(DecodeFormat.DEFAULT);

        Glide.with(context)
                .load(imageUri)
                .thumbnail(0.5f)
                .apply(reqOpt)
                .placeholder(R.drawable.food_placeholder)
                .into(holder.ivItem);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvItem, tvItemPrice, tvItemCuttingPrice, tvItemDiscount, tvItemSchedule;
        private CircleImageView ivItem;
        private RelativeLayout discountLayout;
        OnImageListener onImageListener;
        CardView cardView;

        public ViewHolder(@NonNull View itemView, OnImageListener onImageListener) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            ivItem = itemView.findViewById(R.id.ivItem);
            tvItemCuttingPrice = itemView.findViewById(R.id.tvItemCuttingPrice);
            tvItemDiscount = itemView.findViewById(R.id.tvItemDiscount);
            discountLayout = itemView.findViewById(R.id.discountLayout);
            cardView = itemView.findViewById(R.id.cardView);

            this.onImageListener = onImageListener;

//            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onImageListener.onImageClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnImageListener {
        void onImageClick(int position);
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
