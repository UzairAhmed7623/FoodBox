package com.inkhornsolutions.foodbox.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.models.ItemsModelClass;
import com.inkhornsolutions.foodbox.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class RestaurentItemsAdapter extends RecyclerView.Adapter<RestaurentItemsAdapter.ViewHolder>{

    final private Context context;
    final private List<ItemsModelClass> productList;
    private String name;

    public RestaurentItemsAdapter(Context context, List<ItemsModelClass> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public RestaurentItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.restaurant_items_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurentItemsAdapter.ViewHolder holder, int position) {
        final ItemsModelClass modelClass = productList.get(position);

        holder.tvItem.setText(modelClass.getItemName());
        holder.tvItemPrice.setText(modelClass.getPrice());
        Glide.with(context).load(modelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);
        holder.tvItemSchedule.setText("Available from: "+ modelClass.getSchedule());

        name = modelClass.getUserName();

        holder.ibSelectItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showQuantityDialog(modelClass, holder);

            }
        });

    }

    private double price = 0;
    private double finalPrice = 0;
    private int itemCount = 0;

    private void showQuantityDialog(ItemsModelClass modelClass, RestaurentItemsAdapter.ViewHolder holder) {

        View view = LayoutInflater.from(context).inflate(R.layout.quantity_dialog, null);

        CircleImageView ivItem = view.findViewById(R.id.ivItem);

        TextView tvItemTitle = view.findViewById(R.id.tvItemTitle);
        TextView tvItemPrice = view.findViewById(R.id.tvItemPrice);
        TextView tvItemPriceFinal = view.findViewById(R.id.tvItemPriceFinal);
        TextView tvCount = view.findViewById(R.id.tvCount);

        ImageButton ibAddItem = view.findViewById(R.id.ibAddItem);
        ImageButton ibRemoveItem = view.findViewById(R.id.ibRemoveItem);

        Button btnAddtoCart = view.findViewById(R.id.btnAddtoCart);

        final String productId = modelClass.getId();
        String title = modelClass.getItemName();
        String Price = modelClass.getPrice();
        String description = modelClass.getItemDescription();
        String image = modelClass.getImageUri();

        price = Double.parseDouble(Price.replace("PKR ",""));
        finalPrice = Double.parseDouble(Price.replace("PKR ",""));
        itemCount = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        Glide.with(context).load(image).into(ivItem);
        tvItemTitle.setText("" + title);
        tvItemPrice.setText("PKR " + price);
        tvItemPriceFinal.setText("PKR " + finalPrice);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ibAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalPrice = finalPrice + price;
                itemCount++;

                tvItemPriceFinal.setText("PKR " + finalPrice);
                tvCount.setText("" + itemCount);
            }
        });

        ibRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemCount > 1){
                    finalPrice = finalPrice - price;
                    itemCount--;

                    tvItemPriceFinal.setText("PKR " + finalPrice);
                    tvCount.setText("" + itemCount);
                }
            }
        });

        btnAddtoCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = tvItemTitle.getText().toString().trim();
                String price = tvItemPrice.getText().toString().trim().replace("PKR ","");
                String finalPrice = tvItemPriceFinal.getText().toString().trim().replace("PKR ","");
                String itemCount = tvCount.getText().toString().trim();

                addToCart(productId, title, price, finalPrice, itemCount);

                Log.d("btnAddtoCart2", title + price + finalPrice + itemCount);

                alertDialog.dismiss();

//                holder.tvaddItem.setVisibility(View.GONE);
//                holder.ibSelectItem.setVisibility(View.GONE);
//                holder.tvAdded.setVisibility(View.VISIBLE);

            }
        });
    }

    private int itemId = 0;
    private void addToCart(String productId, String title, String price, String finalPrice, String itemCount) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context, "DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("pId", new String[]{"text", "not null"}))
                .addColumn(new Column("Title", new String[]{"text", "not null"}))
                .addColumn(new Column("Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                .doneTableColumn();

        Log.d("btnAddtoCart3", productId + title + price + finalPrice + itemCount);

        boolean b = easyDB
                .addData("Item_Id", itemId)
                .addData("pId", productId)
                .addData("Title", title)
                .addData("Price", price)
                .addData("Items_Count", itemCount)
                .addData("Final_Price", finalPrice)
//                .addData("Description", description)
                .doneDataAdding();

        if (b){
            Snackbar.make(((RestaurantItems)context).findViewById(android.R.id.content), "Added to Cart!", Snackbar.LENGTH_SHORT).setBackgroundTint(context.getColor(R.color.myColor)).setTextColor(Color.WHITE).show();
        }
        else {
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
        }

        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            boolean update = easyDB.updateData(1,id).rowID(Integer.valueOf(id));
        }

        RestaurantItems.getInstance().updateCartCount();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItem, tvItemPrice, tvaddItem, tvAdded,  tvCount, tvItemSchedule;
        private ImageButton ibSelectItem, ibAddItem, ibRemoveItem;
        private ImageView ivItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tvItem);
            tvaddItem = itemView.findViewById(R.id.tvaddItem);
            tvAdded = itemView.findViewById(R.id.tvAdded);
            ibSelectItem = itemView.findViewById(R.id.ibSelectItem);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            ivItem = itemView.findViewById(R.id.ivItem);
            ibAddItem = itemView.findViewById(R.id.ibAddItem);
            ibRemoveItem = itemView.findViewById(R.id.ibRemoveItem);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvItemSchedule = (TextView) itemView.findViewById(R.id.tvItemSchedule);


        }
    }
}
