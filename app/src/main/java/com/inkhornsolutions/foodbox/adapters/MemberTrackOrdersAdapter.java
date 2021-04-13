package com.inkhornsolutions.foodbox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;

import java.util.ArrayList;

public class MemberTrackOrdersAdapter extends RecyclerView.Adapter<MemberTrackOrdersAdapter.ViewHolder> {

    ArrayList<HistoryModelClass> arrayListMember;

    public MemberTrackOrdersAdapter(ArrayList<HistoryModelClass> arrayListMember) {
        this.arrayListMember = arrayListMember;
    }

    @NonNull
    @Override
    public MemberTrackOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.member_track_orders_adapter, parent, false);
        return new MemberTrackOrdersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberTrackOrdersAdapter.ViewHolder holder, int position) {

        HistoryModelClass memberTrackOrdersClass = arrayListMember.get(position);

        String id = memberTrackOrdersClass.getId();
        String itemName = memberTrackOrdersClass.getItemName();
        String price = memberTrackOrdersClass.getPrice();
        String itemCount = memberTrackOrdersClass.getItems_Count();
        String finalPrice = memberTrackOrdersClass.getFinalPrice();
        String pId = memberTrackOrdersClass.getpId();

        holder.tvItemTrack.setText(itemName);
        holder.tvOrderDateTrack.setText("Date: " + pId);
        holder.tvItemPriceTrack.setText("Price: " + price);
        holder.tvItemCountTrack.setText(itemCount);
        holder.tvTotalTrack.setText(finalPrice);

    }

    @Override
    public int getItemCount() {
        return arrayListMember.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvItemTrack, tvItemPriceTrack, tvItemCountTrack, tvTotalTrack, tvOrderDateTrack;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemTrack = (TextView) itemView.findViewById(R.id.tvItemNameTrack);
            tvItemPriceTrack = (TextView) itemView.findViewById(R.id.tvItemPriceTrack);
            tvItemCountTrack = (TextView) itemView.findViewById(R.id.tvItemCountTrack);
            tvTotalTrack = (TextView) itemView.findViewById(R.id.tvTotalTrack);
            tvOrderDateTrack = (TextView) itemView.findViewById(R.id.tvOrderDateTrack);
        }
    }
}
