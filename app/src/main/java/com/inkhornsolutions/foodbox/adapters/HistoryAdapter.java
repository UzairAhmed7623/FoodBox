package com.inkhornsolutions.foodbox.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.inkhornsolutions.foodbox.R;
import com.inkhornsolutions.foodbox.models.HistoryModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> history = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

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

        String resId = historyModelClass.getResId();
        String resName = historyModelClass.getResName();
        String totalPrice = historyModelClass.getTotalPrice();
        String status = historyModelClass.getStatus();
        String date = historyModelClass.getDate();

        holder.tvResNameHistory.setText(resName);
        holder.tvGrandTotalHistory.setText("Price: " + totalPrice);
        holder.tvDateHistory.setText("Date: " + date);
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.completed)).into(holder.ivStatusHistory);

        boolean isExpanded = history.get(position).isExpanded();

        holder.expandablelLayoutHistory.setVisibility(isExpanded ? View.VISIBLE: View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyModelClass.setExpanded(!historyModelClass.isExpanded());
                notifyItemChanged(position);
            }
        });

        ArrayList<HistoryModelClass> arrayListMember = new ArrayList<>();

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
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
                        holder.rvMemberHistory.setAdapter(new MemberHistoryAdapter(arrayListMember));

                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvResNameHistory, tvDateHistory, tvGrandTotalHistory;
        private ImageView ivStatusHistory;
        private LinearLayout expandablelLayoutHistory;
        private RecyclerView rvMemberHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            ivStatusHistory = (ImageView) itemView.findViewById(R.id.ivStatusHistory);
            tvResNameHistory = (TextView) itemView.findViewById(R.id.tvResNameHistory);
            tvDateHistory = (TextView) itemView.findViewById(R.id.tvDateHistory);
            tvGrandTotalHistory = (TextView) itemView.findViewById(R.id.tvGrandTotalHistory);
            expandablelLayoutHistory = (LinearLayout) itemView.findViewById(R.id.expandablelLayoutHistory);

            rvMemberHistory = (RecyclerView) itemView.findViewById(R.id.rvMemberHistory);
            rvMemberHistory.setLayoutManager(new LinearLayoutManager(context));

        }
    }
}
