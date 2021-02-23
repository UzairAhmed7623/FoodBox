package com.example.foodbox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbox.R;
import com.example.foodbox.models.CartItemsModelClass;
import com.example.foodbox.models.HistoryModelClass;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> history = new ArrayList<>();

    public HistoryAdapter(Context context, List<HistoryModelClass> history) {
        this.context = context;
        this.history = history;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_history_adapter_layout, parent,false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        HistoryModelClass historyModelClass = history.get(position);

        String id = historyModelClass.getId();
        String itemName = historyModelClass.getItemName();
        String price = historyModelClass.getPrice();
        String itemCount = historyModelClass.getItems_Count();
        String finalPrice = historyModelClass.getFinalPrice();
        String pId = historyModelClass.getpId();

        holder.tvItemHistory.setText(itemName);
        holder.tvOrderDateHistory.setText(pId);
        holder.tvItemPriceHistory.setText(price);
        holder.tvItemCountHistory.setText(itemCount);
        holder.tvTotalHistory.setText(finalPrice);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemHistory, tvOrderDateHistory, tvItemPriceHistory, tvItemCountHistory, tvTotalHistory, tvProgressHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemHistory = itemView.findViewById(R.id.tvItemHistory);
            tvOrderDateHistory = itemView.findViewById(R.id.tvOrderDateHistory);
            tvItemPriceHistory = itemView.findViewById(R.id.tvItemPriceHistory);
            tvItemCountHistory = itemView.findViewById(R.id.tvItemCountHistory);
            tvTotalHistory = itemView.findViewById(R.id.tvTotalHistory);
            tvProgressHistory = itemView.findViewById(R.id.tvProgressHistory);


        }
    }
}
