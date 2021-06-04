package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inkhornsolutions.foodbox.Cart;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.RestaurantItems;
import com.inkhornsolutions.foodbox.models.CartItemsModelClass;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class CartItemsAdapter extends RecyclerView.Adapter<CartItemsAdapter.HolderCartItem> {

    private Context context;
    private ArrayList<CartItemsModelClass> cartItems;

    public CartItemsAdapter(Context context, ArrayList<CartItemsModelClass> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_items, parent, false);
        return new HolderCartItem(view);
    }

    private int itemsCount = 0;
    public double allTotalPrice = 0.00;
    double Price;

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        CartItemsModelClass cartItemsModelClass = cartItems.get(position);

        String id = cartItemsModelClass.getId();
        String itemName = cartItemsModelClass.getItemName();
        String price = cartItemsModelClass.getPrice();
        String finalPrice = cartItemsModelClass.getFinalPrice();
        String Items_Count = cartItemsModelClass.getItems_Count();

        holder.tvItemTitle.setText(""+itemName);
        holder.tvItemPrice.setText("PKR"+finalPrice);
        holder.tvItemCount.setText(""+Items_Count);
        Glide.with(context).load(cartItemsModelClass.getItemImage()).placeholder(R.drawable.food_placeholder).into(holder.itemImage);

        holder.ibAddItemCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                itemsCount = Integer.parseInt(holder.tvItemCount.getText().toString().trim());
                itemsCount++;
