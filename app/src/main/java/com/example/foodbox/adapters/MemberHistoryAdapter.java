package com.example.foodbox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbox.R;
import com.example.foodbox.models.HistoryModelClass;

import java.util.ArrayList;

public class MemberHistoryAdapter extends RecyclerView.Adapter<MemberHistoryAdapter.ViewHolder> {

    ArrayList<HistoryModelClass> arrayListMember;

    public MemberHistoryAdapter(ArrayList<HistoryModelClass> arrayListMember) {
        this.arrayListMember = arrayListMember;
    }

    @NonNull
    @Override
    public MemberHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.member_history_adapter_layout, parent, false);
        return new MemberHistoryAdapter.ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull MemberHistoryAdapter.ViewHolder holder, int position) {
        HistoryModelClass memberTrackOrdersClass = arrayListMember.get(position);

        String id = memberTrackOrdersClass.getId();
        String itemName = memberTrackOrdersClass.getItemName();
        String price = memberTrackOrdersClass.getPrice();
        String itemCount = memberTrackOrdersClass.getItems_Count();
        String finalPrice = memberTrackOrdersClass.getFinalPrice();
        String pId = memberTrackOrdersClass.getpId();

        holder.tvItemNameHistory.setText(itemName);
        holder.tvOrderDateHistory.setText("Date: " + pId);
        holder.tvItemPriceHistory.setText("Price: " + price);
        holder.tvItemCountHistory.setText(itemCount);
        holder.tvTotalHistory.setText(finalPrice);
    }

    @Override
    public int getItemCount() {
        return arrayListMember.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemNameHistory, tvItemPriceHistory, tvItemCountHistory, tvTotalHistory, tvOrderDateHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemNameHistory = (TextView) itemView.findViewById(R.id.tvItemNameHistory);
            tvItemPriceHistory = (TextView) itemView.findViewById(R.id.tvItemPriceHistory);
            tvItemCountHistory = (TextView) itemView.findViewById(R.id.tvItemCountHistory);
            tvTotalHistory = (TextView) itemView.findViewById(R.id.tvTotalHistory);
            tvOrderDateHistory = (TextView) itemView.findViewById(R.id.tvOrderDateHistory);

        }
    }
}
