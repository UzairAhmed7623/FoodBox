package com.inkhornsolutions.foodbox.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import org.jetbrains.annotations.NotNull;

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
        final ItemsModelClass modelClass = productList.get(position);

        holder.tvItem.setText(modelClass.getItemName());
        holder.tvItemPrice.setText("PKR"+modelClass.getPrice());
            Glide.with(context).load(modelClass.getImageUri()).placeholder(R.drawable.food_placeholder).fitCenter().into(holder.ivItem);
//            holder.tvItemSchedule.setText("Available from: "+ modelClass.getFrom()+" to "+modelClass.getTo());


        name = modelClass.getUserName();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showQuantityDialog(modelClass);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private double price = 0;
    private double finalPrice = 0;
    private int itemCount = 0;

    private void showQuantityDialog(ItemsModelClass modelClass) {

        View view = LayoutInflater.from(context).inflate(R.layout.quantity_dialog, null);

        CircleImageView ivItem1 = view.findViewById(R.id.ivItem1);
        CircleImageView ivItem2 = view.findViewById(R.id.ivItem2);
        CircleImageView ivItem3 = view.findViewById(R.id.ivItem3);

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

        Glide.with(context).load(image).into(ivItem1);
        Glide.with(context).load(image).into(ivItem2);
        Glide.with(context).load(image).into(ivItem3);
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
