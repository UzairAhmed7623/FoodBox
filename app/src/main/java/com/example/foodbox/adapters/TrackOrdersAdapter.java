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
import com.example.foodbox.models.HistoryModelClass;

import java.util.ArrayList;
import java.util.List;

public class TrackOrdersAdapter extends RecyclerView.Adapter<TrackOrdersAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();

    public TrackOrdersAdapter(Context context, List<HistoryModelClass> trackOrders) {
        this.context = context;
        this.trackOrders = trackOrders;
    }

    @NonNull
    @Override
    public TrackOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.track_order_adapter_layout, parent, false);
        return new TrackOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackOrdersAdapter.ViewHolder holder, int position) {
        HistoryModelClass trackOrdersClass = trackOrders.get(position);

        String id = trackOrdersClass.getId();
        String itemName = trackOrdersClass.getItemName();
        String price = trackOrdersClass.getPrice();
        String itemCount = trackOrdersClass.getItems_Count();
        String finalPrice = trackOrdersClass.getFinalPrice();
        String pId = trackOrdersClass.getpId();
        String status = trackOrdersClass.getStatus();

        holder.tvItemTrack.setText(itemName);
        holder.tvOrderDateTrack.setText("Order date: " + pId);
        holder.tvItemPriceTrack.setText("Price: " + price);
        holder.tvItemCountTrack.setText(itemCount);
        holder.tvTotalTrack.setText(finalPrice);
        holder.tvStatusTrack.setText(status);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return trackOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemTrack, tvItemPriceTrack, tvItemCountTrack, tvTotalTrack, tvOrderDateTrack, tvStatusTrack;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemTrack = (TextView) itemView.findViewById(R.id.tvItemTrack);
            tvItemPriceTrack = (TextView) itemView.findViewById(R.id.tvItemPriceTrack);
            tvItemCountTrack = (TextView) itemView.findViewById(R.id.tvItemCountTrack);
            tvTotalTrack = (TextView) itemView.findViewById(R.id.tvTotalTrack);
            tvOrderDateTrack = (TextView) itemView.findViewById(R.id.tvOrderDateTrack);
            tvStatusTrack = (TextView) itemView.findViewById(R.id.tvStatusTrack);


        }
    }
}