//                Toast.makeText(context, ""+itemsCount, Toast.LENGTH_SHORT).show();
//                itemsCount = itemsCount + itemsCount;

                if (allTotalPrice == 0.00){
                    allTotalPrice = Double.parseDouble(((Cart)context).tvSubTotal.getText().toString().trim().replace("PKR ",""));
                }

                EasyDB easyDB = EasyDB.init(context, "ItemsDatabase")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("pId", new String[]{"text", "not null"}))
                        .addColumn(new Column("Title", new String[]{"text", "not null"}))
                        .addColumn(new Column("Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                        .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                        .doneTableColumn();

                Cursor cursor = easyDB.getAllData();
                while (cursor.moveToNext()){
                    int Id = cursor.getInt(1);

                    if (id.equals(String.valueOf(Id))){
                        boolean updated1 = easyDB.updateData(5, itemsCount).rowID(Id);
                        cartItemsModelClass.setItems_Count(String.valueOf(itemsCount));

                        if (updated1){
                            double finalPrice = Double.parseDouble(price) * itemsCount;

                            boolean updated2 = easyDB.updateData(6, String.valueOf(finalPrice)).rowID(Id);
                            cartItemsModelClass.setFinalPrice(String.valueOf(finalPrice));

                            if (updated2){
                                holder.tvItemPrice.setText("PKR"+finalPrice);

//                                Snackbar.make(v, "Added!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                                Price = cursor.getDouble(4);

                                allTotalPrice = allTotalPrice + Price;

                                ((Cart)context).tvSubTotal.setText("PKR " + String.format("%.2f", allTotalPrice));
                                String dFee = ((Cart)context).tvDeliveryFee.getText().toString().trim().replace("PKR ", "");
                                ((Cart)context).tvGrandTotal.setText("PKR " + (allTotalPrice + Double.parseDouble(dFee.replace("PKR ", ""))));
                            }
                            else {
                                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();

                            }
//                            Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                holder.tvItemCount.setText(""+itemsCount);

                itemsCount = 0;

            }
        });

        holder.ibRemoveItemCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();

                itemsCount = Integer.parseInt(holder.tvItemCount.getText().toString().trim());

                if (itemsCount > 1){
                    itemsCount--;
//                    Toast.makeText(context, ""+itemsCount, Toast.LENGTH_SHORT).show();
//                itemsCount = itemsCount + itemsCount;

                    if (allTotalPrice == 0.00){
                        allTotalPrice = Double.parseDouble(((Cart)context).tvSubTotal.getText().toString().trim().replace("PKR ",""));
                    }


                    EasyDB easyDB = EasyDB.init(context, "ItemsDatabase")
                            .setTableName("ITEMS_TABLE")
                            .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                            .addColumn(new Column("pId", new String[]{"text", "not null"}))
                            .addColumn(new Column("Title", new String[]{"text", "not null"}))
                            .addColumn(new Column("Price", new String[]{"text", "not null"}))
                            .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                            .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                            .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))
                            .doneTableColumn();

                    Cursor cursor = easyDB.getAllData();
                    while (cursor.moveToNext()){
                        int Id = cursor.getInt(1);

                        if (id.equals(String.valueOf(Id))){
                            boolean updated1 = easyDB.updateData(5, itemsCount).rowID(Id);
                            cartItemsModelClass.setItems_Count(String.valueOf(itemsCount));

                            if (updated1){
                                double finalPrice = Double.parseDouble(price) * itemsCount;

                                boolean updated2 = easyDB.updateData(6, String.valueOf(finalPrice)).rowID(Id);
                                cartItemsModelClass.setFinalPrice(String.valueOf(finalPrice));

                                if (updated2){
                                    holder.tvItemPrice.setText("PKR"+finalPrice);
//                                    Snackbar.make(v, "Deleted!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();

                                    Price = cursor.getDouble(4);

                                    allTotalPrice = allTotalPrice - Price;

                                    ((Cart)context).tvSubTotal.setText("PKR " + String.format("%.2f", allTotalPrice));
                                    String dFee = ((Cart)context).tvDeliveryFee.getText().toString().trim().replace("PKR ", "");
                                    ((Cart)context).tvGrandTotal.setText("PKR " + (allTotalPrice + Double.parseDouble(dFee.replace("PKR ", ""))));
                                }
                                else {
                                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();

                                }
//                                Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    holder.tvItemCount.setText(""+itemsCount);

                    itemsCount = 0;
                }
                else {

                    if (allTotalPrice == 0.00){
                        allTotalPrice = Double.parseDouble(((Cart)context).tvSubTotal.getText().toString().trim().replace("PKR ",""));
                    }

                    EasyDB easyDB = EasyDB.init(context, "ItemsDatabase")
                            .setTableName("ITEMS_TABLE")
                            .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                            .addColumn(new Column("pId", new String[]{"text", "not null"}))
                            .addColumn(new Column("Title", new String[]{"text", "not null"}))
                            .addColumn(new Column("Price", new String[]{"text", "not null"}))
                            .addColumn(new Column("Items_Count", new String[]{"text", "not null"}))
                            .addColumn(new Column("Final_Price", new String[]{"text", "not null"}))
//                .addColumn(new Column("Description", new String[]{"text", "not null"}))
                            .addColumn(new Column("Item_Image_Uri", new String[]{"text", "not null"}))

                            .doneTableColumn();

                    Cursor cursor = easyDB.getAllData();
                    while (cursor.moveToNext()){
                        int Id = cursor.getInt(1);

                        if (id.equals(String.valueOf(Id))){
                            boolean updated1 = easyDB.updateData(5, itemsCount).rowID(Id);
                            cartItemsModelClass.setItems_Count(String.valueOf(itemsCount));

                            if (updated1){
                                double finalPrice = Double.parseDouble(price) * itemsCount;

                                boolean updated2 = easyDB.updateData(6, String.valueOf(finalPrice)).rowID(Id);
                                cartItemsModelClass.setFinalPrice(String.valueOf(finalPrice));

                                if (updated2){
                                    holder.tvItemPrice.setText("PKR"+finalPrice);
//                                    Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();

                                    Price = cursor.getDouble(4);

                                    allTotalPrice = allTotalPrice - Price;

                                    ((Cart)context).tvSubTotal.setText("PKR " + String.format("%.2f", allTotalPrice));
                                    String dFee = ((Cart)context).tvDeliveryFee.getText().toString().trim().replace("PKR ", "");
                                    ((Cart)context).tvGrandTotal.setText("PKR " + (allTotalPrice + Double.parseDouble(dFee.replace("PKR ", ""))));
                                }
                                else {
                                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();

                                }
//                                Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    easyDB.deleteRow(1, id);
                    Snackbar.make(v, "Item deleted!", Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    cartItems.remove(position);
                    notifyItemChanged(position);
                    notifyDataSetChanged();

                    Cart.getInstance().updateNumberofItems();
                    RestaurantItems.getInstance().updateCartCount();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView tvItemTitle, tvItemPrice, tvPriceEach, tvItemCount;
        private FloatingActionButton ibAddItemCart, ibRemoveItemCart;
        private CircleImageView itemImage;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            tvItemTitle = (TextView) itemView.findViewById(R.id.tvItemTitle);
            tvItemPrice = (TextView) itemView.findViewById(R.id.tvItemPrice);
            tvItemCount = (TextView) itemView.findViewById(R.id.tvItemCount);
            itemImage = (CircleImageView) itemView.findViewById(R.id.itemImage);
            ibAddItemCart = (FloatingActionButton) itemView.findViewById(R.id.ibAddItemCart);
            ibRemoveItemCart = (FloatingActionButton) itemView.findViewById(R.id.ibRemoveItemCart);
        }
    }
}