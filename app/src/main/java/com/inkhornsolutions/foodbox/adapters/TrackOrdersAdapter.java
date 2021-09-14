package com.inkhornsolutions.foodbox.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TrackOrdersAdapter extends RecyclerView.Adapter<TrackOrdersAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModelClass> trackOrders = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

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
                notifyItemChanged(holder.getBindingAdapterPosition());
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

                    }
                    holder.rvMember.setAdapter(new MemberTrackOrdersAdapter(arrayListMember));

                }
            }
        });

        holder.btnRateAndReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder popDialog = new AlertDialog.Builder(context);
                final RatingBar rating = new RatingBar(context);
                rating.setNumStars(5);
                rating.setStepSize(0.1f);
                rating.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout parent = new LinearLayout(context);
                parent.setGravity(Gravity.CENTER);
                parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                parent.addView(rating);

                // popDialog.setIcon(android.R.drawable.btn_star_big_on);
                popDialog.setTitle("Please give us rating.");
                popDialog.setView(parent);

                // Button OK
                popDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Log.d("rating", ""+rating.getRating());

                                holder.btnRateAndReview.setVisibility(View.GONE);
                                holder.tvShowRating.setVisibility(View.VISIBLE);
                                holder.tvShowRating.setText("You give " + rating.getRating() + " / "+"5.0");

                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                popDialog.create();
                popDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return trackOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStatusTrack, tvResNameTrack, tvDateTrack, tvGradTotalTrack, tvShowRating;
        private LinearLayout expandablelLayoutTrack;
        private RecyclerView rvMember;
        private MaterialButton btnRateAndReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            tvStatusTrack = (TextView) itemView.findViewById(R.id.tvStatusTrack);
            tvResNameTrack = (TextView) itemView.findViewById(R.id.tvResNameTrack);
            tvShowRating = (TextView) itemView.findViewById(R.id.tvShowRating);
            tvDateTrack = (TextView) itemView.findViewById(R.id.tvDateTrack);
            tvGradTotalTrack = (TextView) itemView.findViewById(R.id.tvGradTotalTrack);
            btnRateAndReview = (MaterialButton) itemView.findViewById(R.id.btnRateAndReview);
            expandablelLayoutTrack = (LinearLayout) itemView.findViewById(R.id.expandablelLayoutTrack);

            rvMember = (RecyclerView) itemView.findViewById(R.id.rvMember);
            rvMember.setLayoutManager(new LinearLayoutManager(context));

        }
    }
}
