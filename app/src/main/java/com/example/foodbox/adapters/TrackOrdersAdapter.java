package com.example.foodbox.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodbox.R;
import com.example.foodbox.TrackOrders;
import com.example.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrackOrdersAdapter extends RecyclerView.Adapter<TrackOrdersAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;

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

        String resId = trackOrdersClass.getResId();
        String resName = trackOrdersClass.getResName();
        String totalPrice = trackOrdersClass.getTotalPrice();
        String status = trackOrdersClass.getStatus();
        String date = trackOrdersClass.getDate();

        holder.tvResNameTrack.setText(resName);
        holder.tvGradTotalTrack.setText("Price: " + totalPrice);
        holder.tvDateTrack.setText("Date: " + date);
        holder.tvStatusTrack.setText(status);

        boolean isExpanded = trackOrders.get(position).isExpanded();

        holder.expandablelLayoutTrack.setVisibility(isExpanded ? View.VISIBLE: View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackOrdersClass.setExpanded(!trackOrdersClass.isExpanded());
                notifyItemChanged(position);
            }
        });

        ArrayList<HistoryModelClass> arrayListMember = new ArrayList<>();

        firebaseFirestore.collection("Users").document("cb0xbVIcK5dWphXuHIvVoUytfaM2")
                .collection("Cart").document(resId)
                .collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()){

                        String id = documentSnapshot1.getId();
                        String itemName = documentSnapshot1.getString("title");
                        String price = documentSnapshot1.getString("price");
                        String itemCount = documentSnapshot1.getString("items_count");
                        String finalPrice = documentSnapshot1.getString("final_price");
                        String pId = documentSnapshot1.getString("pId");

                        HistoryModelClass historyModelClass = new HistoryModelClass();

                        historyModelClass.setId(id);
                        historyModelClass.setItemName(itemName);
                        historyModelClass.setPrice(price);
                        historyModelClass.setItems_Count(itemCount);
                        historyModelClass.setFinalPrice(finalPrice);
                        historyModelClass.setpId(pId);

                        Log.d("asdfgh2", ""+id+itemName+price+itemCount+finalPrice+pId);

                        arrayListMember.add(historyModelClass);

                    }
                    holder.rvMember.setAdapter(new MemberTrackOrdersAdapter(arrayListMember));

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStatusTrack, tvResNameTrack, tvDateTrack, tvGradTotalTrack;
        private LinearLayout expandablelLayoutTrack;
        private RecyclerView rvMember;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseFirestore = FirebaseFirestore.getInstance();
            tvStatusTrack = (TextView) itemView.findViewById(R.id.tvStatusTrack);
            tvResNameTrack = (TextView) itemView.findViewById(R.id.tvResNameTrack);
            tvDateTrack = (TextView) itemView.findViewById(R.id.tvDateTrack);
            tvGradTotalTrack = (TextView) itemView.findViewById(R.id.tvGradTotalTrack);
            expandablelLayoutTrack = (LinearLayout) itemView.findViewById(R.id.expandablelLayoutTrack);

            rvMember = (RecyclerView) itemView.findViewById(R.id.rvMember);
            rvMember.setLayoutManager(new LinearLayoutManager(context));

        }
    }
}
